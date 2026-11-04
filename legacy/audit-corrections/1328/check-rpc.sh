#!/bin/sh
set -eu

url=${1:-http://127.0.0.1:8545}
curl --fail --silent --show-error --connect-timeout 2 --max-time 5 \
    --header 'Content-Type: application/json' \
    --data '{"jsonrpc":"2.0","id":1,"method":"eth_chainId","params":[]}' \
    "$url"
