#!/usr/bin/env bash
set -e

# Detect Java
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/javac" ]; then
    JAVAC="$JAVA_HOME/bin/javac"
    JAVA="$JAVA_HOME/bin/java"
elif [ -x "/Library/Java/JavaVirtualMachines/jdk-26.jdk/Contents/Home/bin/javac" ]; then
    export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-26.jdk/Contents/Home"
    JAVAC="$JAVA_HOME/bin/javac"
    JAVA="$JAVA_HOME/bin/java"
elif command -v javac >/dev/null 2>&1; then
    JAVAC="javac"
    JAVA="java"
else
    echo "Error: No Java compiler found!"
    exit 1
fi

echo "Using Java compiler: $JAVAC"
"$JAVA" -version

mkdir -p bin

echo "Compiling all Java source files..."
# Find all .java files in src/
JAVA_FILES=$(find src -name "*.java")

if [ -z "$JAVA_FILES" ]; then
    echo "No Java files found to compile."
    exit 0
fi

"$JAVAC" -encoding UTF-8 -d bin -sourcepath src $JAVA_FILES
echo "Build succeeded! Classes compiled into bin/"

if [ "$1" = "test" ]; then
    echo "Running automated test suite..."
    "$JAVA" -cp bin com.barangay.scholarship.test.TestRunner
elif [ "$1" = "run" ]; then
    echo "Launching Barangay Scholarship Management System GUI..."
    "$JAVA" -cp bin com.barangay.scholarship.Main
fi
