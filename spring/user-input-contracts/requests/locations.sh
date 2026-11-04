#!/usr/bin/env bash
set -eu
base_url="${BASE_URL:-http://localhost:8080}"

# パスの10が利用者ID
curl -si "$base_url/api/users/10"

# クエリのkeywordとpageが検索条件
curl -si "$base_url/api/users?keyword=tanaka&page=1"

# 本文にJSONを送る
curl -si "$base_url/api/users" \
  -H 'Content-Type: application/json' \
  --data-binary @requests/create-user.json

# 本文にURLエンコードしたフォームの値を送る
curl -si "$base_url/users" \
  --data-urlencode 'name=Sato' \
  --data-urlencode 'email=sato@example.com'
