#!/usr/bin/env bash
set -euo pipefail
# Usage: ./scripts/create_secret_from_env.sh secret_name [env_file]
# Default secret name ends with  so Spring can load it directly
SECRET_NAME=${1:-teste_bip_env}
ENV_FILE=${2:-.env}
if [ ! -f "$ENV_FILE" ]; then
  echo "Env file '$ENV_FILE' not found"
  exit 2
fi
# Create docker secret from the env file (will fail if secret already exists)
if docker secret inspect "$SECRET_NAME" >/dev/null 2>&1; then
  echo "Secret '$SECRET_NAME' already exists. Remove it first if you want to recreate."
  exit 0
fi
cat "$ENV_FILE" | docker secret create "$SECRET_NAME" -

echo "Created secret '$SECRET_NAME' from '$ENV_FILE'"
