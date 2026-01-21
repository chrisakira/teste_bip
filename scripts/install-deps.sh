#!/usr/bin/env bash
set -euo pipefail

# Minimal pacman-only installer. Run as root (sudo).
# Usage: sudo ./scripts/install-deps.sh

if [ "$(id -u)" -ne 0 ]; then
  echo "Please run as root: sudo $0" >&2
  exit 1
fi

pacman -Syu --noconfirm
pacman -S --noconfirm jdk-openjdk maven nodejs npm

echo "Done. Installed jdk-openjdk, maven, nodejs, npm."
