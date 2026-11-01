#!/bin/bash
# ============================================================
# build.sh - Compile the Transport Management System
# ============================================================
# HOW TO USE:
#   chmod +x build.sh
#   ./build.sh
# ============================================================

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

# Compile all Java files
# -cp lib/*  : include all JARs in lib folder
# -d out     : output compiled files to out/ folder
# -sourcepath: find source files starting from src/
echo "Compiling Java source files..."

javac -cp "lib/*" \
      -d out \
      -sourcepath src \
      src/com/transport/Main.java \
      2>&1

# Check if compilation succeeded
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build SUCCESSFUL!"
    echo ""
    echo "To run the application:"
    echo "  ./run.sh"
    echo ""
else
    echo ""
    echo "❌ Build FAILED! Fix the errors above and try again."
    echo ""
fi
