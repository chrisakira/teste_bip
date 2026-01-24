#!/bin/bash
# Docker entrypoint script for loading secrets in Docker Swarm
# Secrets are mounted at /run/secrets/

SECRETS_FILE="/run/secrets/teste_bip_secrets"

if [ -f "$SECRETS_FILE" ]; then
    echo "Loading environment variables from Docker secrets..."
    set -a  # automatically export all variables
    source "$SECRETS_FILE"
    set +a
    echo "Secrets loaded successfully."
fi

# Execute the command passed to the container
exec "$@"
