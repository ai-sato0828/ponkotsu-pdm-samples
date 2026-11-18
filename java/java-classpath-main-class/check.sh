#!/bin/sh
set -eu

sample_dir=$(CDPATH= cd "$(dirname "$0")" && pwd)
work_dir=$(mktemp -d)
trap 'rm -rf "$work_dir"' EXIT
unset CLASSPATH JDK_JAVA_OPTIONS JDK_JAVAC_OPTIONS JAVA_TOOL_OPTIONS _JAVA_OPTIONS

cp -R "$sample_dir/src" "$work_dir/src"
cd "$work_dir"
mkdir out empty-classpath

run_java() {
    java -Duser.language=en -Duser.country=US "$@" >stdout.txt 2>stderr.txt
}

check_missing_class() {
    case_name=$1
    classpath=$2
    main_class=$3
    status=0
    run_java -cp "$classpath" "$main_class" || status=$?
    if [ "$status" -ne 1 ] || [ -s stdout.txt ] ||
        ! grep -Fxq "Error: Could not find or load main class $main_class" stderr.txt ||
        ! grep -Fxq "Caused by: java.lang.ClassNotFoundException: $main_class" stderr.txt; then
        cat stdout.txt stderr.txt >&2
        printf 'FAIL %s (exit %s)\n' "$case_name" "$status" >&2
        exit 1
    fi
    printf 'PASS %s (exit %s)\n' "$case_name" "$status"
    cat stderr.txt
}

check_missing_class no-compiled-classes out example.orders.OrderTotal

javac -d out src/example/orders/OrderLine.java src/example/orders/OrderTotal.java
test -f out/example/orders/OrderLine.class
test -f out/example/orders/OrderTotal.class

check_missing_class simple-name out OrderTotal
check_missing_class wrong-classpath-root out/example/orders example.orders.OrderTotal

status=0
run_java -cp out/example/orders OrderTotal || status=$?
if [ "$status" -ne 1 ] || [ -s stdout.txt ] ||
    ! grep -Fxq 'Error: Could not find or load main class OrderTotal' stderr.txt ||
    ! grep -Fxq 'Caused by: java.lang.NoClassDefFoundError: example/orders/OrderTotal (wrong name: OrderTotal)' stderr.txt; then
    cat stdout.txt stderr.txt >&2
    printf 'FAIL class-file-with-different-name (exit %s)\n' "$status" >&2
    exit 1
fi
printf 'PASS class-file-with-different-name (exit %s)\n' "$status"
cat stderr.txt

run_java -cp out example.orders.OrderTotal
test "$(cat stdout.txt)" = "2750"
test ! -s stderr.txt
printf 'PASS qualified-name-and-root (exit 0)\n'
cat stdout.txt

status=0
run_java -cp empty-classpath src/example/orders/OrderTotal.java || status=$?
if [ "$status" -ne 1 ] || [ -s stdout.txt ] ||
    ! grep -Fq 'cannot find symbol' stderr.txt ||
    ! grep -Fq 'symbol:   variable OrderLine' stderr.txt; then
    cat stdout.txt stderr.txt >&2
    printf 'FAIL separate-source-file-on-java17 (exit %s)\n' "$status" >&2
    exit 1
fi
printf 'PASS separate-source-file-on-java17 (exit %s)\n' "$status"
cat stderr.txt
