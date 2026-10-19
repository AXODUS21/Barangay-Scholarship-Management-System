#!/usr/bin/env bash
# ==============================================================================
# Barangay Scholarship & Educational Assistance Management System
# All-in-One Compilation & Execution Launcher
# ==============================================================================
set -e

# Terminal Colors
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${CYAN}===================================================================${NC}"
echo -e "${CYAN}   BARANGAY SCHOLARSHIP & EDUCATIONAL ASSISTANCE MANAGEMENT SYSTEM ${NC}"
echo -e "${CYAN}===================================================================${NC}"

# 1. Locate Java Development Kit (JDK)
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/javac" ]; then
    JAVAC="$JAVA_HOME/bin/javac"
    JAVA="$JAVA_HOME/bin/java"
elif [ -x "/Library/Java/JavaVirtualMachines/jdk-26.jdk/Contents/Home/bin/javac" ]; then
    export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-26.jdk/Contents/Home"
    JAVAC="$JAVA_HOME/bin/javac"
    JAVA="$JAVA_HOME/bin/java"
elif command -v javac >/dev/null 2>&1 && command -v java >/dev/null 2>&1; then
    JAVAC="javac"
    JAVA="java"
else
    echo -e "${RED}[ERROR] No Java compiler (javac) found on your system!${NC}"
    echo "Please ensure Java JDK 17+ is installed and configured in PATH or JAVA_HOME."
    exit 1
fi

echo -e "${GREEN}[INFO] Using Java Compiler:${NC} $JAVAC"
"$JAVA" -version 2>&1 | head -n 1

# 2. Prepare output directory
mkdir -p bin

# 3. Compile all Java source files
echo -e "\n${YELLOW}[1/2] Compiling all source code from src/...${NC}"
JAVA_FILES=$(find src -name "*.java")

if [ -z "$JAVA_FILES" ]; then
    echo -e "${RED}[ERROR] No .java source files found in src/!${NC}"
    exit 1
fi

"$JAVAC" -d bin -sourcepath src $JAVA_FILES
echo -e "${GREEN}[✔] Compilation successful! Classes compiled into bin/${NC}"

# 4. Launch Application GUI
echo -e "\n${YELLOW}[2/2] Launching Graphical User Interface (GUI)...${NC}"
echo -e "${CYAN}-------------------------------------------------------------------${NC}"

exec "$JAVA" -cp bin com.barangay.scholarship.Main
