#!/bin/sh
set -eu

cd -- "$(dirname -- "$0")"
version=10.16.1.1
base=https://repo.maven.apache.org/maven2/org/apache/derby
deps=target/dependency
mkdir -p "$deps" target/classes

for artifact in derby derbyshared derbynet derbyclient derbytools; do
    jar=$deps/$artifact-$version.jar
    if [ ! -f "$jar" ]; then
        curl -fLsS --max-time 30 "$base/$artifact/$version/$artifact-$version.jar" \
            -o "$jar.tmp"
        mv "$jar.tmp" "$jar"
    fi
done

javac -cp "$deps/*" -d target/classes DerbyModesExample.java DerbyStartupCheck.java
java -cp "target/classes:$deps/*" DerbyModesExample
java -cp "target/classes:$deps/*" DerbyStartupCheck
