#!/bin/bash
# ============================================================
# run.sh - Run the Transport Management System
# ============================================================
# HOW TO USE:
#   chmod +x run.sh
#   ./run.sh
# ============================================================

echo "=============================================="
echo "  Starting Transport Management System..."
echo "=============================================="

# Run the compiled application
# -cp "app.jar:out:lib/*" : classpath includes JAR bundle, compiled files and drivers
# com.transport.Main : the main class to run
java -cp "app.jar:out:lib/*" com.transport.Main

echo ""
echo "Application closed."
