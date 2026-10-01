#!/usr/bin/env bash
# The one command the README promises: start the stack, run every suite.
set -euo pipefail
cd "$(dirname "$0")/.."

./scripts/start.sh
./scripts/run-tests.sh
