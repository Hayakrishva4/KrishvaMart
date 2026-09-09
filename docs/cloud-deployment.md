# Cloud Deployment Guide

This project now builds as a single Docker image (see `Dockerfile`) that
runs unmodified on any container-hosting cloud platform. All config comes
from environment variables (`ConfigResolver` - env var wins over
`config.properties` wins over a hardcoded default), and the database
schema + demo data self-initialize on first boot (`SchemaInitializer`) - so
there's no SSH/exec step required to get a fresh deployment usable.

## Important: H2 persistence on ephemeral platforms

H2 (as specified, Section 3) stores data in a file. Most PaaS free tiers
(Render's free web service, Railway's default, Fly.io without a volume)
give you an **ephemeral filesystem** - anything written to disk is lost on
every redeploy or restart. Three ways to handle this, in order of
preference for a capstone project:

1. **Attach a persistent volume/disk** to the H2 container specifically
   (Render persistent disks, Fly.io volumes, a Docker volume on a VM you
   control - `docker-compose.yml` already does this locally via the
   `h2-data` named volume). This is the closest to what Section 10
   describes and keeps H2 exactly as specified.
2. **Run H2 on a small persistent VM** instead of a PaaS container (a
   free-tier Oracle Cloud VM, a $4-6/mo DigitalOcean droplet, etc.) using
   the `deploy/krishvamart-h2.service` systemd unit, and point your PaaS
   app container's `DB_URL` at that VM's public/private IP.
3. **Accept ephemeral data for a demo deployment** - fine for showing the
   app works at a public URL for the Final Review, since
   `SchemaInitializer` reseeds demo accounts/products automatically on
   every restart anyway. Just don't rely on buyer-created data (orders,
   reviews) surviving a redeploy.

## Option A: Render (Docker-based Web Service) - recommended for students

1. Push this repo to GitHub (see the git bundle / earlier instructions if
   you haven't already).
2. On [render.com](https://render.com): New -> Web Service -> connect your
   GitHub repo -> Environment: **Docker** (it will find the `Dockerfile`
   automatically).
3. Add environment variables under the service's "Environment" tab:
   - `AI_CHATBOT_PROVIDER=mock` (or `gemini` + `AI_CHATBOT_API_KEY` if you
     have a key)
   - Leave `DB_URL` unset to use the default embedded-file path inside the
     container (ephemeral - see above), **or** set it to point at a
     separately hosted H2 server (option 2 above) or swap H2 for Render's
     managed Postgres (see "Using a different database" below).
4. Add a **persistent disk** (Render dashboard -> your service -> Disks) if
   you want data to survive restarts, mounted at e.g. `/data`, and set
   `DB_URL=jdbc:h2:/data/krishvamart` (embedded file mode - no separate H2
   server container needed for this option).
5. Deploy. Render builds the Docker image and gives you a public HTTPS URL
   like `https://krishvamart.onrender.com` - that's your "Deployed link"
   for the README and Final Review.
6. Health check path: set it to `/api/v1/health` in Render's settings so
   the platform can tell the app booted correctly.

## Option B: Railway

1. New Project -> Deploy from GitHub repo -> it detects the `Dockerfile`.
2. Add the same environment variables as above under Variables.
3. Railway injects a `PORT` env var - the custom `deploy/docker-server.xml`
   baked into the image already makes Tomcat listen on it (falls back to
   8080 if `PORT` isn't set).
4. Add a Volume (Railway dashboard -> your service -> Volumes) if you want
   persistent H2 storage, mounted at `/data`, `DB_URL=jdbc:h2:/data/krishvamart`.
5. Railway gives you a public URL under Settings -> Networking -> Generate
   Domain.

## Option C: Any VM you control (most faithful to Section 10's reference setup)

Use the systemd + Nginx configs already in `deploy/` (H2 as its own
service, Tomcat as its own service, Nginx reverse proxy with TLS) - see
`deploy/README.md` for the exact steps. This is the closest match to
Section 10's literal reference setup and gives you full control over
persistence, but requires you to provision and maintain the VM yourself.

## Option D: Docker Compose on a single VM

```bash
git clone <your-repo-url> krishvamart && cd krishvamart
docker compose up -d --build
```
Point a domain + reverse proxy (see `deploy/nginx-krishvamart.conf`) at
port 8080 on that VM. Data persists in the `h2-data` Docker volume across
container restarts as long as you don't delete the volume.

## Using a different database instead of H2

Section 3 specifies H2, but if your faculty guide is fine with it (or for a
production-style deployment after the checkpoint), swap in any JDBC
database with genuine persistence (managed Postgres/MySQL from your cloud
platform) by just changing three environment variables - no code changes:

```
DB_URL=jdbc:postgresql://<host>:5432/<dbname>
DB_USER=<user>
DB_PASSWORD=<password>
```

You'd also need to add the matching JDBC driver dependency to `pom.xml` and
adjust `db/schema.sql`'s H2-specific syntax (`AUTO_INCREMENT`, `IF NOT
EXISTS` variants) for the target dialect - this is a bigger change than the
scaffold covers, and stepping outside the spec's H2 requirement should be
cleared with your faculty guide first.

## Verifying a deployment

Once live, run through `docs/test-cases.md` against the real URL (not
localhost) and update `docs/regression-checklist.md`. `GET
https://your-url/api/v1/health` should return `{"status":"UP","db":"UP"}`.
