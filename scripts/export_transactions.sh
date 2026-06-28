#!/bin/bash
set -e

BASE_URL="${BASE_URL:-http://localhost:8080}"
OUTPUT_DIR="${OUTPUT_DIR:-./exports}"
TOKEN=""

usage() {
    echo "Usage: $0 [--token <jwt>] [--url <base_url>] [--out <output_dir>]"
    echo "  --token   JWT bearer token (required when auth is enabled)"
    echo "  --url     API base URL (default: http://localhost:8080)"
    echo "  --out     Output directory (default: ./exports)"
    exit 1
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --token) TOKEN="$2"; shift 2 ;;
        --url)   BASE_URL="$2"; shift 2 ;;
        --out)   OUTPUT_DIR="$2"; shift 2 ;;
        *) usage ;;
    esac
done

AUTH_HEADER=()
if [[ -n "$TOKEN" ]]; then
    AUTH_HEADER=(-H "Authorization: Bearer $TOKEN")
fi

TABLES=(transactions transaction_imports)

mkdir -p "$OUTPUT_DIR"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

echo "Exporting to: $OUTPUT_DIR"
echo "API:          $BASE_URL"
echo ""

for TABLE in "${TABLES[@]}"; do
    FILE="$OUTPUT_DIR/${TABLE}_${TIMESTAMP}.csv"
    echo -n "  Exporting $TABLE ... "
    HTTP_CODE=$(curl -s -o "$FILE" -w "%{http_code}" \
        "${AUTH_HEADER[@]}" \
        "$BASE_URL/api/export/$TABLE")
    if [[ "$HTTP_CODE" == "200" ]]; then
        LINES=$(wc -l < "$FILE")
        echo "OK ($((LINES - 1)) rows) → $FILE"
    else
        echo "FAILED (HTTP $HTTP_CODE)"
        rm -f "$FILE"
        exit 1
    fi
done

echo ""
echo "Done."
