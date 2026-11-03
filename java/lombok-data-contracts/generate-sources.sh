#!/bin/sh
set -eu
lombok_version=1.18.36
maven_repository="${MAVEN_REPO_LOCAL:-$HOME/.m2/repository}"
lombok_jar="$maven_repository/org/projectlombok/lombok/$lombok_version/lombok-$lombok_version.jar"
if [ ! -f "$lombok_jar" ]; then
  echo "Run mvn test first, or set MAVEN_REPO_LOCAL to your Maven cache." >&2
  exit 1
fi
java -jar "$lombok_jar" delombok -f pretty src/main/java -d generated
