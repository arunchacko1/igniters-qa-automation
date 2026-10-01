#!/usr/bin/env bash
# Starts Postgres + the SUT via docker compose and waits for the SUT to
# answer before returning, so scripts/run-tests.sh never races a half-booted app.
set -euo pipefail
cd "$(dirname "$0")/.."

if [ ! -f .env ]; then
  cp .env.example .env
  echo "Created .env from .env.example — edit it if you need different values."
fi

docker compose up -d --build

echo "Waiting for the SUT to become healthy..."
for i in $(seq 1 30); do
  if curl -sf http://localhost:8080/login > /dev/null; then
    echo "SUT is up."
    exit 0
  fi
  sleep 2
done

echo "SUT did not start in time." >&2
exit 1
