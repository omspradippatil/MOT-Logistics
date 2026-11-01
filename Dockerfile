# ============================================================
# Dockerfile - All-in-One MOT Logistics System
# Packages: Java 17 + MariaDB + Seeded Data + noVNC Web UI
# ============================================================
FROM debian:bookworm-slim

# Prevent interactive prompts and force UTF-8 encoding across JVM and shell
ENV DEBIAN_FRONTEND=noninteractive \
    LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"

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

# Configure Openbox defaults and MariaDB for low-memory footprint (ideal for 512MB free tiers like Render)
RUN mkdir -p /root/.config/openbox && touch /root/.config/openbox/rc.xml && \
    mkdir -p /etc/mysql/conf.d && \
    printf "[mysqld]\nbind-address = 0.0.0.0\nperformance_schema = off\ninnodb_buffer_pool_size = 16M\ninnodb_log_buffer_size = 2M\nkey_buffer_size = 8M\nmax_connections = 25\n" > /etc/mysql/conf.d/lean.cnf

# Set working directory
WORKDIR /app

# Copy project files into container
COPY . /app

# Install noVNC index landing page
RUN cp /app/novnc-index.html /usr/share/novnc/index.html && \
    ln -sf /usr/share/novnc/vnc.html /usr/share/novnc/default.html

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
