#!/bin/sh
set -eu

jar=$(cd "$(dirname "$1")" && pwd)/$(basename "$1")
temp=$(mktemp -d)
trap 'rm -rf "$temp"' 0
cd "$temp"

probe() {
    java -jar "$jar" --spring.main.web-application-type=none \
        --spring.main.banner-mode=off --logging.level.root=OFF --demo.print-config=true "$@"
}

expect() {
    label=$1
    milliseconds=$2
    marker=$3
    shift 3
    output=$(probe "$@")
    expected=$(printf 'timeout.ms=%s\njar.marker=%s' "$milliseconds" "$marker")
    [ "$output" = "$expected" ] || {
        printf '%s: expected %s; got %s\n' "$label" "$expected" "$output" >&2
        exit 1
    }
    printf '%s: %s ms (%s)\n' "$label" "$milliseconds" "$marker"
}

unset DEMO_TIMEOUT SPRING_CONFIG_LOCATION SPRING_CONFIG_ADDITIONAL_LOCATION SPRING_PROFILES_ACTIVE
expect jar-default 1000 inside-jar
expect jar-local-profile 2000 inside-jar --spring.profiles.active=local
mkdir config replacement
printf 'demo.timeout=3s\n' > config/application.properties
expect outside-jar 3000 inside-jar
(export DEMO_TIMEOUT=4s; expect environment 4000 inside-jar)
(export DEMO_TIMEOUT=4s; expect command-line 5000 inside-jar --demo.timeout=5s)
printf 'demo.timeout=6s\n' > replacement/application.properties
expect additional-location 6000 inside-jar --spring.config.additional-location=file:./replacement/
expect replacement-location 6000 absent --spring.config.location=file:./replacement/
