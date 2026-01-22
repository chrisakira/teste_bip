#!/bin/bash
# Build the Docker image for the backend
set -e
cd "$(dirname "$0")/../backend-module"
docker build -t bipapp-backend:latest .
echo "Docker image 'bipapp-backend:latest' built successfully."
