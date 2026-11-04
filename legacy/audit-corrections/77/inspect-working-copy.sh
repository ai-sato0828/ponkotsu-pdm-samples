#!/bin/sh
set -eu

if [ "$#" -ne 1 ]; then
    printf '%s\n' 'usage: sh inspect-working-copy.sh WORKING_COPY' >&2
    exit 2
fi

cd -- "$1"
svn info .
svn status --no-ignore .
