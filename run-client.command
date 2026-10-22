#!/bin/bash
set -e
cd "$(dirname "$0")"
HOST="${1:-localhost}"
PORT="${2:-3000}"
if ! command -v mvn >/dev/null 2>&1; then echo "Maven is required. Install with: brew install maven"; read -p "Press Enter to close..."; exit 1; fi
mvn -f client/pom.xml javafx:run -Djavafx.args="$HOST $PORT"
