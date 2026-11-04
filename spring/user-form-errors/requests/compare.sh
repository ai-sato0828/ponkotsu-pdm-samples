#!/usr/bin/env bash
set -eu
base_url="${BASE_URL:-http://localhost:8080}"

# 正しい順番はHTTP 200。利用枠0のエラーを画面に返す
curl -si "$base_url/users" \
  --data-urlencode 'name=Sato' \
  --data-urlencode 'department=OPS' \
  --data-urlencode 'seatLimit=0'

# 誤った順番はHTTP 400。Controllerの処理に入る前に失敗する
curl -si "$base_url/demo/users/wrong-order" \
  --data-urlencode 'name=Sato' \
  --data-urlencode 'department=OPS' \
  --data-urlencode 'seatLimit=0'
