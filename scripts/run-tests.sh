#!/usr/bin/env bash
# Runs every test suite against whatever SUT is already running at $BASE_URL
# (defaults to localhost — see scripts/start.sh). Assumes the SUT is already up.
set -euo pipefail
cd "$(dirname "$0")/.."

export BASE_URL="${BASE_URL:-http://localhost:8080}"
export API_BASE_URL="${API_BASE_URL:-http://localhost:8080/api}"
export DB_HOST="${DB_HOST:-localhost}"
export DB_PORT="${DB_PORT:-5433}"
export POSTGRES_DB="${POSTGRES_DB:-igniters_qa}"
export POSTGRES_USER="${POSTGRES_USER:-igniters}"
export POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-change-me-locally}"
export SELENIUM_HEADLESS="${SELENIUM_HEADLESS:-true}"

echo "== Selenium + REST Assured + JDBC (full suite, no profile = everything) =="
./mvnw -f qa-tests/pom.xml -B test

echo "== Playwright =="
(cd playwright-tests && npm ci && npx playwright install --with-deps chromium && npx playwright test)

echo "== Postman / Newman =="
npx --yes newman run postman/igniters-collection.json -e postman/igniters-environment.json

echo "All suites passed."
