#!/bin/bash
# ============================================================
# build.sh - Compile & Package Transport Management System
# ============================================================
set -e

echo "=============================================="
echo "  Building Transport Management System..."
echo "=============================================="

# Create output directory for compiled .class files
mkdir -p out

# Check if lib folder has required JARs
if [ ! -f "lib/mysql-connector-j.jar" ]; then
    echo ""
    echo "WARNING: lib/mysql-connector-j.jar not found!"
    echo "Download from: https://dev.mysql.com/downloads/connector/j/"
    echo ""
fi

# Compile all Java source files with UTF-8 encoding
echo "Compiling Java source files..."
find src -name "*.java" > sources.txt

javac -encoding UTF-8 \
      -cp "lib/*:src" \
      -d out \
      @sources.txt

rm -f sources.txt

# Package into a standalone JAR file for bulletproof deployment
echo "Packaging into app.jar..."
jar cfe app.jar com.transport.Main -C out .

echo ""
echo "✅ Build SUCCESSFUL! (Generated out/ and app.jar)"
echo ""
echo "To run the application:"
echo "  ./run.sh"
echo ""
