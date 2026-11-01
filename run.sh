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
# -cp "out:lib/*" : classpath includes compiled files and JARs
# com.transport.Main : the main class to run
java -cp "out:lib/*" com.transport.Main

echo ""
echo "Application closed."
