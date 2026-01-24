#!/bin/sh
# Docker entrypoint script for loading secrets in Docker Swarm
# Secrets are mounted at /run/secrets/

SECRETS_FILE="/run/secrets/teste_bip_secrets"

if [ -f "$SECRETS_FILE" ]; then
    echo "Loading environment variables from Docker secrets..."
    # Source the secrets file to export variables
    set -a
    . "$SECRETS_FILE"
    set +a
    echo "Secrets loaded successfully."
fi

# Substitute environment variables in nginx config
envsubst '${BACKEND_API}' < /etc/nginx/templates/nginx.conf.template > /etc/nginx/nginx.conf

# Execute the command passed to the container
exec "$@"
