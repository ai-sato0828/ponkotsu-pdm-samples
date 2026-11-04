#!/bin/sh
set -eu

cd -- "$(dirname -- "$0")"
mkdir -p build
javac --release 17 -d build AbstractApi.java UserRequestService.java LocalUserApi.java SampleController.java
java -cp build SampleController
