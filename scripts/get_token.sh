#!/bin/bash
set -e

BASE_URL="${BASE_URL:-http://localhost:8080}"

usage() {
    echo "Usage: $0 [--url <base_url>] [--username <email>] [--password <password>]"
    echo ""
    echo "Prints the JWT token to stdout so it can be captured:"
    echo "  TOKEN=\$(./scripts/get_token.sh)"
    echo "  ./scripts/export_all.sh --token \"\$TOKEN\""
    exit 1
}

USERNAME=""
PASSWORD=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        --url)      BASE_URL="$2"; shift 2 ;;
        --username) USERNAME="$2"; shift 2 ;;
        --password) PASSWORD="$2"; shift 2 ;;
        *) usage ;;
    esac
done

if [[ -z "$USERNAME" ]]; then
    read -rp "Username (email): " USERNAME
fi

if [[ -z "$PASSWORD" ]]; then
    read -rsp "Password: " PASSWORD
    echo >&2
fi

RESPONSE=$(curl -s -w "\n%{http_code}" \
    -X POST "$BASE_URL/public/api/users/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\": \"$USERNAME\", \"password\": \"$PASSWORD\"}")

HTTP_CODE=$(echo "$RESPONSE" | tail -1)
TOKEN=$(echo "$RESPONSE" | head -1)

if [[ "$HTTP_CODE" != "200" ]]; then
    echo "Login failed (HTTP $HTTP_CODE)" >&2
    exit 1
fi

echo "$TOKEN"
