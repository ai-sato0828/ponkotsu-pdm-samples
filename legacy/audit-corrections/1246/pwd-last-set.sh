#!/bin/bash
set -eu

if (( $# != 1 )) || [[ ! $1 =~ ^[0-9]{1,19}$ ]]; then
    printf '%s\n' 'usage: bash pwd-last-set.sh FILETIME' >&2
    exit 2
fi

ticks=$1
if [[ ${#ticks} -eq 19 && $ticks > 9223372036854775807 ]]; then
    printf '%s\n' 'FILETIME is outside the signed 64-bit range' >&2
    exit 2
fi
if (( 10#$ticks == 0 )); then
    printf '%s\n' 'pwdLastSet is 0; handle it as an attribute state' >&2
    exit 2
fi

seconds=$((10#$ticks / 10000000 - 11644473600))
printf 'Unix seconds: %s\n' "$seconds"
if date --version >/dev/null 2>&1; then
    date -u --date "@$seconds" '+%Y-%m-%d %H:%M:%S UTC'
else
    date -u -r "$seconds" '+%Y-%m-%d %H:%M:%S UTC'
fi
