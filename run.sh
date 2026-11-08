#!/bin/bash
# ===================================================================
# Launch Script for Vehicle Rental System
# ===================================================================

# Set Java 21 if installed via Homebrew or standard path
if [ -d "/opt/homebrew/opt/openjdk@21/bin" ]; then
    export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"
elif [ -d "/usr/local/opt/openjdk@21/bin" ]; then
    export PATH="/usr/local/opt/openjdk@21/bin:$PATH"
fi

echo "Checking Java version..."
java -version 2>&1
if [ $? -ne 0 ]; then
    echo "ERROR: Java 21 is not found. Please install Java 21 (e.g., 'brew install openjdk@21')."
    exit 1
fi

# Check if Maven is available
if command -v mvn >/dev/null 2>&1; then
    echo "Running with Maven..."
    mvn clean compile exec:java
    exit 0
fi

# Fallback: Compile with javac and run
echo "Maven not found. Compiling with javac..."
mkdir -p bin

# Find all java files
find src/main/java -name "*.java" > sources.txt

# Classpath with MySQL connector if present in lib/
CP="bin:src/main/resources"
if ls lib/mysql-connector-*.jar 1> /dev/null 2>&1; then
    for jar in lib/*.jar; do
        CP="$CP:$jar"
    done
fi

javac -cp "$CP" -d bin @sources.txt
rm sources.txt

echo "Starting Vehicle Rental System..."
java -cp "$CP" com.vehiclerental.Main
