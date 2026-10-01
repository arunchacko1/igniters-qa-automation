# Cloud Notes

This project runs entirely on localhost/CI today. Here's how it maps onto a
real cloud deployment, and what would actually change.

## The mapping

| Local piece | Cloud equivalent |
|---|---|
| `docker compose build` (sut-app/Dockerfile) | Push the built image to a **container registry** (e.g. Docker Hub, GitHub Container Registry, AWS ECR). CI would add a `docker build && docker push` step after the existing `package` step. |
| Postgres container in `docker-compose.yml` | A **managed Postgres** instance (e.g. AWS RDS, Supabase, Neon, Azure Database for PostgreSQL). Same Flyway migrations run against it unchanged — Flyway doesn't care whether the database is in a container or managed. |
| `java -jar sut-app.jar` on localhost:8080 | The container deployed to a **compute service** (e.g. AWS ECS/Fargate, Azure App Service, Google Cloud Run, Render, Railway) behind a public URL. |
| `BASE_URL=http://localhost:8080` in every test layer | `BASE_URL=https://staging.example.com` — nothing else about the tests changes, because the base URL was never hard-coded (see `TestConfig`, `playwright.config.ts`, and the Postman environment). |

## What a staging run would look like

1. CI builds and pushes the image on merge to `main`.
2. The compute service picks up the new image and redeploys (rolling or
   blue/green — either works since the app is stateless; the database holds
   all state).
3. A separate CI job (not yet in this repo — see "What I would add next" in
   the README) runs the **smoke suite only** against the staging URL:
   ```bash
   BASE_URL=https://staging.example.com \
   API_BASE_URL=https://staging.example.com/api \
   ./mvnw -f qa-tests/pom.xml test -Psmoke
   ```
4. Full regression and the Playwright/Newman suites still run against the
   disposable `docker compose` environment in CI, the same as today —
   there's no need to risk a shared staging database's seed data for a full
   regression pass on every PR.

## Secrets in a real cloud setup

Locally, `.env` holds the database password and is gitignored. In a cloud
pipeline, the same values would move to the CI provider's encrypted secrets
store (GitHub Actions **Repository secrets**, e.g. `STAGING_DB_PASSWORD`)
and be injected as environment variables at deploy time — never committed,
never printed in logs.

## What's intentionally not done here

- **No real deployment.** Standing up an actual managed Postgres + compute
  service costs real money and credentials this portfolio project doesn't
  need to depend on to make its point. The architecture above is the actual
  plan if/when it's deployed, not a hypothetical.
- **No infrastructure-as-code.** A real version of this would define the
  registry, database, and compute service in Terraform or a provider's own
  IaC tool rather than clicking through a console — left out here to keep
  the repo's scope to testing, which is what it's meant to demonstrate.
