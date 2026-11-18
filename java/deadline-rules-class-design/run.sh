#!/bin/sh
set -eu

cd -- "$(dirname -- "$0")"
classes=$(mktemp -d "${TMPDIR:-/tmp}/deadline-demo.XXXXXX")
trap 'rm -rf "$classes"' 0
javac --release 17 -encoding UTF-8 -d "$classes" src/main/java/example/deadline/*.java
java -cp "$classes" example.deadline.DeadlineDemo
