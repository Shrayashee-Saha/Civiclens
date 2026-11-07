// Report intake: validation, idempotent storage, rate limiting, and optional verified forwarding.
// Storage is a JSON file, which is fine for a prototype. Use a real database in production.

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const DATA_FILE = path.join(__dirname, 'data', 'reports.json');
const CONFIG_FILE = path.join(__dirname, 'offices.config.json');

const CATEGORIES = ['ROAD', 'STREETLIGHT', 'DRAINAGE', 'WASTE', 'FACILITY'];
const STATUSES = ['RECEIVED', 'FORWARDED', 'OFFICE_CONFIRMED', 'RESOLVED'];
const MAX_PER_HOUR = 20;
const rateWindows = new Map(); // ip -> { count, windowStart }

function loadReports() {
  try {
    return JSON.parse(fs.readFileSync(DATA_FILE, 'utf8'));
  } catch (e) {
    return [];
  }
}

function saveReports(list) {
  fs.mkdirSync(path.dirname(DATA_FILE), { recursive: true });
  fs.writeFileSync(DATA_FILE, JSON.stringify(list, null, 2));
}

function loadOffices() {
  try {
    return JSON.parse(fs.readFileSync(CONFIG_FILE, 'utf8')).offices || {};
  } catch (e) {
    return {};
  }
}

function rateLimited(ip) {
  const now = Date.now();
  const entry = rateWindows.get(ip);
  if (!entry || now - entry.windowStart > 60 * 60 * 1000) {
    rateWindows.set(ip, { count: 1, windowStart: now });
    return false;
  }
  entry.count += 1;
  return entry.count > MAX_PER_HOUR;
}

function validate(body) {
  if (typeof body.clientId !== 'string' || body.clientId.length < 8 || body.clientId.length > 64) {
    return 'clientId must be 8 to 64 characters';
  }
  if (!CATEGORIES.includes(body.category)) {
    return 'unknown category';
  }
  const description = typeof body.description === 'string' ? body.description.trim() : '';
  if (description.length < 20 || description.length > 500) {
    return 'description must be 20 to 500 characters';
  }
  if (typeof body.district !== 'string' || body.district.trim() === '' || body.district.length > 100) {
    return 'district is required';
  }
  if (body.locationNote !== undefined && (typeof body.locationNote !== 'string' || body.locationNote.length > 200)) {
    return 'locationNote must be 200 characters or fewer';
  }
  if (body.officeId !== undefined && body.officeId !== null && (typeof body.officeId !== 'string' || body.officeId.length > 64)) {
    return 'officeId is invalid';
  }
  if (typeof body.createdAt !== 'number') {
    return 'createdAt must be a number';
  }
  return null;
}

function publicView(r) {
  return { id: r.id, clientId: r.clientId, status: r.status, receivedAt: r.receivedAt };
}

// Returns { code, body }. Reports are only ever forwarded to offices that are verified and enabled.
async function createReport(body, ip) {
  if (rateLimited(ip)) {
    return { code: 429, body: { error: 'too many reports from this address, try later' } };
  }
  const problem = validate(body);
  if (problem) {
    return { code: 400, body: { error: problem } };
  }

  const list = loadReports();
  const existing = list.find((r) => r.clientId === body.clientId);
  if (existing) {
    return { code: 200, body: publicView(existing) }; // idempotent: a retry never creates a duplicate
  }

  const record = {
    id: crypto.randomUUID(),
    clientId: body.clientId,
    category: body.category,
    description: body.description.trim(),
    district: body.district.trim(),
    locationNote: body.locationNote || '',
    officeId: body.officeId || null,
    createdAt: body.createdAt,
    receivedAt: new Date().toISOString(),
    status: 'RECEIVED',
    forwarding: 'NOT_FORWARDED',
  };

  const office = record.officeId ? loadOffices()[record.officeId] : null;
  const forwardingOn = process.env.FORWARDING_ENABLED === 'true';
  if (forwardingOn && office && office.verified === true && office.webhookUrl) {
    try {
      const res = await fetch(office.webhookUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ reportId: record.id, category: record.category, district: record.district,
          description: record.description, locationNote: record.locationNote, createdAt: record.createdAt }),
      });
      if (res.ok) {
        record.status = 'FORWARDED';
        record.forwarding = 'FORWARDED_TO_OFFICE';
      } else {
        record.forwarding = 'FORWARD_FAILED';
      }
    } catch (e) {
      record.forwarding = 'FORWARD_FAILED';
    }
  }

  list.push(record);
  saveReports(list);
  return { code: 201, body: publicView(record) };
}

function getReport(id) {
  const record = loadReports().find((r) => r.id === id);
  return record ? { code: 200, body: publicView(record) } : { code: 404, body: { error: 'not found' } };
}

// Admin only. OFFICE_CONFIRMED and RESOLVED can come only from here, never from the app or automation.
function setStatus(id, status) {
  if (!STATUSES.includes(status)) {
    return { code: 400, body: { error: 'unknown status' } };
  }
  const list = loadReports();
  const record = list.find((r) => r.id === id);
  if (!record) {
    return { code: 404, body: { error: 'not found' } };
  }
  record.status = status;
  record.statusUpdatedAt = new Date().toISOString();
  saveReports(list);
  return { code: 200, body: publicView(record) };
}

module.exports = { createReport, getReport, setStatus };
