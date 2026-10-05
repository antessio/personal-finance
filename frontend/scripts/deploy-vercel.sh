#!/usr/bin/env bash
# Deploy the frontend to Vercel.
#
# Usage:
#   API_URL=https://api.example.com ./scripts/deploy-vercel.sh            # production
#   API_URL=https://api.example.com ./scripts/deploy-vercel.sh preview    # preview
#   USE_MOCK=true ./scripts/deploy-vercel.sh                              # mock demo, no backend
#
# Env:
#   API_URL       Public HTTPS URL of the backend (required unless USE_MOCK=true)
#   AUTH_ENABLED  NEXT_PUBLIC_AUTH_ENABLED value (default: true)
#   USE_MOCK      NEXT_PUBLIC_USE_MOCK value (default: false)
#
# NEXT_PUBLIC_* vars are inlined at build time, so they are passed to the
# build via --build-env and --env on every deploy.

set -euo pipefail

cd "$(dirname "$0")/.."

TARGET="${1:-production}"
AUTH_ENABLED="${AUTH_ENABLED:-true}"
USE_MOCK="${USE_MOCK:-false}"
API_URL="${API_URL:-}"

case "$TARGET" in
  production|preview) ;;
  *) echo "Unknown target '$TARGET' (use 'production' or 'preview')" >&2; exit 1 ;;
esac

if [[ "$USE_MOCK" != "true" ]]; then
  if [[ -z "$API_URL" ]]; then
    echo "API_URL is required (or set USE_MOCK=true)" >&2
    exit 1
  fi
  if [[ "$API_URL" != https://* ]]; then
    echo "API_URL must be https:// — browsers block http:// calls from a Vercel (HTTPS) page" >&2
    exit 1
  fi
  if [[ "$AUTH_ENABLED" != "true" ]]; then
    echo "WARNING: deploying a publicly reachable app with auth disabled." >&2
    read -r -p "Continue? [y/N] " answer
    [[ "$answer" == "y" || "$answer" == "Y" ]] || exit 1
  fi
fi

if ! command -v vercel >/dev/null 2>&1; then
  echo "Vercel CLI not found. Install with: npm i -g vercel" >&2
  exit 1
fi

if [[ ! -d .vercel ]]; then
  echo "Project not linked yet — running 'vercel link'"
  vercel link
fi

ENV_ARGS=(
  --build-env "NEXT_PUBLIC_USE_MOCK=$USE_MOCK"
  --build-env "NEXT_PUBLIC_AUTH_ENABLED=$AUTH_ENABLED"
)
if [[ -n "$API_URL" ]]; then
  ENV_ARGS+=(--build-env "NEXT_PUBLIC_API_URL=$API_URL")
fi

DEPLOY_ARGS=()
[[ "$TARGET" == "production" ]] && DEPLOY_ARGS+=(--prod)

echo "Deploying ($TARGET): API_URL=${API_URL:-<none>} AUTH_ENABLED=$AUTH_ENABLED USE_MOCK=$USE_MOCK"
vercel deploy "${DEPLOY_ARGS[@]}" "${ENV_ARGS[@]}"
