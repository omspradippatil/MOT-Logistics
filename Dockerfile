# ============================================================
# Dockerfile - All-in-One MOT Logistics System
# Packages: Java 17 + MariaDB + Seeded Data + noVNC Web UI
# ============================================================
FROM debian:bookworm-slim

# Prevent interactive prompts during package installation
ENV DEBIAN_FRONTEND=noninteractive

# Install dependencies:
# - OpenJDK 17 JDK for compiling & running Java Swing
# - MariaDB server (MySQL drop-in replacement)
# - Xvfb (Virtual framebuffer for headless GUI)
# - Openbox (Lightweight window manager for borders & title bars)
# - x11vnc & websockify & novnc (Web browser VNC access)
# - TrueType fonts for clean Java Swing UI rendering
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-17-jdk \
    mariadb-server \
    mariadb-client \
    xvfb \
    openbox \
    x11vnc \
    novnc \
    websockify \
    fonts-dejavu-core \
    fonts-freefont-ttf \
    fonts-liberation \
    fontconfig \
    procps \
    curl \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Set up noVNC default landing page (auto-connect + auto-scale)
RUN if [ -d /usr/share/novnc ]; then \
        echo '<!DOCTYPE html><html><head><meta http-equiv="refresh" content="0; url=\x27vnc.html?autoconnect=true&resize=scale&title=MOT%20Logistics%20System\x27"><title>MOT Logistics</title></head><body>Redirecting to <a href="vnc.html?autoconnect=true&resize=scale&title=MOT%20Logistics%20System">MOT Logistics System</a>...</body></html>' > /usr/share/novnc/index.html; \
    fi

# Configure MariaDB for low-memory footprint and 0.0.0.0 binding (ideal for 512MB free tiers like Render)
RUN mkdir -p /etc/mysql/conf.d && \
    printf "[mysqld]\nbind-address = 0.0.0.0\nperformance_schema = off\ninnodb_buffer_pool_size = 16M\ninnodb_log_buffer_size = 2M\nkey_buffer_size = 8M\nmax_connections = 25\n" > /etc/mysql/conf.d/lean.cnf

# Set working directory
WORKDIR /app

# Copy project files into container
COPY . /app

# If lib/mysql-connector-j.jar is missing (e.g. fresh git clone), download it automatically
RUN if [ ! -f /app/lib/mysql-connector-j.jar ]; then \
        echo "Downloading MySQL Connector/J driver..." && \
        mkdir -p /app/lib && \
        curl -sSL "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar" -o /app/lib/mysql-connector-j.jar; \
    fi

# Pre-initialize MariaDB and bake the full database & seed data into the image
RUN mkdir -p /var/run/mysqld /var/lib/mysql && \
    chown -R mysql:mysql /var/run/mysqld /var/lib/mysql && \
    mariadb-install-db --user=mysql --ldata=/var/lib/mysql && \
    service mariadb start && \
    mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED VIA mysql_native_password USING PASSWORD('');" && \
    mysql -u root -e "CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED VIA mysql_native_password USING PASSWORD('');" && \
    mysql -u root -e "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;" && \
    mysql -u root -e "GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION;" && \
    mysql -u root -e "FLUSH PRIVILEGES;" && \
    mysql -u root < /app/database/schema.sql && \
    service mariadb stop

# Compile the Java application during build
RUN chmod +x /app/build.sh /app/run.sh /app/docker-entrypoint.sh && \
    /app/build.sh

# Expose ports:
# 6080: Web browser UI (noVNC)
# 3306: MySQL database
# 5900: Raw VNC protocol
EXPOSE 6080 3306 5900

# Mountable volumes for persistence
VOLUME ["/app/invoices", "/var/lib/mysql"]

# Set entrypoint
ENTRYPOINT ["/app/docker-entrypoint.sh"]
