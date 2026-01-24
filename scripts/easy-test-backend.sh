#!/usr/bin/env bash
set -euo pipefail

# Simple smoke-test script for the backend REST API.
# Usage: ./scripts/test-backend.sh [BASE_URL]
# Example: ./scripts/test-backend.sh http://localhost:8080

BASE=${1:-http://localhost:8080}

echo "Using BASE=$BASE"

FAIL=0
PASS=0

ok(){ echo "[OK] $1"; PASS=$((PASS+1)); }
err(){ echo "[FAIL] $1"; FAIL=$((FAIL+1)); }

do_check(){
  method=$1; url=$2; data=$3; expect_code=$4; expect_body=${5:-}
  if [ "$method" = "GET" ] || [ "$method" = "DELETE" ]; then
    resp=$(curl -s -w "\n%{http_code}" -X "$method" "$url")
  else
    resp=$(curl -s -w "\n%{http_code}" -H "Content-Type: application/json" -d "$data" -X "$method" "$url")
  fi

  body=$(echo "$resp" | sed '$d')
  code=$(echo "$resp" | tail -n1)

  if [ "$code" != "$expect_code" ]; then
    err "$method $url -> expected HTTP $expect_code, got $code"
    echo "$body"
    return
  fi

  if [ -n "$expect_body" ]; then
    if ! echo "$body" | grep -qF "$expect_body"; then
      err "$method $url -> body did not contain '$expect_body'"
      echo "$body"
      return
    fi
  fi

  ok "$method $url -> $code"
}

echo "\n1) List all beneficios (expect 200 and 'Beneficio A')"
do_check GET "$BASE/api/v1/beneficios" "" 200 "Beneficio A"

echo "\n2) Get beneficio id=1 (expect 200)"
do_check GET "$BASE/api/v1/beneficios/1" "" 200 "Beneficio A"

echo "\n3) Create a temporary beneficio (expect 201)"
create_resp=$(curl -s -w "\n%{http_code}" -H "Content-Type: application/json" -d '{"nome":"Temp","descricao":"temp","valor":10.00,"ativo":true}' -X POST "$BASE/api/v1/beneficios")
create_body=$(echo "$create_resp" | sed '$d')
create_code=$(echo "$create_resp" | tail -n1)
if [ "$create_code" != "201" ]; then
  err "POST /api/v1/beneficios -> expected 201, got $create_code"; echo "$create_body"
else
  ok "POST /api/v1/beneficios -> 201"
  new_id=$(echo "$create_body" | sed -n 's/.*"id"[[:space:]]*:[[:space:]]*\([0-9][0-9]*\).*/\1/p') || true
  if [ -z "${new_id:-}" ]; then
    err "Could not parse id from response: $create_body"
  else
    ok "Created id=$new_id"
  fi
fi

echo "\n4) Transfer 100 from 1 -> 2 (expect 200 and success message)"
do_check POST "$BASE/api/v1/beneficios/transferir" '{"origemId":1,"destinoId":2,"valor":100}' 200 "Transferência realizada com sucesso"

echo "\n5) Transfer huge amount from 2 -> 1 (expect 400 insufficient funds)"
do_check POST "$BASE/api/v1/beneficios/transferir" '{"origemId":2,"destinoId":1,"valor":100000000}' 400 "Saldo insuficiente"

if [ -n "${new_id:-}" ]; then
  echo "\n6) Delete created beneficio id=$new_id (expect 204)"
  do_check DELETE "$BASE/api/v1/beneficios/$new_id" "" 204 ""
fi

echo "\nSummary: $PASS passed, $FAIL failed"
if [ "$FAIL" -ne 0 ]; then
  exit 1
fi
exit 0
