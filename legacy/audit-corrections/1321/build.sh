#!/bin/sh
set -eu

mkdir -p build
printf '%s\n' 'build smoke check' > build/output.txt
test -s build/output.txt
