#!/bin/bash
# ============================================================
# docker-entrypoint.sh - Container Startup for MOT Logistics
# ============================================================
set -e

# Graceful shutdown handler
cleanup() {
    echo ""
    echo "=============================================="
    echo "  Shutting down MOT Logistics System..."
    echo "=============================================="
    service mariadb stop 2>/dev/null || true
    kill $(jobs -p) 2>/dev/null || true
    exit 0
}
trap cleanup SIGINT SIGTERM

echo "=============================================="
echo "  Starting MOT Logistics Container..."
echo "=============================================="

# Ensure permissions on MySQL data and invoice directories
mkdir -p /var/lib/mysql /var/run/mysqld /app/invoices
chown -R mysql:mysql /var/lib/mysql /var/run/mysqld
chmod 777 /app/invoices

# Check if MariaDB needs initialization (e.g. empty mounted volume)
if [ ! -d "/var/lib/mysql/mysql" ]; then
    echo "[1/4] Initializing MariaDB system tables..."
    mariadb-install-db --user=mysql --ldata=/var/lib/mysql >/dev/null 2>&1
fi

# Configure MariaDB to listen on 0.0.0.0 if not already configured
if [ -f /etc/mysql/mariadb.conf.d/50-server.cnf ]; then
    sed -i 's/^bind-address\s*=.*/bind-address = 0.0.0.0/' /etc/mysql/mariadb.conf.d/50-server.cnf || true
fi

echo "[2/4] Starting MariaDB service..."
service mariadb start

# Wait for MariaDB to become ready
for i in {1..30}; do
    if mysqladmin ping -u root --silent 2>/dev/null; then
        break
    fi
    sleep 1
done

# Initialize transport_db and grant permissions if not present
if ! mysql -u root -e "USE transport_db;" 2>/dev/null; then
    echo "[3/4] Database 'transport_db' not detected. Importing schema.sql..."
    mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED VIA mysql_native_password USING PASSWORD('');" 2>/dev/null || true
    mysql -u root -e "CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED VIA mysql_native_password USING PASSWORD('');" 2>/dev/null || true
    mysql -u root -e "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;" 2>/dev/null || true
    mysql -u root -e "GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION;" 2>/dev/null || true
    mysql -u root -e "FLUSH PRIVILEGES;" 2>/dev/null || true
    mysql -u root < /app/database/schema.sql
    echo "      Database seeded successfully with all initial data."
else
    echo "[3/4] Database 'transport_db' is already initialized."
fi

# Configure display server
export DISPLAY="${DISPLAY:-:1}"
RESOLUTION="${RESOLUTION:-1366x768x24}"

if [ "$GUI_MODE" = "native" ]; then
    echo "[4/4] GUI Mode: Native X11 Forwarding (DISPLAY=$DISPLAY)"
else
    echo "[4/4] GUI Mode: Web noVNC (Display resolution: $RESOLUTION)"
    
    # Clean up stale X locks if any
    rm -f /tmp/.X1-lock /tmp/.X11-unix/X1 2>/dev/null || true

    # Start Xvfb (virtual framebuffer)
    Xvfb :1 -screen 0 "$RESOLUTION" -ac &
    sleep 1

    # Start Openbox window manager for proper window frames and resizing
    openbox &
    sleep 1

    # Start x11vnc server (port 5900)
    x11vnc -display :1 -nopw -listen 0.0.0.0 -xkb -forever -shared -bg >/dev/null 2>&1

    # Start websockify / noVNC web server (port 6080)
    websockify --web=/usr/share/novnc 6080 127.0.0.1:5900 >/dev/null 2>&1 &
fi

echo ""
echo "============================================================"
echo "  ✅ MOT Logistics System is ready to use!"
echo "============================================================"
echo "  🌐 Web Browser UI : http://localhost:6080"
echo "  🗄️ MySQL Database  : port 3306 (user: root, no password)"
echo "  🖥️ Native VNC Port : 5900"
echo "============================================================"
echo "  Default Logins:"
echo "    - Admin  : username=admin  | password=admin123"
echo "    - Driver : username=rahul  | password=rahul123"
echo "============================================================"
echo ""

cd /app

# Ensure classes are built
if [ ! -d "/app/out/com" ]; then
    echo "Compiling Java classes..."
    ./build.sh
fi

# Run the Java Swing Application in an auto-recovery loop
while true; do
    echo "Starting MOT Logistics Java Application..."
    java -cp "out:lib/*" com.transport.Main || true
    echo "Application exited. Relaunching in 2 seconds (stop the container to exit)..."
    sleep 2
done
