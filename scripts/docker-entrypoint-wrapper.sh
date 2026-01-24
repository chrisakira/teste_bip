#!/bin/bash
# Script to load environment variables from Docker secrets file
# This is used in Docker Swarm deployment

SECRETS_FILE="/run/secrets/teste_bip_secrets"

if [ -f "$SECRETS_FILE" ]; then
    echo "Loading secrets from $SECRETS_FILE"
    set -a  # automatically export all variables
    source "$SECRETS_FILE"
    set +a
fi

# Execute the original command
exec "$@"
