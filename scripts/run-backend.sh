#!/usr/bin/env bash
set -euo pipefail

# Builds and runs the backend-module Spring Boot app.
# Usage:
#   ./scripts/run-backend.sh        # build (skip tests) then run
#   ./scripts/run-backend.sh --run  # run without building (requires existing jar)
#   ./scripts/run-backend.sh --no-skip-tests  # build and run with tests
#   ./scripts/run-backend.sh --mvn-run  # run via 'mvn spring-boot:run'

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/backend-module"

SKIP_TESTS=1
MVN_RUN=0

for arg in "$@"; do
  case "$arg" in
    --no-skip-tests) SKIP_TESTS=0 ;;
    --run) SKIP_TESTS=1; BUILD_ONLY=0; RUN_ONLY=1 ;;
    --mvn-run) MVN_RUN=1 ;;
    -h|--help) echo "Usage: $0 [--no-skip-tests] [--run] [--mvn-run]"; exit 0 ;;
    *) ;;
  esac
done

if ! command -v mvn >/dev/null 2>&1; then
  echo "maven (mvn) not found in PATH. Install Maven to proceed." >&2
  exit 2
fi

if [ "$MVN_RUN" -eq 1 ]; then
  echo "Running via Maven Spring Boot plugin (mvn spring-boot:run)..."
  (cd "$BACKEND_DIR" && mvn spring-boot:run)
  exit $?
fi

echo "Building backend-module..."
if [ "$SKIP_TESTS" -eq 1 ]; then
  mvn -f "$BACKEND_DIR/pom.xml" clean package -DskipTests
else
  mvn -f "$BACKEND_DIR/pom.xml" clean package
fi

echo "Locating generated jar..."
JAR=$(ls "$BACKEND_DIR/target/"*.jar 2>/dev/null | grep -v '\-sources\.jar$' | grep -v '\-javadoc\.jar$' | grep -v '\-original\.jar$' | head -n1 || true)

if [ -z "$JAR" ]; then
  echo "No jar found in $BACKEND_DIR/target. Build may have failed." >&2
  exit 3
fi

echo "Running $JAR..."
exec java -jar "$JAR"
