# CivicLens proxy

One small Node server with two jobs. It holds the Gemini key, and it receives citizen reports.

## Start

Requires Node 18 or newer. There are no packages to install.

    GEMINI_API_KEY=your_key GEMINI_MODEL=your_model ADMIN_TOKEN=a_long_random_string node server.js

`GEMINI_MODEL` must be a current model name from Google AI Studio.

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | /ask | Assistant prompt. Returns `{text}`. |
| POST | /reports | Submit a report. Idempotent by `clientId`. Returns `{id, status}`. |
| GET | /reports/:id | Current status of a report. |
| POST | /admin/reports/:id/status | Admin only, with `Authorization: Bearer ADMIN_TOKEN`. Sets `OFFICE_CONFIRMED` or `RESOLVED`. |

## Status meanings

- **RECEIVED:** CivicLens stored the report. Nothing was sent to an office.
- **FORWARDED:** sent to an office that is verified and enabled. The office has not confirmed it yet.
- **OFFICE_CONFIRMED:** the office confirmed receipt. Set only by an admin, after confirmation.
- **RESOLVED:** the office marked the problem resolved. Set only by an admin.

## Turning on forwarding (only after agreement)

1. Get a written agreement with the office. Keep a copy.
2. Get the office's webhook URL or official intake address.
3. Edit `offices.config.json`:

       "off_works_1": {
         "verified": true,
         "webhookUrl": "https://office.example.gov/intake",
         "verifiedSource": "Agreement dated ..., signed by ..."
       }

4. Start the proxy with `FORWARDING_ENABLED=true`.

Without all of these, reports stay at RECEIVED. This is deliberate.

## Local testing

The emulator reaches this computer at 10.0.2.2. In the project's `local.properties`, add:

    report.url=http://10.0.2.2:8787

Debug builds allow plain http. Use https for any real deployment.

## Security notes

- Keep the keys and admin token in environment variables or a secrets manager, never in code.
- Rate limiting here is in memory and resets on restart. Use a shared store in production.
- Add authentication for citizen accounts before handling personal data at scale.
- Store reports in a real database with backups, and publish a privacy notice that says who can see them.
