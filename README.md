# Stock Index Trend Trading Microservices Project

## 项目概述

这是一个基于 **Spring Cloud 2022.0.4** (Spring Boot 2.7.18) 和 **JDK 17** 的微服务项目，用于**股票指数趋势交易与回测系统**。

本项目已完成从 **Dubbo3 + Nacos** 架构的迁移，替换了原有的 Netflix 全家桶组件。

---

## 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 17 | JDK 版本 |
| Spring Boot | 2.7.18 | 应用框架 |
| Spring Cloud | 2022.0.4 | 微服务框架 |
| Apache Dubbo | 3.2.8 | RPC 框架 (Triple 协议) |
| Nacos | 2.2.3 | 服务注册与配置中心 |
| Resilience4j | 2.2.0 | 熔断器 (替代 Hystrix) |
| Spring Data Redis | - | 缓存 |
| Quartz Scheduler | - | 定时任务 |
| Spring Cloud Sleuth Zipkin | 3.1.9 | 链路追踪 |

---

## 模块架构

### 活跃模块 (8个)

| 模块 | 端口 | Dubbo端口 | 职责 |
|------|------|-----------|------|
| trend-api | - | - | Dubbo RPC 接口定义 |
| index-codes-service | 8010 | 20881 | 指数代码管理 (Dubbo Provider) |
| index-data-service | 8011 | 20882 | 指数数据检索 (Dubbo Provider) |
| third-part-index-data-project | 8013 | 20883 | 第三方数据提供 (Dubbo Provider) |
| index-gather-store-service | 8012 | 20884 | 数据聚合、Quartz同步、Redis缓存 |
| trend-trading-backtest-service | 8051 | 20885 | 交易回测引擎 (Dubbo Consumer) |
| trend-trading-backtest-view | 8052 | - | Thymeleaf Web UI |
| index-zuul-service | 8050 | - | API 网关 |

### 已移除模块 (5个)

| 原模块 | 替代方案 |
|--------|----------|
| eureka-server | Nacos Server |
| index-config-server | Nacos Config |
| index-hystrix-dashboard | Spring Boot Actuator |
| index-turbine | Spring Boot Actuator |

---

## 技术架构图

```
                           +-------------------+
                           |   Nacos Server   |
                           |   (8848/9848)    |
                           +--------+----------+
                                    |
        +------------+    +----------+----------+    +---------------------+
        |   Client   |--->|   Zuul Gateway   |--->|  index-codes-service |
        +------------+    |     (8050)        |--->|  index-data-service |
                         |                    |--->|  backtest-service    |
                         +--------------------+    +---------------------+
                                  |
                         +--------+--------+
                         | Dubbo RPC (Tri)  |
                         | 20881-20885     |
                         +-----------------+
```

---

## 服务依赖关系

```
trend-trading-backtest-service
  └── Dubbo Consumer → index-data-service (index-data-group)

index-gather-store-service
  └── Dubbo Consumer → third-part-index-data-project (third-part-index-data-group)
```

---

## Dubbo 服务配置

### 服务分组

| 服务 | Group | Version | 协议 | 端口 |
|------|-------|---------|------|------|
| index-codes-service | index-codes-group | 1.0.0 | tri | 20881 |
| index-data-service | index-data-group | 1.0.0 | tri | 20882 |
| third-part-index-data-project | third-part-index-data-group | 1.0.0 | tri | 20883 |

### 通用配置

```yaml
dubbo:
  application:
    name: ${spring.application.name}
    qos-enable: false
  protocol:
    name: tri
    threads: 200
    iothreads: 4
  registry:
    address: nacos://localhost:8848
    group: DUBBO_DEFAULT_GROUP
  provider:
    timeout: 30000
    retries: 0
  consumer:
    timeout: 30000
    retries: 0
    check: false
```

---

## API 网关路由

| 路径 | 目标服务 |
|------|----------|
| /api-codes/* | index-codes-service |
| /api-backtest/* | trend-trading-backtest-service |
| /api-view/* | trend-trading-backtest-view |

---

## Nacos 配置

所有服务通过 `bootstrap.yml` 连接 Nacos Config Server:

```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        file-extension: yaml
        namespace: public
        group: DEFAULT_GROUP
      discovery:
        server-addr: localhost:8848
```

---

## 监控端点

| 端点 | 描述 |
|------|------|
| /actuator/health | 健康检查 |
| /actuator/info | 应用信息 |
| /actuator/metrics | 指标监控 |
| /actuator/dubbo | Dubbo 运行时信息 |
| /actuator/dubbo-provider | Provider 统计 |
| /actuator/dubbo-consumer | Consumer 统计 |

---

## 启动顺序

### 前置条件

1. **启动 Nacos Server**
   ```bash
   docker run --name nacos -d -p 8848:8848 -p 9848:9848 nacos/nacos-server:v2.2.3
   ```

2. **启动 Redis** (用于 index-gather-store-service 缓存)

### 服务启动顺序

1. third-part-index-data-project (8013) - Dubbo Provider
2. index-gather-store-service (8012) - Dubbo Consumer + Provider
3. index-codes-service (8010) - Dubbo Provider
4. index-data-service (8011) - Dubbo Provider
5. trend-trading-backtest-service (8051) - Dubbo Consumer
6. index-zuul-service (8050) - API Gateway
7. trend-trading-backtest-view (8052) - Web UI

### 快速启动脚本

```bash
# 构建所有模块
mvn clean package -DskipTests

# 执行启动脚本
./scripts/startup.sh
```

---

## 构建与运行

### 构建

```bash
# 构建所有模块
mvn clean install

# 跳过测试构建
mvn clean install -DskipTests

# 打包
mvn clean package -DskipTests
```

### 运行

```bash
# 方式1: 使用启动脚本
./scripts/startup.sh

# 方式2: 手动启动
java -jar index-codes-service/target/index-codes-service-1.0-SNAPSHOT.jar --port=8010
java -jar index-data-service/target/index-data-service-1.0-SNAPSHOT.jar --port=8011
# ...
```

### 测试

```bash
# 运行所有测试
mvn test

# 运行单个模块测试
mvn test -pl index-codes-service
```

---

## 关键 URL

| 服务 | URL |
|------|-----|
| Nacos Console | http://localhost:8848/nacos |
| Zuul Gateway | http://localhost:8050 |
| 回测视图 | http://localhost:8052 |
| 服务健康检查 | http://localhost:8051/actuator/health |
| Zipkin | http://localhost:9411 |

---

## 迁移历史

| 阶段 | 内容 |
|------|------|
| Phase 1 | 创建 trend-api，迁移到 Dubbo3 + Nacos |
| Phase 2 | 迁移 zuul 和 view 到 Nacos discovery |
| Phase 3 | 移除已弃用模块 (Eureka, Hystrix, Turbine, Config Server) |
| Phase 4 | Dubbo RPC 优化 (版本、分组、超时配置) |
| Phase 5 | 添加可观测性 (Actuator 端点) |
| Phase 6 | 移除 Eureka/Feign 依赖 |
| Phase 7 | 添加 bootstrap.yml 支持 Nacos Config |
| Phase 8 | 更新文档和启动脚本 |

---

## 原始架构 (已废弃)

原项目使用 **Netflix Eureka + Hystrix + Zuul + Spring Cloud Config** 架构，已全部迁移至 **Nacos + Dubbo3 + Resilience4j**。

---

## 许可证

MIT License
