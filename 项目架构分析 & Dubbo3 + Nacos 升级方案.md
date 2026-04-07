项目架构分析 & Dubbo3 + Nacos 升级方案

    一、当前架构分析

    1.1 技术栈现状

    ┌──────────────┬─────────────────────────────┬──────────────────────┐
    │     组件     │          当前版本           │         状态         │
    ├──────────────┼─────────────────────────────┼──────────────────────┤
    │ JDK          │ 1.8 → 17                    │ 已升级               │
    ├──────────────┼─────────────────────────────┼──────────────────────┤
    │ Spring Boot  │ 2.0.3 → 2.7.18              │ 已升级               │
    ├──────────────┼─────────────────────────────┼──────────────────────┤
    │ Spring Cloud │ Finchley.RELEASE → 2022.0.4 │ 已升级               │
    ├──────────────┼─────────────────────────────┼──────────────────────┤
    │ 服务注册     │ Netflix Eureka              │ 待升级               │
    ├──────────────┼─────────────────────────────┼──────────────────────┤
    │ RPC框架      │ Feign + Hystrix             │ 待升级               │
    ├──────────────┼─────────────────────────────┼──────────────────────┤
    │ 熔断器       │ Hystrix                     │ 已迁移至Resilience4j │
    ├──────────────┼─────────────────────────────┼──────────────────────┤
    │ 网关         │ Netflix Zuul                │ 待升级               │
    └──────────────┴─────────────────────────────┴──────────────────────┘

    1.2 当前微服务架构

    ┌─────────────────────────────────────────────────────────────────┐
    │                        Eureka Server                            │
    │                          (8761)                                  │
    └─────────────────────────────────────────────────────────────────┘
                                    │
            ┌───────────────────────┼───────────────────────┐
            │                       │                       │
            ▼                       ▼                       ▼
    ┌───────────────┐     ┌───────────────┐     ┌───────────────┐
    │ index-codes   │     │ index-data    │     │ third-part    │
    │ -service      │     │ -service      │     │ -index-data   │
    │ (8010)        │     │ (8011)        │     │ (8013)        │
    └───────────────┘     └───────────────┘     └───────────────┘
            │                       │                       │
            └───────────────────────┼───────────────────────┘
                                    │
                                    ▼
                        ┌───────────────────────┐
                        │ index-gather-store     │
                        │ -service (8012)       │
                        │ Feign + Resilience4j   │
                        └───────────────────────┘
                                    │
                                    ▼
    ┌─────────────────────────────────────────────────────────────────┐
    │                    trend-trading-backtest-service (8051)        │
    │                    Feign + Resilience4j                          │
    └─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
    ┌─────────────────────────────────────────────────────────────────┐
    │                      Zuul Gateway (8050)                         │
    │              /api-codes/* → index-codes-service                 │
    │              /api-backtest/* → backtest-service                 │
    └─────────────────────────────────────────────────────────────────┘

    1.3 当前架构问题

    1. Eureka 2.x 停止维护：Eureka 2.x 已不再发布，Netflix 仅维护 1.x
    2. Feign + Hystrix 耦合：Hystrix 已进入维护模式，Resilience4j 是官方推荐
    3. Zuul 性能瓶颈：Zuul 1.x 基于阻塞 I/O，Spring Cloud Gateway 是未来方向
    4. 缺乏统一配置中心：当前无分布式配置管理
    5. 服务间通信效率低：REST HTTP 通信 overhead 较大

    ---
    二、目标架构设计

    2.1 目标技术栈

    ┌────────────────┬──────────────────────┬──────────────────────────┐
    │      组件      │       目标版本       │           说明           │
    ├────────────────┼──────────────────────┼──────────────────────────┤
    │ Spring Cloud   │ 2022.0.4             │ 当前稳定版               │
    ├────────────────┼──────────────────────┼──────────────────────────┤
    │ 服务注册与发现 │ Nacos 2.2.x          │ 替代 Eureka              │
    ├────────────────┼──────────────────────┼──────────────────────────┤
    │ RPC 框架       │ Dubbo 3.2.x          │ Triple 协议，高性能      │
    ├────────────────┼──────────────────────┼──────────────────────────┤
    │ 配置中心       │ Nacos Config         │ 替代 Spring Cloud Config │
    ├────────────────┼──────────────────────┼──────────────────────────┤
    │ 网关           │ Spring Cloud Gateway │ 响应式，非阻塞           │
    ├────────────────┼──────────────────────┼──────────────────────────┤
    │ 熔断器         │ Resilience4j 2.2.x   │ 官方推荐                 │
    └────────────────┴──────────────────────┴──────────────────────────┘

    2.2 目标架构图

    ┌─────────────────────────────────────────────────────────────────┐
    │                     Nacos Server (8848)                         │
    │              服务注册 + 配置中心 + 分布式锁                       │
    └─────────────────────────────────────────────────────────────────┘
                                    │
            ┌───────────────────────┼───────────────────────┐
            │                       │                       │
            ▼                       ▼                       ▼
    ┌───────────────┐     ┌───────────────┐     ┌───────────────┐
    │ index-codes   │     │ index-data    │     │ third-part    │
    │ -service      │     │ -service      │     │ -index-data   │
    │ (8010)        │     │ (8011)        │     │ (8013)        │
    │ @DubboService │     │ @DubboService │     │ @DubboService │
    └───────────────┘     └───────────────┘     └───────────────┘
            │                       │                       │
            └───────────────────────┼───────────────────────┘
                                    │ Dubbo RPC (Triple)
                                    ▼
                        ┌───────────────────────┐
                        │ index-gather-store    │
                        │ -service (8012)      │
                        │ @DubboService +      │
                        │ @DubboReference      │
                        └───────────────────────┘
                                    │
                                    ▼
    ┌─────────────────────────────────────────────────────────────────┐
    │              trend-trading-backtest-service (8051)              │
    │              @DubboReference (消费方)                            │
    └─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
    ┌─────────────────────────────────────────────────────────────────┐
    │              Spring Cloud Gateway (8050)                        │
    │              动态路由 + JWT 认证                                  │
    └─────────────────────────────────────────────────────────────────┘

    ---
    三、升级方案详解

    3.1 阶段划分

    ┌────────┬─────────────────────────────────┬───────────┐
    │  阶段  │              内容               │   工期    │
    ├────────┼─────────────────────────────────┼───────────┤
    │ 阶段一 │ trend-api 模块创建              │ ✅ 已完成 │
    ├────────┼─────────────────────────────────┼───────────┤
    │ 阶段二 │ Nacos + Dubbo 基础设施搭建      │ 1天       │
    ├────────┼─────────────────────────────────┼───────────┤
    │ 阶段三 │ 服务注册与发现改造              │ 2-3天     │
    ├────────┼─────────────────────────────────┼───────────┤
    │ 阶段四 │ Dubbo RPC 服务改造              │ 3-5天     │
    ├────────┼─────────────────────────────────┼───────────┤
    │ 阶段五 │ 网关升级为 Spring Cloud Gateway │ 1-2天     │
    ├────────┼─────────────────────────────────┼───────────┤
    │ 阶段六 │ 配置中心迁移到 Nacos Config     │ 1天       │
    ├────────┼─────────────────────────────────┼───────────┤
    │ 阶段七 │ 集成测试与验证                  │ 2天       │
    └────────┴─────────────────────────────────┴───────────┘

    3.2 阶段二：Nacos + Dubbo 基础设施

    3.2.1 添加 Maven 依赖（父 pom.xml）

    <!-- Spring Cloud Alibaba (Nacos) BOM -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-alibaba-dependencies</artifactId>
        <version>2022.0.0.0</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>

    <!-- Dubbo3 BOM -->
    <dependency>
        <groupId>org.apache.dubbo</groupId>
        <artifactId>dubbo-bom</artifactId>
        <version>3.2.8</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>

    3.2.2 服务模块通用依赖

    <!-- Nacos 服务发现 -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>

    <!-- Dubbo RPC -->
    <dependency>
        <groupId>org.apache.dubbo</groupId>
        <artifactId>dubbo-spring-boot-starter</artifactId>
    </dependency>

    <!-- 移除 Eureka Client（将不再使用） -->
    <!--
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    -->

    3.2.3 application.yml 配置示例

    spring:
      application:
        name: index-codes-service
      cloud:
        nacos:
          discovery:
            server-addr: localhost:8848
            namespace: public
            group: DEFAULT_GROUP
          config:
            server-addr: localhost:8848
            file-extension: yml
            namespace: public
            group: DEFAULT_GROUP

    dubbo:
      registry:
        address: nacos://localhost:8848
      protocol:
        name: tri       # Triple 协议 (兼容 gRPC)
        port: -1        # 自动分配端口
      services:
        IndexCodesDubboService:
          version: 1.0.0
          group: DEFAULT_GROUP

    3.3 阶段三：服务注册与发现改造

    3.3.1 index-codes-service 改造

    1. 替换启动类注解

    // 旧：Eureka Client
    @EnableEurekaClient
    @EnableDiscoveryClient

    // 新：Nacos + Dubbo
    @EnableDubbo
    @EnableScheduling

    2. 实现 Dubbo 服务提供者

    package cn.how2j.trend.service.impl;

    import cn.how2j.trend.dubbo.IndexCodesDubboService;
    import cn.how2j.trend.pojo.Index;
    import cn.how2j.trend.service.IndexService;
    import com.apache.dubbo.config.annotation.DubboService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.cache.annotation.CacheConfig;
    import org.springframework.cache.annotation.Cacheable;

    import java.util.List;

    @DubboService(
        version = "1.0.0",
        group = "DEFAULT_GROUP",
        timeout = 3000,
        retries = 0
    )
    @CacheConfig(cacheNames = "indexes")
    public class IndexServiceImpl implements IndexService, IndexCodesDubboService {

        @Autowired
        private IndexService indexService;

        @Override
        @Cacheable(key = "'all_codes'")
        public List<Index> getIndexes() {
            return indexService.getIndexes();
        }

        @Override
        public List<Index> getCodes() {
            return getIndexes();
        }

        @Override
        public Index getIndex(String code) {
            return getIndexes().stream()
                .filter(i -> i.getCode().equals(code))
                .findFirst()
                .orElse(null);
        }

        @Override
        public List<Index> fresh() {
            // 实现刷新逻辑
            return getIndexes();
        }

        @Override
        public void remove(String code) {
            // 实现移除逻辑
        }

        @Override
        public Index store(String code) {
            // 实现存储逻辑
            return getIndex(code);
        }
    }

    3.4 阶段四：Dubbo RPC 服务改造

    3.4.1 消费端改造（index-gather-store-service）

    package cn.how2j.trend.service.impl;

    import cn.how2j.trend.dubbo.IndexCodesDubboService;
    import cn.how2j.trend.dubbo.ThirdPartIndexDataDubboService;
    import cn.how2j.trend.pojo.Index;
    import cn.how2j.trend.pojo.IndexData;
    import com.apache.dubbo.config.annotation.DubboReference;
    import io.github.resilience4j.annotation.CircuitBreaker;
    import org.springframework.stereotype.Service;

    import java.util.List;

    @Service
    public class IndexDataServiceImpl {

        @DubboReference(
            version = "1.0.0",
            group = "DEFAULT_GROUP",
            timeout = 5000,
            check = false
        )
        private ThirdPartIndexDataDubboService thirdPartIndexDataService;

        @DubboReference(
            version = "1.0.0",
            group = "DEFAULT_GROUP",
            timeout = 3000
        )
        private IndexCodesDubboService indexCodesService;

        @CircuitBreaker(name = "default", fallbackMethod = "thirdPartNotConnected")
        public List<IndexData> getIndexData(String code) {
            // 调用第三方 Dubbo 服务
            return thirdPartIndexDataService.getIndexData(code);
        }

        public List<IndexData> thirdPartNotConnected(String code, Throwable t) {
            // 降级处理
            return List.of();
        }

        public List<Index> getCodes() {
            return indexCodesService.getCodes();
        }
    }

    3.4.2 三种序列化协议对比

    ┌────────────┬────────────────────────┬─────────────────┐
    │    协议    │          特点          │    适用场景     │
    ├────────────┼────────────────────────┼─────────────────┤
    │ dubbo      │ Hessian2 序列化        │ 传统 Dubbo 兼容 │
    ├────────────┼────────────────────────┼─────────────────┤
    │ tri (推荐) │ Triple 协议，兼容 gRPC │ 跨语言、云原生  │
    ├────────────┼────────────────────────┼─────────────────┤
    │ rest       │ REST HTTP              │ 与网关集成      │
    └────────────┴────────────────────────┴─────────────────┘

    ---
    四、配置中心迁移

    4.1 Nacos Config vs Spring Cloud Config

    ┌────────────┬────────────────────┬─────────────────────┐
    │    特性    │    Nacos Config    │ Spring Cloud Config │
    ├────────────┼────────────────────┼─────────────────────┤
    │ 配置持久化 │ 内置支持           │ 依赖 Git            │
    ├────────────┼────────────────────┼─────────────────────┤
    │ 热刷新     │ @RefreshScope 自动 │ 需 POST /refresh    │
    ├────────────┼────────────────────┼─────────────────────┤
    │ 多环境     │ namespace + group  │ profile             │
    ├────────────┼────────────────────┼─────────────────────┤
    │ 权限管理   │ 内置支持           │ 需额外配置          │
    ├────────────┼────────────────────┼─────────────────────┤
    │ 高可用     │ 集群模式           │ 需 Git + Bus        │
    └────────────┴────────────────────┴─────────────────────┘

    4.2 配置示例

    # application.yml
    spring:
      application:
        name: index-codes-service
      cloud:
        nacos:
          config:
            server-addr: localhost:8848
            namespace: ${NACOS_NAMESPACE:public}
            group: ${NACOS_GROUP:DEFAULT_GROUP}
            file-extension: yml
            refresh-enabled: true
          discovery:
            server-addr: localhost:8848
            namespace: ${NACOS_NAMESPACE:public}
            group: ${NACOS_GROUP:DEFAULT_GROUP}

    ---
    五、网关升级方案

    5.1 Zuul → Spring Cloud Gateway

    ┌──────────┬──────────────────┬──────────────────────┐
    │   特性   │ Netflix Zuul 1.x │ Spring Cloud Gateway │
    ├──────────┼──────────────────┼──────────────────────┤
    │ I/O 模型 │ 阻塞             │ 非阻塞 (Netty)       │
    ├──────────┼──────────────────┼──────────────────────┤
    │ 编程模型 │ Filter           │ Handler/Filter       │
    ├──────────┼──────────────────┼──────────────────────┤
    │ 动态路由 │ 需重启           │ 无需重启             │
    ├──────────┼──────────────────┼──────────────────────┤
    │ 限流     │ 需集成 Bucket4j  │ 内置支持             │
    ├──────────┼──────────────────┼──────────────────────┤
    │ 认证鉴权 │ Filter 实现      │ 需自定义 Filter      │
    └──────────┴──────────────────┴──────────────────────┘

    5.2 Gateway 改造示例

    package cn.how2j.trend.gateway;

    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;
    import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
    import org.springframework.cloud.gateway.route.RouteLocator;
    import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
    import org.springframework.context.annotation.Bean;

    @SpringBootApplication
    @EnableDiscoveryClient
    public class ApiGatewayApplication {

        public static void main(String[] args) {
            SpringApplication.run(ApiGatewayApplication.class, args);
        }

        @Bean
        public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
            return builder.routes()
                .route("index-codes", r -> r
                    .path("/api-codes/**")
                    .filters(f -> f.stripPrefix(1))
                    .uri("dubbo://index-codes-service:8010"))
                .route("trend-backtest", r -> r
                    .path("/api-backtest/**")
                    .filters(f -> f.stripPrefix(1))
                    .uri("dubbo://trend-trading-backtest-service:8051"))
                .build();
        }
    }

    ---
    六、关键风险与缓解措施

    ┌───────────────────────┬────────────────┬──────────────────────────────────────┐
    │         风险          │      影响      │               缓解措施               │
    ├───────────────────────┼────────────────┼──────────────────────────────────────┤
    │ Dubbo 与 Feign 并存   │ 迁移周期长     │ 分阶段迁移，逐步切换                 │
    ├───────────────────────┼────────────────┼──────────────────────────────────────┤
    │ 服务版本兼容性        │ 服务间调用失败 │ 使用 version + group 隔离            │
    ├───────────────────────┼────────────────┼──────────────────────────────────────┤
    │ Nacos 单点故障        │ 服务不可用     │ 部署 Nacos 集群（3节点）             │
    ├───────────────────────┼────────────────┼──────────────────────────────────────┤
    │ 配置热刷新生效延迟    │ 配置变更不生效 │ 等待 @RefreshScope 完成后验证        │
    ├───────────────────────┼────────────────┼──────────────────────────────────────┤
    │ 网关路由到 Dubbo 服务 │ 协议不兼容     │ 使用 Spring Cloud Gateway Dubbo 集成 │
    └───────────────────────┴────────────────┴──────────────────────────────────────┘

    ---
    七、测试验证清单

    7.1 单元测试

    # 编译验证
    mvn clean compile -DskipTests

    # 打包验证
    mvn clean package -DskipTests

    # 运行测试
    mvn test

    7.2 集成测试

    ┌────────────────┬─────────────────────────────────────────────────────┐
    │     测试项     │                      验证方法                       │
    ├────────────────┼─────────────────────────────────────────────────────┤
    │ Nacos 服务注册 │ 访问 http://localhost:8848/nacos 查看服务列表       │
    ├────────────────┼─────────────────────────────────────────────────────┤
    │ Dubbo RPC 调用 │ 调用 index-codes-service 的 getCodes() 方法         │
    ├────────────────┼─────────────────────────────────────────────────────┤
    │ 配置热刷新     │ 修改 Nacos 配置，验证应用自动reload                 │
    ├────────────────┼─────────────────────────────────────────────────────┤
    │ Gateway 路由   │ 访问 http://localhost:8050/api-codes/codes 验证路由 │
    ├────────────────┼─────────────────────────────────────────────────────┤
    │ 熔断器         │ 模拟服务不可用，验证降级逻辑                        │
    └────────────────┴─────────────────────────────────────────────────────┘

    ---
    八、升级时间线预估

    Week 1: 基础设施搭建 + Nacos 单机部署
            - 安装 Nacos 2.2.x
            - 创建 trend-api 模块
            - 父 pom 依赖完善

    Week 2-3: 服务注册与发现改造
            - index-codes-service Dubbo 化
            - index-data-service Dubbo 化
            - third-part-index-data-project Dubbo 化

    Week 4: 服务间调用改造
            - index-gather-store-service 消费端改造
            - trend-trading-backtest-service 消费端改造
            - 熔断器配置验证

    Week 5: 网关 + 配置中心
            - 部署 Spring Cloud Gateway
            - Nacos Config 配置迁移
            - 数据验证

    Week 6: 测试与优化
            - 全链路测试
            - 性能基准测试
            - 文档更新

    ---
    九、附录

    9.1 Nacos 快速部署

    # 下载 Nacos
    wget https://github.com/alibaba/nacos/releases/download/2.2.3/nacos-server-2.2.3.zip

    # 解压并启动（单机模式）
    unzip nacos-server-2.2.3.zip
    cd nacos/bin
    ./startup.sh -m standalone

    9.2 相关资源

    - https://dubbo.apache.org/zh/docs3-v2/java-sdk/
    - https://nacos.io/zh/docs/quick-start.html
    - https://spring.io/projects/spring-cloud-gateway
    - https://spring-cloud-alibaba-group.github.io/github-pages/2022.0.0.0/zh-cn/