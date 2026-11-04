#!/usr/bin/env bash
set -eu
base_url="${BASE_URL:-http://localhost:8080}"

# 文法エラー。閉じ波括弧がない
curl -si "$base_url/api/users" \
  -H 'Content-Type: application/json' \
  --data-binary '{"name":"Suzuki","email":"suzuki@example.com","notificationLimit":5'

# 型変換エラー。manyをIntegerにできない
curl -si "$base_url/api/users" \
  -H 'Content-Type: application/json' \
  --data-binary '{"name":"Suzuki","email":"suzuki@example.com","notificationLimit":"many"}'

# 本文なし。Content-TypeはJSON
curl -si "$base_url/api/users" \
  -H 'Content-Type: application/json' \
  --data-binary ''

# 制約違反。Integerには変換できるが、1以上ではない
curl -si "$base_url/api/users" \
  -H 'Content-Type: application/json' \
  --data-binary '{"name":"Suzuki","email":"suzuki@example.com","notificationLimit":0}'
