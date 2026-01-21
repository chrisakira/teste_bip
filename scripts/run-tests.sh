#!/usr/bin/env bash
set -euo pipefail

# Runs the backend test suite with coverage and prints locations of reports.
# Usage: ./scripts/run-tests.sh [unit|integration|all] [-q|--quiet]
#   unit        - Run only unit tests (*Test.java, *UnitTest.java)
#   integration - Run only integration tests (*IntegrationTest.java)
#   all         - Run all tests (default if no argument provided)
#   -q, --quiet - Show only test summary (pretty output)

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/backend-module"

cd "$BACKEND_DIR"

# Parse arguments
TEST_TYPE="all"
QUIET=false

for arg in "$@"; do
    case "$arg" in
        -q|--quiet) QUIET=true ;;
        unit|integration|all) TEST_TYPE="$arg" ;;
    esac
done

# Build Maven command
MVN_OPTS="-Dnet.bytebuddy.experimental=true --batch-mode"

run_tests() {
    local test_filter="$1"
    local mvn_cmd="mvn test $MVN_OPTS"
    
    if [ -n "$test_filter" ]; then
        mvn_cmd="$mvn_cmd -Dtest=\"$test_filter\" -DfailIfNoTests=false"
    fi

    if $QUIET; then
        # Run and capture full output (not quiet mode - we need the data)
        echo -e "\n🧪 Running tests...\n"
        
        TMPFILE=$(mktemp)
        eval "$mvn_cmd" > "$TMPFILE" 2>&1 || true
        
        # Extract results from surefire summary line
        SUMMARY_LINE=$(grep -E "^\[INFO\] Tests run:" "$TMPFILE" | tail -1)
        
        if [ -n "$SUMMARY_LINE" ]; then
            TOTAL=$(echo "$SUMMARY_LINE" | grep -oP 'Tests run: \K[0-9]+')
            FAILURES=$(echo "$SUMMARY_LINE" | grep -oP 'Failures: \K[0-9]+')
            ERRORS=$(echo "$SUMMARY_LINE" | grep -oP 'Errors: \K[0-9]+')
            SKIPPED=$(echo "$SUMMARY_LINE" | grep -oP 'Skipped: \K[0-9]+')
            PASSED=$((TOTAL - FAILURES - ERRORS - SKIPPED))
        else
            TOTAL=0; FAILURES=0; ERRORS=0; SKIPPED=0; PASSED=0
        fi
        
        # Check if build succeeded
            if grep -q "BUILD SUCCESS" "$TMPFILE"; then
                # Generate JaCoCo report so we can show coverage percent
                mvn jacoco:report -Dnet.bytebuddy.experimental=true --batch-mode > /dev/null 2>&1 || true

                COVERAGE="n/a"
                if [ -f target/site/jacoco/index.html ]; then
                    COVERAGE=$(grep -oP '[0-9]+%' target/site/jacoco/index.html | head -1 || true)
                fi

                echo -e "────────────────────────────────────────"
                echo -e "  ✅ \033[32mALL TESTS PASSED\033[0m"
                echo -e "────────────────────────────────────────"
                echo -e "  📊 Total:    $TOTAL"
                echo -e "  ✓  Passed:   $PASSED"
                echo -e "  ✗  Failed:   $FAILURES"
                echo -e "  ⚠  Errors:   $ERRORS"
                echo -e "  ⏭  Skipped:  $SKIPPED"
                echo -e "  📈 Coverage:  $COVERAGE"
                echo -e "────────────────────────────────────────\n"
                rm -f "$TMPFILE"
                return 0
            else
                # Try to generate coverage even on failure to aid debugging
                mvn jacoco:report -Dnet.bytebuddy.experimental=true --batch-mode > /dev/null 2>&1 || true

                COVERAGE="n/a"
                if [ -f target/site/jacoco/index.html ]; then
                    COVERAGE=$(grep -oP '[0-9]+%' target/site/jacoco/index.html | head -1 || true)
                fi

                echo -e "────────────────────────────────────────"
                echo -e "  ❌ \033[31mTESTS FAILED\033[0m"
                echo -e "────────────────────────────────────────"
                echo -e "  📊 Total:    $TOTAL"
                echo -e "  ✓  Passed:   $PASSED"
                echo -e "  ✗  Failed:   $FAILURES"
                echo -e "  ⚠  Errors:   $ERRORS"
                echo -e "  ⏭  Skipped:  $SKIPPED"
                echo -e "  📈 Coverage:  $COVERAGE"
                echo -e "────────────────────────────────────────"
                echo -e "\n\033[33mFailed tests:\033[0m"
                grep -E "<<< FAILURE|<<< ERROR" "$TMPFILE" | head -10 || echo "  (check surefire reports for details)"
                echo -e "────────────────────────────────────────\n"
                rm -f "$TMPFILE"
                return 1
        fi
    else
        eval "$mvn_cmd"
    fi
}

case "$TEST_TYPE" in
    unit)
        echo "🔬 Running UNIT tests..."
        # Exclude integration tests - use Surefire exclusion pattern
        run_tests "**/*Test.java, **/*UnitTest.java, !**/*IntegrationTest.java"
        RC=$?
        ;;
    integration)
        echo "🔗 Running INTEGRATION tests..."
        run_tests "**/*IntegrationTest.java"
        RC=$?
        ;;
    all)
        echo "🧪 Running ALL tests..."
        run_tests ""
        RC=$?
        ;;
    *)
        echo "Usage: $0 [unit|integration|all] [-q|--quiet]"
        echo "  unit        - Run only unit tests"
        echo "  integration - Run only integration tests"
        echo "  all         - Run all tests (default)"
        echo "  -q, --quiet - Pretty summary output only"
        exit 1
        ;;
esac

if ! $QUIET; then
    echo
    echo "Surefire reports: $BACKEND_DIR/target/surefire-reports"
    echo "JaCoCo report: $BACKEND_DIR/target/site/jacoco/index.html"
fi

exit $RC

exit $RC
