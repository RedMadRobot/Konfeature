#!/usr/bin/env bash

set -e

RESET="\033[0m"
F_BOLD="\033[1m"
F_BOLD_RESET="\033[22m"
C_RED="\033[31m"
C_GREEN="\033[32m"

function log_success() { printf -- "\n${F_BOLD}${C_GREEN}%s${RESET}\n" "$*"; }
function log_error() { printf -- "\n${F_BOLD}${C_RED}ERROR:${F_BOLD_RESET} %s${RESET}\n" "$*"; }

if [[ $# -ne 1 ]]; then
  log_error "Please specify the actual repository name. Usage: ./cleanup.sh MyRepositoryName"
  exit 1
fi

NAME="$1"
SAFE_NAME="$(echo $NAME | sed 's/[^a-zA-Z0-9]//g' | tr '[:upper:]' '[:lower:]')"

STUB="%Stub%"
SAFE_STUB="%stub%"

# Replace
sed -i "s/$SAFE_STUB/$SAFE_NAME/g" $(find . -type f -not -path "**/build/**" -not -path "**/.**")
sed -i "s/$STUB/$NAME/g" $(find . -type f -not -path "**/build/**" -not -path "**/.**")

# Move stub
mv stub/src/main/kotlin/Stub.kt stub/src/main/kotlin/$NAME.kt
mv stub/ $SAFE_NAME

# Cleanup
if [[ $GITHUB_ACTIONS ]]; then
  rm .github/workflows/cleanup.yml
  rm -- "$0"
fi

# Remove leftover empty directories
find . -type d -empty -delete
