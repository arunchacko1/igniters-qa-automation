#!/usr/bin/env bash
# Flyway applies db/migration/V2__seed_data.sql automatically on every
# startup against a fresh schema — there's no separate "seed" step to run.
# This script just gives you a clean database volume to apply it against
# again, for when test data has drifted too far from the original seed.
set -euo pipefail
cd "$(dirname "$0")/.."

echo "Dropping the database volume and restarting with a clean seed..."
docker compose down -v
./scripts/start.sh
