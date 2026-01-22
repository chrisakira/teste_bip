#!/bin/bash
# Run the backend Docker image
set -e
SCRIPT_DIR="$(dirname "$0")"
docker run --rm -p 8080:8080 --name bipapp-backend bipapp-backend:latest
