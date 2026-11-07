// CivicLens BD proxy: AI assistant (/ask) and report intake (/reports).
// Secrets live only in environment variables. Android apps never see them.
//
// Start (Node 18 or newer, no npm install needed):
//   GEMINI_API_KEY=... GEMINI_MODEL=... ADMIN_TOKEN=... node server.js
// Optional: FORWARDING_ENABLED=true (only after offices are verified in offices.config.json)

const http = require('http');
const { createReport, getReport, setStatus } = require('./reports');

const PORT = process.env.PORT || 8787;
const API_KEY = process.env.GEMINI_API_KEY;
const MODEL = process.env.GEMINI_MODEL;
const ADMIN_TOKEN = process.env.ADMIN_TOKEN;
const MAX_BODY_BYTES = 40000;
const MAX_PROMPT_CHARS = 20000;

if (!API_KEY || !MODEL) {
  console.error('Set GEMINI_API_KEY and GEMINI_MODEL before starting the proxy.');
  process.exit(1);
}

function send(res, status, payload) {
  res.writeHead(status, { 'Content-Type': 'application/json; charset=utf-8' });
  res.end(JSON.stringify(payload));
}

function readJson(req, res, handler) {
  let body = '';
  let tooLarge = false;
  req.on('data', (chunk) => {
    body += chunk;
    if (body.length > MAX_BODY_BYTES) {
      tooLarge = true;
      req.destroy();
    }
  });
  req.on('end', async () => {
    if (tooLarge) {
      return send(res, 413, { error: 'request too large' });
    }
    let parsed;
    try {
      parsed = body === '' ? {} : JSON.parse(body);
    } catch (e) {
      return send(res, 400, { error: 'invalid JSON' });
    }
    try {
      await handler(parsed);
    } catch (err) {
      console.error('handler error:', err.message);
      send(res, 500, { error: 'internal error' });
    }
  });
}

async function askGemini(prompt) {
  const upstream = await fetch(
    `https://generativelanguage.googleapis.com/v1beta/models/${MODEL}:generateContent`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'x-goog-api-key': API_KEY },
      body: JSON.stringify({ contents: [{ parts: [{ text: prompt }] }] }),
    }
  );
  const data = await upstream.json();
  const text = (data?.candidates?.[0]?.content?.parts || []).map((p) => p.text || '').join('');
  return { ok: upstream.ok && text !== '', text };
}

const server = http.createServer((req, res) => {
  const ip = req.socket.remoteAddress || 'unknown';

  // POST /ask
  if (req.method === 'POST' && req.url === '/ask') {
    return readJson(req, res, async (body) => {
      const prompt = body.prompt;
      if (typeof prompt !== 'string' || prompt.trim() === '' || prompt.length > MAX_PROMPT_CHARS) {
        return send(res, 400, { error: 'prompt missing or too long' });
      }
      const result = await askGemini(prompt);
      if (!result.ok) {
        return send(res, 502, { error: 'assistant unavailable' });
      }
      return send(res, 200, { text: result.text });
    });
  }

  // POST /reports
  if (req.method === 'POST' && req.url === '/reports') {
    return readJson(req, res, async (body) => {
      const result = await createReport(body, ip);
      send(res, result.code, result.body);
    });
  }

  // GET /reports/:id
  const getMatch = req.method === 'GET' && req.url.match(/^\/reports\/([0-9a-f-]{36})$/);
  if (getMatch) {
    const result = getReport(getMatch[1]);
    return send(res, result.code, result.body);
  }

  // POST /admin/reports/:id/status  (Authorization: Bearer ADMIN_TOKEN)
  const adminMatch = req.method === 'POST' && req.url.match(/^\/admin\/reports\/([0-9a-f-]{36})\/status$/);
  if (adminMatch) {
    if (!ADMIN_TOKEN) {
      return send(res, 503, { error: 'admin disabled: ADMIN_TOKEN not set' });
    }
    if (req.headers.authorization !== `Bearer ${ADMIN_TOKEN}`) {
      return send(res, 401, { error: 'unauthorized' });
    }
    return readJson(req, res, async (body) => {
      const result = setStatus(adminMatch[1], body.status);
      send(res, result.code, result.body);
    });
  }

  return send(res, 404, { error: 'not found' });
});

server.listen(PORT, () => {
  console.log(`CivicLens proxy listening on port ${PORT}`);
});
