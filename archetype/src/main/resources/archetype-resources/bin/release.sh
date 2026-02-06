#!/bin/bash

# =============================================================================
# Release Script - Production Image Export
# =============================================================================
#
# IMPORTANT: This script must be run from the HOST machine, NOT inside the container
# Reason: Docker commands are not available inside the development container
#
# Usage: ./bin/release.sh
#
# What this script does:
# 1. Build Spring Boot application (Maven)
# 2. Build Svelte GUI (Vite)
# 3. Create optimized Docker image for production
# 4. Export image to tar file
# 5. Generate install.sh script for target server
# 6. Package everything into release.tar.gz
#
# Output: release.tar.gz containing:
#   - crm-image.tar (Docker image)
#   - install.sh (installation script for production server)
#
# Production Server Requirements:
# - Docker installed
# - Minimal resources: 512MB RAM, 1 CPU core (staging/beta-test mode)
# - Port 8080 available
#
# =============================================================================

set -e

# Color output functions
error() { printf '\033[0;31mERROR: %s\033[0m\n' "$1" >&2; exit 1; }
info() { printf '\033[0;34mINFO: %s\033[0m\n' "$1"; }
warn() { printf '\033[0;33mWARNING: %s\033[0m\n' "$1" >&2; }
success() { printf '\033[0;32m✓ SUCCESS: %s\033[0m\n' "$1"; }

# =============================================================================
# Configuration
# =============================================================================

WORKSPACE="/usr/src/app"
RELEASE_DIR="$WORKSPACE/release"
IMAGE_NAME="crm-app"
IMAGE_TAG="latest"
TAR_NAME="crm-image.tar"
RELEASE_PACKAGE="release.tar.gz"

# Production container resource limits (staging/beta-test mode)
# These are conservative limits for underprovisioned servers
MEMORY_LIMIT="512m"        # 512MB RAM
MEMORY_RESERVATION="256m"  # 256MB reserved
CPU_LIMIT="1.0"            # 1 CPU core
CPU_RESERVATION="0.5"      # 0.5 CPU cores reserved

# =============================================================================
# Pre-flight checks
# =============================================================================

info "Starting release build process..."

# Check if we're inside the container (we should NOT be)
if [ -f "/.dockerenv" ]; then
    error "This script must be run from the HOST machine, not inside the container. Exit the container first."
fi

# Check if docker is available
if ! command -v docker >/dev/null 2>&1; then
    error "Docker is not installed or not in PATH. Please install Docker first."
fi

# Check if we're in the project root
if [ ! -f "$WORKSPACE/pom.xml" ]; then
    error "pom.xml not found. Please run this script from the project root."
fi

# Load environment variables
if [ -f "$WORKSPACE/.env" ]; then
    info "Loading environment variables from .env..."
    set -a
    . "$WORKSPACE/.env"
    set +a
else
    warn ".env file not found. Using default values."
    PROJECT_NAME="crm"
    ARTIFACT_VERSION="1.0-SNAPSHOT"
fi

info "Project: $PROJECT_NAME"
info "Version: $ARTIFACT_VERSION"

# =============================================================================
# Clean and prepare release directory
# =============================================================================

info "Preparing release directory..."

if [ -d "$RELEASE_DIR" ]; then
    warn "Release directory exists. Cleaning..."
    rm -rf "$RELEASE_DIR"
fi

mkdir -p "$RELEASE_DIR"
success "Release directory prepared: $RELEASE_DIR"

# =============================================================================
# Step 1: Build Spring Boot Application
# =============================================================================

info "Step 1/6: Building Spring Boot application..."

# DEBUG: If this fails, check:
# - Maven is installed: mvn --version
# - pom.xml is valid: mvn validate
# - All dependencies are resolvable: mvn dependency:resolve
# - Java version matches pom.xml requirements

cd "$WORKSPACE"
docker exec crm-dev bash -c "cd /usr/src/app && bin/cmd app build" || {
    error "Failed to build Spring Boot application. Check Maven logs above."
}

JAR_FILE="$WORKSPACE/target/${PROJECT_NAME}-${ARTIFACT_VERSION}.jar"

if [ ! -f "$JAR_FILE" ]; then
    error "JAR file not found: $JAR_FILE. Build may have failed silently."
fi

success "Spring Boot application built: $JAR_FILE"

# =============================================================================
# Step 2: Build Svelte GUI
# =============================================================================

info "Step 2/6: Building Svelte GUI..."

# DEBUG: If this fails, check:
# - Node.js and npm are installed in container
# - gui/package.json exists and is valid
# - All npm dependencies are installed: cd gui && npm install
# - Vite config is correct: gui/vite.config.js

docker exec crm-dev bash -c "cd /usr/src/app && bin/cmd gui build" || {
    error "Failed to build Svelte GUI. Check npm/vite logs above."
}

STATIC_DIR="$WORKSPACE/src/main/resources/static"

if [ ! -d "$STATIC_DIR" ] || [ -z "$(ls -A $STATIC_DIR)" ]; then
    error "Static files not found in $STATIC_DIR. GUI build may have failed."
fi

success "Svelte GUI built: $STATIC_DIR"

# =============================================================================
# Step 3: Create Production Dockerfile
# =============================================================================

info "Step 3/6: Creating production Dockerfile..."

# DEBUG: This Dockerfile uses:
# - eclipse-temurin:21-jre-alpine for minimal image size
# - Non-root user for security
# - Health check endpoint
# If image build fails:
# - Check if base image is available: docker pull eclipse-temurin:21-jre-alpine
# - Verify JAR file path is correct
# - Check EXPOSE port matches application.properties

cat > "$RELEASE_DIR/Dockerfile" << 'DOCKERFILE_EOF'
# Production Dockerfile for CRM Application
# Uses minimal JRE image for smaller size and better security

FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser

# Copy application JAR
COPY app.jar /app/app.jar

# Create directories for data and logs
RUN mkdir -p /app/data /app/logs && \
    chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/status/health || exit 1

# Set JVM options for limited resources (staging/beta environment)
# -Xms256m: Initial heap size 256MB
# -Xmx512m: Maximum heap size 512MB (matches container memory limit)
# -XX:MaxMetaspaceSize=128m: Metaspace limit
# -XX:+UseSerialGC: Serial GC for low memory footprint
# These settings prioritize stability over performance for underprovisioned servers
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -Djava.security.egd=file:/dev/./urandom"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
DOCKERFILE_EOF

success "Production Dockerfile created"

# =============================================================================
# Step 4: Build Docker Image
# =============================================================================

info "Step 4/6: Building Docker image..."

# Copy JAR to release directory with standard name
cp "$JAR_FILE" "$RELEASE_DIR/app.jar"

# DEBUG: If docker build fails:
# - Check Dockerfile syntax: docker build --check $RELEASE_DIR
# - Verify base image is accessible
# - Check disk space: df -h
# - Try building with --no-cache flag
# - Check Docker daemon is running: docker info

cd "$RELEASE_DIR"
docker build -t "${IMAGE_NAME}:${IMAGE_TAG}" . || {
    error "Failed to build Docker image. Check Dockerfile and build logs above."
}

# Verify image was created
if ! docker images "${IMAGE_NAME}:${IMAGE_TAG}" | grep -q "${IMAGE_NAME}"; then
    error "Docker image not found after build. Build may have failed silently."
fi

success "Docker image built: ${IMAGE_NAME}:${IMAGE_TAG}"

# =============================================================================
# Step 5: Export Docker Image to TAR
# =============================================================================

info "Step 5/6: Exporting Docker image to tar..."

# DEBUG: If export fails:
# - Check disk space: df -h
# - Verify image exists: docker images
# - Check write permissions in release directory

docker save "${IMAGE_NAME}:${IMAGE_TAG}" -o "$RELEASE_DIR/$TAR_NAME" || {
    error "Failed to export Docker image. Check disk space and permissions."
}

if [ ! -f "$RELEASE_DIR/$TAR_NAME" ]; then
    error "TAR file not created: $RELEASE_DIR/$TAR_NAME"
fi

TAR_SIZE=$(du -h "$RELEASE_DIR/$TAR_NAME" | cut -f1)
success "Docker image exported: $TAR_NAME (Size: $TAR_SIZE)"

# =============================================================================
# Step 6: Generate Installation Script
# =============================================================================

info "Step 6/6: Generating installation script..."

# DEBUG: This script will be run on the production server
# If installation fails on production:
# - Check Docker is installed: docker --version
# - Check Docker service is running: systemctl status docker
# - Verify port 8080 is available: netstat -tuln | grep 8080
# - Check disk space on production: df -h
# - Verify tar file was transferred correctly: md5sum crm-image.tar

cat > "$RELEASE_DIR/install.sh" << 'INSTALL_EOF'
#!/bin/bash

# =============================================================================
# CRM Application - Production Installation Script
# =============================================================================
#
# This script installs the CRM application on the production server
#
# Requirements:
# - Docker installed and running
# - Port 8080 available
# - At least 1GB disk space
# - At least 512MB RAM available
#
# Usage: sudo ./install.sh
#
# =============================================================================

set -e

# Color output
error() { printf '\033[0;31mERROR: %s\033[0m\n' "$1" >&2; exit 1; }
info() { printf '\033[0;34mINFO: %s\033[0m\n' "$1"; }
success() { printf '\033[0;32m✓ SUCCESS: %s\033[0m\n' "$1"; }

# Configuration
CONTAINER_NAME="crm-production"
IMAGE_TAR="crm-image.tar"
DATA_DIR="/var/lib/crm/data"
LOGS_DIR="/var/lib/crm/logs"
APP_PORT="8080"

# Resource limits (staging/beta mode - works on underprovisioned servers)
MEMORY_LIMIT="512m"
MEMORY_RESERVATION="256m"
CPU_LIMIT="1.0"
CPU_RESERVATION="0.5"

info "CRM Application - Production Installation"
info "=========================================="

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    error "Please run as root (use sudo)"
fi

# Check Docker
if ! command -v docker >/dev/null 2>&1; then
    error "Docker is not installed. Please install Docker first."
fi

if ! docker info >/dev/null 2>&1; then
    error "Docker daemon is not running. Please start Docker service."
fi

# Check if image tar exists
if [ ! -f "$IMAGE_TAR" ]; then
    error "Image file not found: $IMAGE_TAR"
fi

info "Loading Docker image..."
docker load -i "$IMAGE_TAR" || error "Failed to load Docker image"
success "Docker image loaded"

# Create data directories
info "Creating data directories..."
mkdir -p "$DATA_DIR" "$LOGS_DIR"
chmod 755 "$DATA_DIR" "$LOGS_DIR"
success "Data directories created"

# Stop and remove existing container if exists
if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    info "Stopping existing container..."
    docker stop "$CONTAINER_NAME" 2>/dev/null || true
    docker rm "$CONTAINER_NAME" 2>/dev/null || true
    success "Existing container removed"
fi

# Start new container with resource limits
info "Starting CRM application container..."
info "  Memory limit: $MEMORY_LIMIT"
info "  CPU limit: $CPU_LIMIT"
info "  Port: $APP_PORT"

docker run -d \
    --name "$CONTAINER_NAME" \
    --restart unless-stopped \
    --memory="$MEMORY_LIMIT" \
    --memory-reservation="$MEMORY_RESERVATION" \
    --cpus="$CPU_LIMIT" \
    --cpu-shares=512 \
    -p "${APP_PORT}:8080" \
    -v "${DATA_DIR}:/app/data" \
    -v "${LOGS_DIR}:/app/logs" \
    -e SPRING_PROFILES_ACTIVE=production \
    crm-app:latest || error "Failed to start container"

success "Container started: $CONTAINER_NAME"

# Wait for application to be ready
info "Waiting for application to start (max 60 seconds)..."
COUNTER=0
while [ $COUNTER -lt 60 ]; do
    if docker exec "$CONTAINER_NAME" wget --no-verbose --tries=1 --spider http://localhost:8080/api/status/health 2>/dev/null; then
        success "Application is ready!"
        break
    fi
    sleep 2
    COUNTER=$((COUNTER + 2))
done

if [ $COUNTER -ge 60 ]; then
    error "Application failed to start within 60 seconds. Check logs: docker logs $CONTAINER_NAME"
fi

# Display status
echo ""
info "Installation completed successfully!"
echo ""
info "Application Details:"
info "  Container: $CONTAINER_NAME"
info "  Status: $(docker inspect -f '{{.State.Status}}' $CONTAINER_NAME)"
info "  URL: http://localhost:${APP_PORT}"
info "  Data: $DATA_DIR"
info "  Logs: $LOGS_DIR"
echo ""
info "Useful commands:"
info "  View logs:    docker logs -f $CONTAINER_NAME"
info "  Stop:         docker stop $CONTAINER_NAME"
info "  Start:        docker start $CONTAINER_NAME"
info "  Restart:      docker restart $CONTAINER_NAME"
info "  Shell access: docker exec -it $CONTAINER_NAME sh"
info "  Remove:       docker stop $CONTAINER_NAME && docker rm $CONTAINER_NAME"
echo ""
INSTALL_EOF

chmod +x "$RELEASE_DIR/install.sh"
success "Installation script generated: install.sh"

# =============================================================================
# Package Release
# =============================================================================

info "Packaging release..."

cd "$RELEASE_DIR"
tar -czf "$RELEASE_PACKAGE" "$TAR_NAME" install.sh || {
    error "Failed to create release package"
}

if [ ! -f "$RELEASE_DIR/$RELEASE_PACKAGE" ]; then
    error "Release package not created"
fi

PACKAGE_SIZE=$(du -h "$RELEASE_DIR/$RELEASE_PACKAGE" | cut -f1)
success "Release package created: $RELEASE_PACKAGE (Size: $PACKAGE_SIZE)"

# =============================================================================
# Cleanup temporary files
# =============================================================================

info "Cleaning up temporary files..."
rm -f "$RELEASE_DIR/app.jar"
rm -f "$RELEASE_DIR/Dockerfile"
rm -f "$RELEASE_DIR/$TAR_NAME"
rm -f "$RELEASE_DIR/install.sh"

# =============================================================================
# Summary
# =============================================================================

echo ""
success "=========================================="
success "Release build completed successfully!"
success "=========================================="
echo ""
info "Release package: $RELEASE_DIR/$RELEASE_PACKAGE"
info "Package size: $PACKAGE_SIZE"
echo ""
info "Next steps:"
info "1. Transfer release package to production server:"
info "   scp $RELEASE_DIR/$RELEASE_PACKAGE user@server:/tmp/"
echo ""
info "2. On production server, extract and install:"
info "   cd /tmp"
info "   tar -xzf $RELEASE_PACKAGE"
info "   sudo ./install.sh"
echo ""
info "Production container configuration:"
info "  Memory: $MEMORY_LIMIT (reserved: $MEMORY_RESERVATION)"
info "  CPU: $CPU_LIMIT cores (reserved: $CPU_RESERVATION)"
info "  Port: 8080"
info "  Mode: Staging/Beta-test (optimized for limited resources)"
echo ""
