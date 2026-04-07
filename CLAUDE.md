# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Spring Cloud microservices project for stock index trend trading and backtesting, upgraded to **JDK 17**, **Spring Boot 2.7.18**, **Spring Cloud 2022.0.4**, **Dubbo3**, and **Nacos**. Uses Maven multi-module structure with 8 modules.

## Build & Test Commands

```bash
# Build all modules
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Run tests for all modules
mvn test

# Run tests for a specific module
mvn test -pl index-codes-service

# Package all modules
mvn clean package -DskipTests
```

## Microservices Architecture (Dubbo3 + Nacos)

| Module | Purpose | Port | Dubbo Port |
|--------|---------|------|------------|
| trend-api | Dubbo RPC interfaces (API module) | - | - |
| index-codes-service | Index codes management (Dubbo Provider) | 8010 | 20881 |
| index-data-service | Index data retrieval (Dubbo Provider) | 8011 | 20882 |
| third-part-index-data-project | Third-party index data (Dubbo Provider) | 8013 | 20883 |
| index-gather-store-service | Data aggregation, Quartz sync, Redis cache | 8012 | 20884 |
| trend-trading-backtest-service | Trading backtesting (Dubbo Consumer) | 8051 | 20885 |
| trend-trading-backtest-view | Thymeleaf web UI | 8052 | - |
| index-zuul-service | API Gateway with routing | 8050 | - |

**Deprecated/Removed Modules:**
- `eureka-server` (replaced by Nacos Server)
- `index-config-server` (replaced by Nacos Config)
- `index-hystrix-dashboard` (replaced by Actuator endpoints)
- `index-turbine` (replaced by Actuator endpoints)

## Key Technical Components

- **Nacos Server**: Service registry and config center (`localhost:8848`)
- **Dubbo3 RPC**: Triple protocol for inter-service communication
- **Redis**: Used for caching (index-gather-store-service)
- **Quartz Scheduler**: `IndexDataSyncJob` for periodic index data synchronization
- **Resilience4j**: Circuit breaker pattern (replacing Hystrix)
- **Zipkin**: Distributed tracing (`localhost:9411`)
- **Spring Boot Actuator**: Monitoring and health checks

## Dubbo Service Groups

| Service | Group | Version |
|---------|-------|---------|
| index-codes-service | index-codes-group | 1.0.0 |
| index-data-service | index-data-group | 1.0.0 |
| third-part-index-data-project | third-part-index-data-group | 1.0.0 |

## Service Dependencies (Dubbo RPC)

```
trend-trading-backtest-service → Dubbo → index-data-service
index-gather-store-service → Dubbo → third-part-index-data-project
```

## API Gateway Routes (Zuul)

| Path | Service |
|------|---------|
| /api-codes/* | index-codes-service |
| /api-backtest/* | trend-trading-backtest-service |
| /api-view/* | trend-trading-backtest-view |

## Startup Order

1. **Nacos Server** - `docker run --name nacos -d -p 8848:8848 -p 9848:9848 nacos/nacos-server:v2.2.3`
2. **Redis** (if not running)
3. **third-part-index-data-project** (8083) - Dubbo provider
4. **index-gather-store-service** (8012) - Dubbo consumer + provider
5. **index-codes-service** (8010) - Dubbo provider
6. **index-data-service** (8011) - Dubbo provider
7. **trend-trading-backtest-service** (8051) - Dubbo consumer
8. **index-zuul-service** (8050) - API Gateway
9. **trend-trading-backtest-view** (8052) - Web UI

## Port Configuration

Most services support `port=` command-line argument. Example:
```bash
java -jar index-codes-service.jar --port=8010
```

## Actuator Endpoints

| Endpoint | Description |
|----------|-------------|
| /actuator/health | Service health status |
| /actuator/info | Application info |
| /actuator/metrics | Application metrics |
| /actuator/dubbo | Dubbo runtime info |
| /actuator/dubbo-provider | Provider statistics |
| /actuator/dubbo-consumer | Consumer statistics |

## Nacos Configuration

All services read configuration from Nacos Config Server with:
- Server: `localhost:8848`
- Namespace: `public`
- Group: `DEFAULT_GROUP`
- File extension: `yaml`

Configuration files in Nacos:
- `{application-name}.yaml` - Service-specific config
- `shared-data.yaml` - Shared configuration
