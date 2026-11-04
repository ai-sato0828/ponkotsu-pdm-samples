#!/bin/sh
set -eu

if [ "$#" -ne 2 ]; then
    printf '%s\n' 'usage: sh export-columns.sh INPUT OUTPUT' >&2
    exit 2
fi

input=$1
output=$2
set -C
awk -F, 'BEGIN { OFS = "," } { print $1, $2, $3, $4 }' < "$input" > "$output"
