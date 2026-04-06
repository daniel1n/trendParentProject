# JDK 8 → JDK 17 升级指南

## 升级概述

| 项目 | 升级前 | 升级后 |
|------|--------|--------|
| JDK | 1.8 | 17 |
| Spring Boot | 2.0.3.RELEASE | 2.7.18 |
| Spring Cloud | Finchley.RELEASE | 2022.0.4 |
| Hutool | 4.3.1 | 5.8.22 |

---

## 一、父 pom.xml 变更

### 文件：`pom.xml`

```diff
@@ -28,15 +28,16 @@
     <parent>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter-parent</artifactId>
-        <version>2.0.3.RELEASE</version>
+        <version>2.7.18</version>
         <relativePath/>
     </parent>

     <properties>
         <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
         <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
-        <java.version>1.8</java.version>
-        <spring-cloud.version>Finchley.RELEASE</spring-cloud.version>
+        <java.version>17</java.version>
+        <spring-cloud.version>2022.0.4</spring-cloud.version>
+        <hutool.version>5.8.22</hutool.version>
     </properties>
```

**变更位置：**
- 第31行：Spring Boot 版本 `2.0.3.RELEASE` → `2.7.18`
- 第38行：Java 版本 `1.8` → `17`
- 第39行：Spring Cloud 版本 `Finchley.RELEASE` → `2022.0.4`
- 第40行（新增）：Hutool 版本属性 `hutool.version=5.8.22`

```diff
@@ -50,7 +51,7 @@
         <dependency>
             <groupId>cn.hutool</groupId>
             <artifactId>hutool-all</artifactId>
-            <version>4.3.1</version>
+            <version>${hutool.version}</version>
         </dependency>
```

**变更位置：**
- 第54行：Hutool 版本 `4.3.1` → `${hutool.version}`

```diff
@@ -70,6 +71,33 @@
                 <type>pom</type>
                 <scope>import</scope>
             </dependency>
+            <!-- Netflix components for Spring Cloud 2022.0.4 -->
+            <dependency>
+                <groupId>org.springframework.cloud</groupId>
+                <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
+                <version>2.2.10.RELEASE</version>
+            </dependency>
+            <dependency>
+                <groupId>org.springframework.cloud</groupId>
+                <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
+                <version>2.2.10.RELEASE</version>
+            </dependency>
+            <dependency>
+                <groupId>org.springframework.cloud</groupId>
+                <artifactId>spring-cloud-starter-netflix-turbine</artifactId>
+                <version>2.2.10.RELEASE</version>
+            </dependency>
+            <dependency>
+                <groupId>org.springframework.cloud</groupId>
+                <artifactId>spring-cloud-starter-netflix-zuul</artifactId>
+                <version>2.2.10.RELEASE</version>
+            </dependency>
+            <!-- Sleuth Zipkin for Spring Cloud 2022.0.4 -->
+            <dependency>
+                <groupId>org.springframework.cloud</groupId>
+                <artifactId>spring-cloud-sleuth-zipkin</artifactId>
+                <version>3.1.9</version>
+            </dependency>
```

**变更位置：**
- 第74-100行（新增）：Netflix 组件和 Zipkin 依赖版本声明

---

## 二、模块 pom.xml 变更

### 2.1 index-gather-store-service

**文件：`index-gather-store-service/pom.xml`**

```diff
@@ -23,10 +23,10 @@
             <groupId>org.springframework.cloud</groupId>
             <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
         </dependency>
-        <!-- 断路器 -->
+        <!-- 断路器 (Resilience4j) -->
         <dependency>
             <groupId>org.springframework.cloud</groupId>
-            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
+            <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
         </dependency>
```

**变更位置：**
- 第26行：注释 `断路器` → `断路器 (Resilience4j)`
- 第29行：依赖 `spring-cloud-starter-netflix-hystrix` → `spring-cloud-starter-circuitbreaker-resilience4j`

### 2.2 trend-trading-backtest-service

**文件：`trend-trading-backtest-service/pom.xml`**

```diff
@@ -28,15 +28,15 @@
             <artifactId>spring-cloud-starter-openfeign</artifactId>
         </dependency>

-        <!-- 断路器 -->
+        <!-- 断路器 (Resilience4j) -->
         <dependency>
             <groupId>org.springframework.cloud</groupId>
-            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
+            <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
         </dependency>
         <!--zipkin-->
         <dependency>
             <groupId>org.springframework.cloud</groupId>
-            <artifactId>spring-cloud-starter-zipkin</artifactId>
+            <artifactId>spring-cloud-sleuth-zipkin</artifactId>
         </dependency>
```

**变更位置：**
- 第31行：注释 `断路器` → `断路器 (Resilience4j)`
- 第34行：依赖 `spring-cloud-starter-netflix-hystrix` → `spring-cloud-starter-circuitbreaker-resilience4j`
- 第39行：依赖 `spring-cloud-starter-zipkin` → `spring-cloud-sleuth-zipkin`

### 2.3 Zipkin 依赖包名变更

以下模块的 pom.xml 变更相同（第32行）：

**文件：`index-codes-service/pom.xml`**
```diff
-            <artifactId>spring-cloud-starter-zipkin</artifactId>
+            <artifactId>spring-cloud-sleuth-zipkin</artifactId>
```

**文件：`index-data-service/pom.xml`**
```diff
-            <artifactId>spring-cloud-starter-zipkin</artifactId>
+            <artifactId>spring-cloud-sleuth-zipkin</artifactId>
```

**文件：`index-zuul-service/pom.xml`**
```diff
-            <artifactId>spring-cloud-starter-zipkin</artifactId>
+            <artifactId>spring-cloud-sleuth-zipkin</artifactId>
```

**文件：`trend-trading-backtest-view/pom.xml`**
```diff
-            <artifactId>spring-cloud-starter-zipkin</artifactId>
+            <artifactId>spring-cloud-sleuth-zipkin</artifactId>
```

---

## 三、Java 代码变更

### 3.1 Hutool NetUtil 包名变更

**影响：所有 Application 类**

```diff
// 升级前
import cn.hutool.core.util.NetUtil;

// 升级后
import cn.hutool.core.net.NetUtil;
```

| 文件 | 行号 |
|------|------|
| `eureka-server/.../EurekaServerApplication.java` | 第3行 |
| `index-codes-service/.../IndexCodesApplication.java` | 第5行 |
| `index-data-service/.../IndexDataApplication.java` | 第5行 |
| `index-gather-store-service/.../IndexGatherStoreApplication.java` | 第4行 |
| `index-zuul-service/.../IndexZuulServiceApplication.java` | 第3行 |
| `index-config-server/.../IndexConfigServerApplication.java` | 第3行 |
| `index-hystrix-dashboard/.../IndexHystrixDashboardApplication.java` | 第3行 |
| `index-turbine/.../IndexTurbineApplication.java` | 第3行 |
| `third-part-index-data-project/.../ThirdPartIndexDataApplication.java` | 第4行 |
| `trend-trading-backtest-service/.../TrendTradingBackTestServiceApplication.java` | 第5行 |
| `trend-trading-backtest-view/.../TrendTradingBackTestViewApplication.java` | 第5行 |

---

### 3.2 @EnableEurekaClient → @EnableDiscoveryClient

```diff
// 升级前
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
@EnableEurekaClient

// 升级后
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
@EnableDiscoveryClient
```

| 文件 | 旧导入行号 | 旧注解行号 | 新导入行号 | 新注解行号 |
|------|-----------|-----------|-----------|-----------|
| `eureka-server/.../EurekaServerApplication.java` | 6 | 14 | 5 | 13 |
| `index-codes-service/.../IndexCodesApplication.java` | 11 | 26 | 11 | 25 |
| `index-data-service/.../IndexDataApplication.java` | 11 | 26 | 11 | 25 |
| `index-gather-store-service/.../IndexGatherStoreApplication.java` | 10 | 20 | 10 | 19 |
| `index-zuul-service/.../IndexZuulServiceApplication.java` | 6 | 17 | 6 | 16 |
| `index-config-server/.../IndexConfigServerApplication.java` | 8 | 17 | 7 | 16 |
| `third-part-index-data-project/.../ThirdPartIndexDataApplicationApplication.java` | 9 | 16 | 9 | 15 |
| `trend-trading-backtest-service/.../TrendTradingBackTestServiceApplication.java` | 10 | 28 | 8 | 25 |
| `trend-trading-backtest-view/.../TrendTradingBackTestViewApplication.java` | 11 | 25 | 10 | 24 |

---

### 3.3 @EnableHystrix 已移除

**文件：`index-gather-store-service/src/main/java/cn/how2j/trend/IndexGatherStoreApplication.java`**

```diff
@@ -1,14 +1,13 @@
 package cn.how2j.trend;

 import cn.hutool.core.convert.Convert;
-import cn.hutool.core.util.NetUtil;
+import cn.hutool.core.net.NetUtil;
 import cn.hutool.core.util.NumberUtil;
 import cn.hutool.core.util.StrUtil;
 import org.springframework.boot.autoconfigure.SpringBootApplication;
 import org.springframework.boot.builder.SpringApplicationBuilder;
 import org.springframework.cache.annotation.EnableCaching;
-import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
-import org.springframework.cloud.netflix.hystrix.EnableHystrix;
+import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
 import org.springframework.context.annotation.Bean;
 import org.springframework.web.client.RestTemplate;

@@ -17,8 +16,7 @@ import org.springframework.web.client.RestTemplate;
  * @date 2020-6-13 11:08
  */
 @SpringBootApplication
-@EnableEurekaClient
-@EnableHystrix
+@EnableDiscoveryClient
 @EnableCaching
```

**变更位置：**
- 第3行：`cn.hutool.core.util.NetUtil` → `cn.hutool.core.net.NetUtil`
- 第10-11行（删除）：`EnableEurekaClient` 和 `EnableHystrix` 导入
- 第13行（新增）：`EnableDiscoveryClient` 导入
- 第19-20行（删除）：`@EnableEurekaClient` 和 `@EnableHystrix` 注解
- 第19行（新增）：`@EnableDiscoveryClient` 注解

---

### 3.4 @EnableCircuitBreaker 已移除

**文件：`trend-trading-backtest-service/src/main/java/cn/how2j/trend/TrendTradingBackTestServiceApplication.java`**

```diff
@@ -1,18 +1,14 @@
 package cn.how2j.trend;

-import brave.sampler.Sampler;
 import cn.hutool.core.convert.Convert;
 import cn.hutool.core.thread.ThreadUtil;
-import cn.hutool.core.util.NetUtil;
+import cn.hutool.core.net.NetUtil;
 import cn.hutool.core.util.NumberUtil;
 import cn.hutool.core.util.StrUtil;
 import org.springframework.boot.autoconfigure.SpringBootApplication;
 import org.springframework.boot.builder.SpringApplicationBuilder;
-import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
 import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
-import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
 import org.springframework.cloud.openfeign.EnableFeignClients;
-import org.springframework.context.annotation.Bean;

 import java.util.Scanner;
@@ -25,10 +21,8 @@ import java.util.concurrent.TimeoutException;
  * @date 2020-6-14 10:48
  */
 @SpringBootApplication
-@EnableEurekaClient
 @EnableDiscoveryClient
 @EnableFeignClients
-@EnableCircuitBreaker
 public class TrendTradingBackTestServiceApplication {
```

**变更位置：**
- 第3行（删除）：`import brave.sampler.Sampler;`
- 第5行：`cn.hutool.core.util.NetUtil` → `cn.hutool.core.net.NetUtil`
- 第9-11行（删除）：`EnableCircuitBreaker`、`EnableEurekaClient`、`Bean` 导入
- 第13行（删除）：`EnableEurekaClient` 导入
- 第26-28行：删除 `@EnableEurekaClient` 和 `@EnableCircuitBreaker` 注解

---

### 3.5 @HystrixCommand → @CircuitBreaker

#### 3.5.1 IndexServiceImpl

**文件：`index-gather-store-service/src/main/java/cn/how2j/trend/service/impl/IndexServiceImpl.java`**

```diff
@@ -5,7 +5,7 @@ import cn.how2j.trend.service.IndexService;
 import cn.how2j.trend.util.SpringContextUtil;
 import cn.hutool.core.collection.CollUtil;
 import cn.hutool.core.collection.CollectionUtil;
-import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
+import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
 import org.springframework.beans.factory.annotation.Autowired;

@@ -30,7 +30,7 @@ public class IndexServiceImpl implements IndexService {
     private RestTemplate restTemplate;

     @Override
-    @HystrixCommand(fallbackMethod = "thirdPartNotConnected")
+    @CircuitBreaker(name = "default", fallbackMethod = "thirdPartNotConnected")
     public List<Index> fresh() {

@@ -65,8 +65,8 @@ public class IndexServiceImpl implements IndexService {
     }

     @Override
-    public List<Index> thirdPartNotConnected() {
-        System.out.println("thirdPartNotConnected()");
+    public List<Index> thirdPartNotConnected(Throwable t) {
+        System.out.println("thirdPartNotConnected(), cause: " + t.getMessage());
         Index index = new Index();
         index.setCode("000000");
         index.setName("无效指数代码");
```

**变更位置：**
- 第8行：`com.netflix.hystrix...HystrixCommand` → `io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker`
- 第33行：`@HystrixCommand(fallbackMethod = "thirdPartNotConnected")` → `@CircuitBreaker(name = "default", fallbackMethod = "thirdPartNotConnected")`
- 第68行：`thirdPartNotConnected()` → `thirdPartNotConnected(Throwable t)`
- 第69行：`System.out.println("thirdPartNotConnected()")` → `System.out.println("thirdPartNotConnected(), cause: " + t.getMessage())`

#### 3.5.2 IndexDataServiceImpl

**文件：`index-gather-store-service/src/main/java/cn/how2j/trend/service/impl/IndexDataServiceImpl.java`**

```diff
@@ -6,7 +6,7 @@ import cn.how2j.trend.util.SpringContextUtil;
 import cn.hutool.core.collection.CollUtil;
 import cn.hutool.core.collection.CollectionUtil;
 import cn.hutool.core.convert.Convert;
-import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
+import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
 import org.springframework.beans.factory.annotation.Autowired;

@@ -33,7 +33,7 @@ public class IndexDataServiceImpl implements IndexDataService {
     private RestTemplate restTemplate;

     @Override
-    @HystrixCommand(fallbackMethod = "thirdPartNotConnected")
+    @CircuitBreaker(name = "default", fallbackMethod = "thirdPartNotConnected")
     public List<IndexData> fresh(String code) {

@@ -75,8 +75,8 @@ public class IndexDataServiceImpl implements IndexDataService {
     }

     @Override
-    public List<IndexData> thirdPartNotConnected(String code) {
-        System.out.println("thirdPartNotConnected()");
+    public List<IndexData> thirdPartNotConnected(String code, Throwable t) {
+        System.out.println("thirdPartNotConnected(), cause: " + t.getMessage());
         IndexData indexData = new IndexData();
         indexData.setClosePoint(0);
         indexData.setDate("n/a");
```

**变更位置：**
- 第9行：`com.netflix.hystrix...HystrixCommand` → `io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker`
- 第36行：`@HystrixCommand(fallbackMethod = "thirdPartNotConnected")` → `@CircuitBreaker(name = "default", fallbackMethod = "thirdPartNotConnected")`
- 第78行：`thirdPartNotConnected(String code)` → `thirdPartNotConnected(String code, Throwable t)`
- 第79行：`System.out.println("thirdPartNotConnected()")` → `System.out.println("thirdPartNotConnected(), cause: " + t.getMessage())`

---

### 3.6 接口签名变更

#### IndexService

**文件：`index-gather-store-service/src/main/java/cn/how2j/trend/service/IndexService.java`**

```diff
@@ -54,9 +54,10 @@ public interface IndexService {
      * 如果fetch_indexes_from_third_part获取失败了，
      * 就自动调用 third_part_not_connected 并返回
      *
+     * @param t 异常信息
      * @return 断路器的数据
      */
-    List<Index> thirdPartNotConnected();
+    List<Index> thirdPartNotConnected(Throwable t);
```

**变更位置：**
- 第58行（新增）：`@param t 异常信息`
- 第60行：`thirdPartNotConnected()` → `thirdPartNotConnected(Throwable t)`

#### IndexDataService

**文件：`index-gather-store-service/src/main/java/cn/how2j/trend/service/IndexDataService.java`**

```diff
@@ -61,9 +61,10 @@ public interface IndexDataService {
      * 就自动调用 third_part_not_connected 并返回
      *
      * @param code 证券代码
+     * @param t 异常信息
      * @return 断路器的数据
      */
-    List<IndexData> thirdPartNotConnected(String code);
+    List<IndexData> thirdPartNotConnected(String code, Throwable t);
```

**变更位置：**
- 第64行（新增）：`@param t 异常信息`
- 第66行：`thirdPartNotConnected(String code)` → `thirdPartNotConnected(String code, Throwable t)`

---

### 3.7 brave.sampler.Sampler Bean 已移除

**说明：Spring Cloud Sleuth 3.x 通过配置文件设置采样率**

```diff
// 升级前 - 删除以下代码
import brave.sampler.Sampler;

@Bean
public Sampler defaultSampler() {
    return Sampler.ALWAYS_SAMPLE;
}
```

**影响文件及变更位置：**

| 文件 | 删除导入行号 | 删除 Bean 行号 |
|------|-------------|---------------|
| `index-codes-service/.../IndexCodesApplication.java` | 3 | 88-91 |
| `index-data-service/.../IndexDataApplication.java` | 3 | 90-93 |
| `index-zuul-service/.../IndexZuulServiceApplication.java` | 3, 8 | 33-36 |
| `trend-trading-backtest-service/.../TrendTradingBackTestServiceApplication.java` | 3, 11, 14 | 89-92 |
| `trend-trading-backtest-view/.../TrendTradingBackTestViewApplication.java` | 3 | 95-98 |

---

### 3.8 重复注解清理

部分文件存在重复的 `@EnableDiscoveryClient` 注解，删除了重复项。

---

## 四、变更文件清单

### 4.1 POM 文件（7个）

| 模块 | 文件路径 | 变更类型 |
|------|----------|----------|
| 父项目 | `pom.xml` | 版本升级 + 新增依赖管理 |
| index-gather-store-service | `index-gather-store-service/pom.xml` | Hystrix → Resilience4j |
| trend-trading-backtest-service | `trend-trading-backtest-service/pom.xml` | Hystrix → Resilience4j + Zipkin |
| index-codes-service | `index-codes-service/pom.xml` | Zipkin 包名变更 |
| index-data-service | `index-data-service/pom.xml` | Zipkin 包名变更 |
| index-zuul-service | `index-zuul-service/pom.xml` | Zipkin 包名变更 |
| trend-trading-backtest-view | `trend-trading-backtest-view/pom.xml` | Zipkin 包名变更 |

### 4.2 Java 文件（13个）

| 模块 | 文件路径 | 变更类型 |
|------|----------|----------|
| eureka-server | `EurekaServerApplication.java` | NetUtil 包名 |
| third-part-index-data-project | `ThirdPartIndexDataApplication.java` | NetUtil + EnableEurekaClient |
| index-gather-store-service | `IndexGatherStoreApplication.java` | NetUtil + EnableEurekaClient + EnableHystrix |
| index-gather-store-service | `IndexService.java` | 接口签名变更 |
| index-gather-store-service | `IndexDataService.java` | 接口签名变更 |
| index-gather-store-service | `IndexServiceImpl.java` | @HystrixCommand → @CircuitBreaker |
| index-gather-store-service | `IndexDataServiceImpl.java` | @HystrixCommand → @CircuitBreaker |
| index-codes-service | `IndexCodesApplication.java` | NetUtil + EnableEurekaClient + Sampler Bean |
| index-data-service | `IndexDataApplication.java` | NetUtil + EnableEurekaClient + Sampler Bean |
| index-zuul-service | `IndexZuulServiceApplication.java` | NetUtil + EnableEurekaClient + Sampler Bean |
| index-config-server | `IndexConfigServerApplication.java` | NetUtil + EnableEurekaClient |
| trend-trading-backtest-service | `TrendTradingBackTestServiceApplication.java` | NetUtil + EnableEurekaClient + EnableCircuitBreaker + Sampler Bean |
| trend-trading-backtest-view | `TrendTradingBackTestViewApplication.java` | NetUtil + EnableEurekaClient + Sampler Bean |
| index-hystrix-dashboard | `IndexHystrixDashboardApplication.java` | NetUtil |
| index-turbine | `IndexTurbineApplication.java` | NetUtil |

---

## 五、依赖版本对照表

| 依赖 | 旧版本 | 新版本 | 备注 |
|------|--------|--------|------|
| Spring Boot | 2.0.3.RELEASE | 2.7.18 | |
| Spring Cloud | Finchley.RELEASE | 2022.0.4 | |
| Java | 1.8 | 17 | |
| Hutool | 4.3.1 | 5.8.22 | |
| spring-cloud-starter-netflix-hystrix | BOM管理 | 2.2.10.RELEASE | 需手动声明 |
| spring-cloud-starter-netflix-hystrix-dashboard | BOM管理 | 2.2.10.RELEASE | 需手动声明 |
| spring-cloud-starter-netflix-turbine | BOM管理 | 2.2.10.RELEASE | 需手动声明 |
| spring-cloud-starter-netflix-zuul | BOM管理 | 2.2.10.RELEASE | 需手动声明 |
| spring-cloud-starter-zipkin | BOM管理 | **已移除** | 改用 spring-cloud-sleuth-zipkin:3.1.9 |

---

## 六、构建命令变更

```bash
# 升级前
mvn clean install
mvn clean install -DskipTests

# 升级后（跳过测试编译和运行）
mvn clean package -Dmaven.test.skip=true
```

---

## 七、运行时依赖

### 必须
- **Redis** (端口 6379)
  - index-gather-store-service
  - index-codes-service
  - index-data-service

### 可选
- **RabbitMQ** (端口 5672) - trend-trading-backtest-view 需要

### 不需要
- MySQL（项目中无 MySQL 依赖）

---

## 八、验证步骤

```bash
# 1. 编译验证
mvn clean compile -Dmaven.test.skip=true

# 2. 打包验证
mvn clean package -Dmaven.test.skip=true

# 3. 查看构建产物
ls -la */target/*.jar
```
