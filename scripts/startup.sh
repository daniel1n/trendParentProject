#!/bin/bash

# Dubbo3 + Nacos Migration - Service Startup Script
# Prerequisites:
# 1. Nacos Server running on localhost:8848
# 2. Redis running on localhost:6379

echo "=========================================="
echo "Dubbo3 + Nacos Service Startup Script"
echo "=========================================="

# Base directory
BASE_DIR=$(cd "$(dirname "$0")/.." && pwd)
MODULES=(
    "third-part-index-data-project"
    "index-gather-store-service"
    "index-codes-service"
    "index-data-service"
    "trend-trading-backtest-service"
    "index-zuul-service"
    "trend-trading-backtest-view"
)

# Check if Nacos is running
check_nacos() {
    if curl -s http://localhost:8848/nacos/v1/console/health > /dev/null 2>&1; then
        echo "[OK] Nacos Server is running on localhost:8848"
        return 0
    else
        echo "[WARN] Nacos Server is not running on localhost:8848"
        echo "       Please start Nacos: docker run --name nacos -d -p 8848:8848 -p 9848:9848 nacos/nacos-server:v2.2.3"
        return 1
    fi
}

# Check if Redis is running
check_redis() {
    if nc -z localhost 6379 2>/dev/null; then
        echo "[OK] Redis is running on localhost:6379"
        return 0
    else
        echo "[WARN] Redis is not running on localhost:6379"
        echo "       Please start Redis before running index-gather-store-service"
        return 1
    fi
}

# Start a service
start_service() {
    local module=$1
    local jar="${module}/target/${module}-1.0-SNAPSHOT.jar"

    if [ ! -f "$BASE_DIR/$jar" ]; then
        echo "[SKIP] $jar not found. Run 'mvn clean package' first."
        return 1
    fi

    echo "[START] Starting $module..."
    cd "$BASE_DIR/$module"
    nohup java -jar "$jar" > "$BASE_DIR/logs/${module}.log" 2>&1 &
    echo "[PID] $module started with PID $!"
}

# Main
echo ""
check_nacos
check_redis

echo ""
echo "Starting services..."

# Create logs directory
mkdir -p "$BASE_DIR/logs"

# Start each service
for module in "${MODULES[@]}"; do
    start_service "$module"
    sleep 2
done

echo ""
echo "=========================================="
echo "Services started. Check logs in ./logs/"
echo "=========================================="
echo ""
echo "Service Endpoints:"
echo "  - Nacos Console:     http://localhost:8848/nacos"
echo "  - Zuul Gateway:     http://localhost:8050/api-codes/*"
echo "  - Backtest View:    http://localhost:8052"
echo "  - Actuator Health:  http://localhost:8051/actuator/health"
echo ""
