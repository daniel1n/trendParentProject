# Stock Index Trend Trading Microservices Project

## 项目概述

这是一个基于 Spring Cloud Finchley.RELEASE (Spring Boot 2.0.3) 的微服务项目，用于**股票指数趋势交易与回测系统**。

## 技术栈

| 组件 | 版本 |
|------|------|
| Spring Boot | 2.0.3.RELEASE |
| Spring Cloud | Finchley.RELEASE |
| Java | 1.8 |
| Maven | 多模块结构 |
| Netflix Eureka | 服务注册中心 |
| Netflix Hystrix | 熔断器 (已弃用) |
| Netflix Zuul | API网关 (已弃用) |
| Spring Data Redis | 缓存 |
| Quartz Scheduler | 定时任务 |
| Spring Cloud Zipkin | 链路追踪 |

---

## 模块架构

### 1. eureka-server (端口: 8761)
**职责**: Netflix Eureka 服务注册中心，所有服务在此注册和发现

**关键依赖**:
- spring-cloud-starter-netflix-eureka-server

**配置**: 单机模式，`register-with-eureka: false`, `fetch-registry: false`

---

### 2. index-config-server (端口: 8060)
**职责**: Spring Cloud Config Server，Git-backed 配置管理

**关键依赖**:
- spring-cloud-config-server
- spring-cloud-starter-netflix-eureka-client

**Git配置**: `https://github.com/daniel1n/trendConfig/`, path: `respo`

---

### 3. index-zuul-service (端口: 8050)
**职责**: Netflix Zuul API网关，所有客户端请求的统一入口

**路由配置**:
```
/api-codes/*    → index-codes-service
/api-backtest/* → trend-trading-backtest-service
/api-view/*     → trend-trading-backtest-view
```

**关键依赖**:
- spring-cloud-starter-netflix-zuul
- spring-cloud-starter-netflix-eureka-client
- spring-cloud-starter-zipkin

---

### 4. index-codes-service (端口: 8011)
**职责**: 股票指数代码管理

**API端点**:
- `GET /codes` - 返回所有指数代码列表

**数据模型**: `Index { code, name }`

**通信方式**:
- 使用 RestTemplate 调用 `third-part-index-data-project` (硬编码 localhost:8090)
- 结果缓存在 Redis

**关键依赖**:
- spring-boot-starter-web
- spring-boot-starter-data-redis
- spring-cloud-starter-netflix-eureka-client

---

### 5. index-data-service (端口: 8021)
**职责**: 指数历史数据存储与检索 (OHLCV数据)

**API端点**:
- `GET /data/{code}` - 获取指定指数的数据

**数据模型**: `IndexData { date, closePoint }`

**通信方式**:
- 使用 Feign Client (带 Hystrix 降级) 调用 `index-gather-store-service`
- 从 Redis 获取数据

**关键依赖**:
- spring-boot-starter-web
- spring-boot-starter-data-redis
- spring-cloud-starter-netflix-eureka-client

---

### 6. third-part-index-data-project (端口: 8090)
**职责**: 模拟第三方数据提供商，提供静态JSON数据

**数据端点**:
- `/indexes/codes.json` - 指数代码列表
- `/indexes/{code}.json` - 指定指数的历史数据

**关键依赖**:
- spring-boot-starter-web
- spring-cloud-starter-netflix-eureka-client

---

### 7. index-gather-store-service (端口: 8001)
**职责**: 数据聚合服务，定时从第三方获取数据并存储到Redis

**API端点**:
- `GET /freshCodes` - 刷新指数代码
- `GET /getCodes` - 获取指数代码
- `GET /removeCodes` - 清除指数代码缓存
- `GET /freshIndexData/{code}` - 刷新指定指数数据
- `GET /getIndexData/{code}` - 获取指定指数数据
- `GET /removeIndexData/{code}` - 清除指定指数数据缓存

**定时任务**:
- `IndexDataSyncJob` 每1分钟执行一次，同步所有指数数据

**缓存策略**: 使用 `@Cacheable`, `@CacheEvict`, `@CachePut`

**关键依赖**:
- spring-boot-starter-web
- spring-boot-starter-data-redis
- spring-boot-starter-quartz
- spring-cloud-starter-netflix-eureka-client
- spring-cloud-starter-netflix-hystrix

---

### 8. trend-trading-backtest-service (端口: 8051)
**职责**: 核心回测引擎，模拟交易策略并计算收益

**API端点**:
```
GET /simulate/{code}/{ma}/{buyThreshold}/{sellThreshold}/{serviceCharge}/{startDate}/{endDate}
```
执行回测模拟，返回交易记录和收益

**功能特性**:
- 移动平均线计算
- 买卖阈值逻辑
- 交易记录跟踪
- 年化收益计算

**通信方式**:
- 使用 Feign Client (`IndexDataClient`) 调用 `index-data-service`
- 启用 Hystrix 熔断，降级时返回空数据

**关键依赖**:
- spring-boot-starter-web
- spring-cloud-starter-openfeign
- spring-cloud-starter-netflix-hystrix
- spring-cloud-starter-zipkin
- spring-boot-starter-actuator

---

### 9. trend-trading-backtest-view (端口: 8041)
**职责**: Thymeleaf Web UI，可视化回测结果

**页面路由**:
- `GET /` - 返回 Thymeleaf 视图 `view.html`

**通信方式**:
- 通过 Zuul 网关获取数据 (`/api-backtest/*`)

**动态配置**:
- `@RefreshScope` 支持运行时配置刷新
- 使用 RabbitMQ 接收配置刷新事件

**关键依赖**:
- spring-boot-starter-web
- spring-boot-starter-thymeleaf
- spring-cloud-starter-config
- spring-cloud-starter-bus-amqp
- spring-cloud-starter-netflix-eureka-client

---

### 10. index-hystrix-dashboard (端口: 8070)
**职责**: Hystrix 断路器监控仪表盘

**访问地址**: `http://localhost:8070/hystrix`

**监控端点**: 各服务的 `/actuator/hystrix.stream`

**关键依赖**:
- spring-cloud-starter-netflix-hystrix-dashboard
- spring-cloud-starter-netflix-hystrix
- spring-boot-starter-actuator

---

### 11. index-turbine (端口: 8080)
**职责**: 聚合多个服务实例的 Hystrix 指标

**访问地址**: `http://localhost:8080/turbine.stream`

**监控目标**: `trend-trading-backtest-service`

**关键依赖**:
- spring-cloud-starter-netflix-turbine
- spring-cloud-starter-netflix-hystrix-dashboard
- spring-boot-starter-actuator

---

## 服务通信架构

```
                            +------------------+
                            |   Zipkin (9411)  |
                            +--------+---------+
                                     |
+--------+    +------------+    +-----------+-----------+    +------------------------+
| Client |----| Zuul (8050)|--->| index-codes (8011)    |    | Eureka Server (8761)  |
+--------+    |            |--->| index-data (8021)      |<---|                        |
             |            |--->| backtest (8051)        |    | (所有服务注册于此)       |
             |            |--->| view (8041)             |    +------------------------+
             +------------+    +------------------------+
                                     |           |
                            +--------+           +--------+
                            | Redis  |           | RabbitMQ|
                            | (6379) |           | (5672) |
                            +--------+           +--------+
```

### 服务间通信方式

| 源服务 | 目标服务 | 通信方式 | 用途 |
|--------|----------|----------|------|
| index-codes-service | third-part-index-data-project | RestTemplate | 获取指数代码 |
| index-gather-store-service | third-part-index-data-project | RestTemplate | 获取指数数据 |
| index-gather-store-service | Redis | Spring Data Redis | 缓存数据 |
| trend-trading-backtest-service | index-data-service | Feign + Hystrix | 获取历史数据 |
| trend-trading-backtest-view | Zuul Gateway | HTTP | 获取回测结果 |
| 所有服务 | Eureka | Netflix Eureka Client | 服务注册与发现 |

---

## 数据流向

### 数据采集流程
```
1. Quartz (每1分钟) → IndexDataSyncJob.execute()
2. IndexService.fresh() → RestTemplate 调用 third-part-index-data:8090
3. IndexDataService.fresh(code) → RestTemplate 调用 /indexes/{code}.json
4. 使用 @CachePut 存储到 Redis
```

### 回测流程
```
1. 客户端 → Zuul Gateway (8050) → /api-backtest/*
2. Zuul → trend-trading-backtest-service (8051)
3. BackTestService → IndexDataClient (Feign)
4. IndexDataClient → index-data-service (8021) → Redis
5. 计算移动平均线，模拟买卖交易
6. 返回结果：交易记录、总收益、年化收益
```

---

## 监控配置

### Hystrix Dashboard (端口: 8070)
- 单服务断路器监控
- 访问: `http://localhost:8070/hystrix`
- 监控流: 各服务的 `/actuator/hystrix.stream`

### Turbine (端口: 8080)
- 聚合多个实例的 Hystrix 指标
- 访问: `http://localhost:8080/turbine.stream`
- 主要监控: `trend-trading-backtest-service`

### Zipkin (外部服务: 9411)
- 分布式链路追踪
- 所有服务配置: `spring.zipkin.base-url: http://localhost:9411`
- Brave sampler 设置为 ALWAYS_SAMPLE

---

## 配置管理

### 静态配置 (application.yml)
大部分服务使用硬编码的 `application.yml`:
- Eureka 注册地址: `http://localhost:8761/eureka/`
- Zipkin 端点: `http://localhost:9411`
- 服务特定配置

### Spring Cloud Config Server
- Git 后端配置: `https://github.com/daniel1n/trendConfig/`
- 被 `trend-trading-backtest-view` 使用

### 动态刷新
- `@RefreshScope` 注解支持运行时配置刷新
- RabbitMQ 总线传播刷新事件
- 端点: `POST /actuator/bus-refresh`

---

## 启动顺序

1. eureka-server (8761) - 服务注册中心
2. index-config-server (8888) - 配置中心 (可选)
3. index-codes-service (8010)
4. index-data-service (8011)
5. third-part-index-data-project (8013)
6. index-gather-store-service (8012)
7. trend-trading-backtest-service (8051)
8. trend-trading-backtest-view (8052)
9. index-zuul-service (8050)
10. index-hystrix-dashboard (8484)
11. index-turbine (8989)

---

## 已知问题

### 严重问题

| 问题 | 严重性 | 说明 |
|------|--------|------|
| **技术栈过时** | 高 | Finchley.RELEASE (2018) 已EOL，建议升级到2022.x |
| **Hystrix已弃用** | 高 | Netflix Hystrix 已停止维护，应迁移到 Resilience4j |
| **Zuul已弃用** | 高 | Zuul 1.x 已EOL，应迁移到 Spring Cloud Gateway |
| **硬编码服务URL** | 中 | `index-gather-store-service` 使用硬编码 `localhost:8090` |

### 技术债务

1. **紧耦合**: `index-gather-store-service` 直接调用 `localhost:8090` 而非使用服务发现
2. **无API版本控制**: 路由如 `/codes`, `/data/{code}` 无版本前缀
3. **无认证**: 所有服务开放，Zuul 无认证过滤器
4. **同步通信**: 大量使用 RestTemplate/Feign 阻塞调用
5. **单点Redis**: 无Redis集群配置，存在单点故障风险
6. **进程内Quartz**: Quartz调度器在服务内运行，可考虑外部化

### 依赖问题

| 依赖 | 问题 |
|------|------|
| spring-cloud-starter-netflix-hystrix | 已弃用 - Netflix Hystrix 2.x 维护模式 |
| spring-cloud-starter-netflix-zuul | 已弃用 - Zuul 1.x EOL |
| hutool-all 4.3.1 | 过时 (2019) - 当前版本 5.8.x |

---

## 迁移建议

**推荐迁移路径**:

| 当前 | 推荐替代 |
|------|----------|
| Netflix Hystrix | Resilience4j |
| Netflix Zuul | Spring Cloud Gateway |
| Netflix Eureka | Spring Cloud Loadbalancer + Consul/Nacos |
| Spring Boot 2.0.3 | Spring Boot 2.7.x 或 3.x |
| Spring Cloud Finchley | Spring Cloud 2022.x |

---

## 构建与运行

### 构建
```bash
mvn clean install
```

### 构建 (跳过测试)
```bash
mvn clean install -DskipTests
```

### 运行服务
```bash
# 使用 Java 8 运行
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home
$JAVA_HOME/bin/java -jar target/xxx-1.0-SNAPSHOT.jar
```

### 关键URL

| 服务 | URL |
|------|-----|
| Eureka Dashboard | http://localhost:8761 |
| Zuul Gateway | http://localhost:8050 |
| Hystrix Dashboard | http://localhost:8070/hystrix |
| Turbine | http://localhost:8080/turbine.stream |
| 回测视图 | http://localhost:8041 |
| Zipkin | http://localhost:9411 |
