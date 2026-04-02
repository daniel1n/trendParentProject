# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Spring Cloud (Finchley.RELEASE / Spring Boot 2.0.3) microservices project for stock index trend trading and backtesting. Uses Maven multi-module structure with 11 modules.

## Build & Test Commands

```bash
# Build all modules
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Run tests for a specific module (from module directory)
mvn test

# Run a single test class
mvn test -Dtest=AppTest
```

## Microservices Architecture

| Module | Purpose | Port |
|--------|---------|------|
| eureka-server | Service registry (Netflix Eureka) | 8761 |
| index-config-server | Centralized config (Spring Cloud Config, Git-backed) | 8888 |
| index-zuul-service | API Gateway with routing | 8050 |
| index-codes-service | Index codes management | 8010 |
| index-data-service | Index data retrieval | 8011 |
| index-gather-store-service | Data aggregation, scheduled sync with Quartz, Redis caching | 8012 |
| third-part-index-data-project | Mock third-party index data provider (static JSON) | 8013 |
| trend-trading-backtest-service | Trading backtesting with Feign + Hystrix | 8051 |
| trend-trading-backtest-view | Thymeleaf web UI for visualizations | 8052 |
| index-hystrix-dashboard | Hystrix circuit breaker monitoring | 8484 |
| index-turbine | Turbine for aggregating Hystrix metrics | 8989 |

## Key Technical Components

- **Eureka**: All services register with `http://localhost:8761/eureka/`
- **Redis**: Used by index-gather-store-service for caching (configured in `RedisCacheConfig`)
- **Quartz Scheduler**: `IndexDataSyncJob` runs periodic index data synchronization
- **Feign Clients**: trend-trading-backtest-service uses `@EnableFeignClients` to call other services
- **Hystrix Circuit Breaker**: Enabled with `@EnableCircuitBreaker`
- **Zipkin**: Distributed tracing at `http://localhost:9411`

## Service Dependencies

```
Zuul Gateway (8050) → Routes:
  /api-codes/* → index-codes-service (8010)
  /api-backtest/* → trend-trading-backtest-service (8051)
  /api-view/* → trend-trading-backtest-view (8052)

index-gather-store-service (8012) → Uses Feign to call third-part-index-data-project (8013)

trend-trading-backtest-service (8051) → Feign clients to:
  - index-codes-service
  - index-data-service
  - index-gather-store-service
```

## Startup Order

1. eureka-server (8761)
2. index-config-server (8888) - optional, if using external config
3. index-codes-service (8010)
4. index-data-service (8011)
5. third-part-index-data-project (8013)
6. index-gather-store-service (8012)
7. trend-trading-backtest-service (8051)
8. trend-trading-backtest-view (8052)
9. index-zuul-service (8050)
10. index-hystrix-dashboard (8484) + index-turbine (8989) - for monitoring

## Port Configuration

Most services support `port=` command-line argument. The trend-trading-backtest-service prompts for port if not specified, defaulting to 8051 after 5 seconds.
