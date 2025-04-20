
## 一、SpringCloud 简介

### 核心思想

- Spring Cloud 的核心思想是将大型应用拆分为多个微服务，每个模块可以由不同的团队独立开发，最终通过路由进行服务调用和转发。
### Maven配置

- **父 POM**：
    
    - 使用 Maven 构建父 POM 文件，利用 `dependencyManagement` 来管理子模块的依赖版本，确保所有模块使用一致的依赖版本，从而提高兼容性和维护性。
    - 在父 POM 中的 `<modules>` 标签下列出所有子模块。
- **公共模块**：
    
    - 创建公共模块时，不需要启动类，只需定义工具类、接口等，其他子模块可以通过 Maven 引用该模块的依赖
### 模块间通信

- 不同模组之间是通过 openfeign 工具进行互通，调用模块其需要在其项目中创建一个接口，并且需要在该接口上面添加@FeignClient(value = "xxx"),x为被调用模块的名称。
	- 在其控制层中使用时通过DI其接口，进而在接口中调用调用对应的接口方法。
	- 接口中的内容就是xxx模块中控制层中的方法签名；

### 常用的中间体

- **Redis**：用于缓存和数据存储，可提高系统性能。
- **Nacos**：作为服务发现和配置管理工具，支持动态配置和服务注册。
- **Sentinel/Dubbo**：用于服务熔断和流量控制，确保系统的稳定性。
- **RabbitMQ/Kafka**：用于异步消息处理和事件驱动架构，提升系统的解耦性和响应速度。
- **Spring Cloud Gateway**：用于API网关，集中管理路由和过滤器，提供统一的入口。
- **Spring Security** 进行认证和授权，保护微服务的访问安全。
- **任务调度Quartz、Elastic Job**：用于定时执行任务，管理定时任务和批量处理，确保任务的可靠执行。

## 二、Nacos 注册中心

###  定义

Nacos 是一个开源的服务注册与配置管理中心，旨在帮助开发者简化微服务架构中的服务发现和配置管理。它提供可视化界面，支持动态配置和热重载，从而提高开发效率。为了启用动态更新功能，需要在启动类上添加 `@RefreshScope` 注解。
### 作用

- **服务发现**：Nacos 允许服务实例注册，并使其他服务能够发现这些实例，简化了服务间的调用。
- **动态配置管理**：通过 Nacos，开发者可以在运行时修改配置，而无需重启服务，极大提高了系统的灵活性。
- **负载均衡**：结合 Nacos 的实例管理，支持轮询等多种负载均衡策略，确保请求均匀分配到各个实例。
	- **心跳机制**：Nacos 通过心跳任务监控服务实例的健康状态。
### 配置

- **子模块application.yml配置**：以下是子模块的通用配置内容，包含 Nacos 的地址和配置文件的名称。（其中的import中的需要在nacos的可视化平台进行创建）
```
spring:  
  application:  
    name: gateway-service  
  config:  
    import: nacos:gateway-service-dev.yml  
  cloud:  
    nacos:  
      discovery:  
        server-addr: localhost:8848  
      config:  
        server-addr: localhost:8848  
        file-extension: yml
```

- **子模块 pom.xml 配置依赖**：
```xml
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>

        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
```

- **nacos平台配置**(跟子模块的配置一样，只是将其内容换到nacos的平台进行设置)
```yml
server:
  port: 9083
```
## 三、Gateway路由搭建

### 定义

Gateway 路由的作用是作为统一的网关，为前端提供公共 IP，避免在访问不同模块时需要使用不同的 IP 地址。通过 Gateway，客户端只需访问一个入口点，简化了服务调用。
### 作用

- **统一入口**：为微服务提供一个统一的访问入口，简化客户端的调用。
- **负载均衡**：通过集成负载均衡器，确保请求均匀分配到多个服务实例。
- **路由管理**：可以根据请求路径、方法等条件进行路由转发，灵活配置路由规则。
- **流量控制**：结合 Sentinel，可以实现流量控制和熔断，保障系统的稳定性。

### 配置和依赖

- 搭建 Gateway 路由需要在 `pom.xml` 中添加以下依赖：

```
<!-- Gateway -->
<dependency>  
    <groupId>org.springframework.cloud</groupId>  
    <artifactId>spring-cloud-starter-gateway</artifactId>  
</dependency>  
<dependency>  
    <groupId>org.springframework.cloud</groupId>  
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>  
</dependency>

```

- 其 Gateway 在 nacos 中的 yml 配置
```yml
server:
  port: 9998
spring:
  cloud:
    gateway:
      default-filters:
        - AddRequestHeader=origin,gateway # 添加名为origin的请求头，值为gateway
      globalcors:
        cors-configurations:
          '[/**]': # 匹配所有请求
            allowedOrigins: "*" #跨域处理 允许所有的域
            allowedMethods: # 支持的方法
              - GET
              - POST
              - PUT
              - DELETE
      discovery:
        locator:
          enabled: true #开启注册中心路由功能
      routes:  # 路由
        - id: order-service #路由ID，没有固定要求，但是要保证唯一，建议配合服务名
          uri: lb://order-service # 匹配提供服务的路由地址
          predicates: # 断言
            - Path=/order/** # 断言，路径相匹配进行路由
        - id: point-service #路由ID，没有固定要求，但是要保证唯一，建议配合服务名
          uri: lb://point-service # 匹配提供服务的路由地址
          predicates: # 断言
            - Path=/point/** # 断言，路径相匹配进行路由
        - id: user-service #路由ID，没有固定要求，但是要保证唯一，建议配合服务名
          uri: lb://user-service # 匹配提供服务的路由地址
          predicates: # 断言
            - Path=/user/** # 断言，路径相匹配进行路由
```




### 关于 Gateway 和 Nginx 的区别

- **Gateway**：通常是微服务架构中用于API管理的组件，负责路由、负载均衡、认证、限流等功能。它可以处理内部服务间的通信和外部请求。
- **Nginx**：是一个高性能的反向代理服务器和负载均衡器。除了处理静态文件外，Nginx 还可以用于流量控制、SSL 终端、缓存等。
## 四、Sentinel 检测中心

### 定义

Sentinel 是阿里巴巴开源的一个轻量级流量控制组件，旨在帮助开发者在分布式系统中实现流量控制、熔断、降级等功能，以保障系统的稳定性和高可用性。它通过监控服务的 QPS（每秒请求数）、响应时间等指标，动态调整流量策略。

### 流控规则

QPS（Queries Per Second）是衡量系统处理请求能力的重要指标。Sentinel 允许开发者设定 QPS 限流规则，当请求数超过设定的阈值时，自动进行流量控制，防止系统过载。通过监控 QPS，开发者可以及时发现并处理流量异常问题。

- 阀值类型：限制流量的标准，可以是固定值或动态生成的值。
	- QPS
	- 并发线程数
- 单机阀值：针对单个服务实例的流量限制，如 1000 QPS、1000个线程
- 集群：针对单个服务实例的流量限制，如 1000 QPS、1000个线程
- 流控模式：
	- **直接**：直接对请求进行限流。
	- **关联**：多个资源间的关联流控。
	- **链路**：根据服务调用链的情况进行流控。
- 流控效果：
	- **快速失败**：一旦超过阈值，立即返回错误。（即在查询访问期间，最多可以只有单机阀值个查询，其他的就返回错误）
	- **Warm Up**：在流量增长时逐渐增加流量限制。
		- **预热时长**：指在流量增加的过程中，系统所需的时间段。在这个时间段内，流量限制会逐渐上升，而不是立即达到最大值。
	- **排队等待**：将请求排队，直到流量恢复正常。
		- 超时时长：超过多少时间后，对应的查询就是失败。
### 熔断

熔断是指在系统出现故障时，主动中断对某个服务的调用，以避免错误请求进一步影响系统的稳定性。Sentinel 提供了熔断机制，当请求失败率超过一定比例时，将自动断开对该服务的请求，直到系统恢复正常。熔断的主要目的是保护系统，避免因个别服务的故障导致整个系统崩溃。

-  熔断策略
	- **慢调用比例**：定义在一定时间窗口内，慢请求占总请求的比例。若该比例超过配置值，则触发熔断。例如，设置为 20%，意味着如果在统计窗口内，20% 以上的请求响应时间超过设定的最大 RT，则触发熔断。
	- **异常比例**：定义在一定时间窗口内，失败请求占总请求的比例。当请求失败率超过此比例时，触发熔断。例如，设置为 10%，表示如果 10% 以上的请求失败，则进行熔断。
	- **异常数**：在统计窗口内，请求失败的绝对数量。当失败次数超过此值时触发熔断。例如，设置为 50 表示如果在统计窗口内失败的请求数超过 50 次，则触发熔断。
-  其他配置
	- **最大 RT**：最大响应时间阈值。如果请求的响应时间超过此值，将被视为慢请求。可设置为 500 ms。
	- **比例阀值**：熔断触发的比例阈值。可以设置为 30%，表示当请求失败率达到 30% 时触发熔断。
	- **熔断时长**：熔断持续的时间。设置为 60 秒，在此期间系统将拒绝所有对该服务的请求。
	- **最小请求数**：触发熔断所需的最小请求数。只有当请求数超过该值时，熔断策略才会生效。例如，设置为 100，意味着在统计窗口内至少需要 100 次请求。
	- **统计时长**：统计熔断条件的时间窗口。设置为 1 分钟，表示在过去 1 分钟内统计请求的成功率和失败率。
### 热点

热点问题是指某个特定的资源或服务在短时间内接收到大量请求，可能导致系统过载。Sentinel 的热点规则可以识别并控制这种情况，允许开发者设定特定资源的流量控制策略，以确保系统的稳定性。例如，可以对特定的 URL 或 API 接口设置流量限制，避免瞬时流量带来的压力。


- **限流模式**：通常使用 **QPS 模式**，即以每秒请求数（QPS）为单位对热点资源进行流量控制。
- **参数索引**：用于识别请求中的具体参数，例如，可以根据某个查询参数（如用户 ID 或商品 ID）进行流控。开发者可以设定请求的特定参数作为热点索引，以便更精确地控制流量。
- **单机阀值**：针对单个服务实例的流量限制。例如，设置单机阀值为 100 QPS，意味着该服务实例在单位时间内最多处理 100 个请求。
- **统计窗口时长**：用于统计流量的时间窗口，通常设置为 1 秒、5 秒或 1 分钟等。例如，设置为 10 秒表示在过去的 10 秒内统计请求数，以判断是否触发流量控制。
### 授权规则

授权规则用于控制哪些请求可以访问特定的服务或资源。Sentinel 允许开发者定义细粒度的授权规则，根据请求的来源、请求参数等进行流量控制。通过配置授权规则，可以实现对某些用户或请求的优先级管理，从而提高系统的安全性和稳定性。

- **流控应用**：指定哪些服务或资源需要应用授权规则。例如，可以针对某个特定的微服务或 API 接口配置流控规则，以确保只有符合条件的请求可以访问。
- **授权类型**：
    - **白名单**：允许访问的请求来源。可以根据 IP 地址、用户 ID 或其他标识符来设置白名单。只有在白名单中的请求才能成功访问被保护的资源。例如，设置特定的用户 ID（如 12345）为白名单，只有该用户的请求会被允许通过。
    - **黑名单**：禁止访问的请求来源。类似于白名单，可以根据 IP 地址、用户 ID 或其他标识符来设置黑名单。黑名单中的请求将被拒绝访问。例如，设置某些已知的恶意 IP 为黑名单，这些 IP 的请求将直接被拒绝。：
## 五、JWT鉴权使用

### 定义

JWT（JSON Web Token）是一种开放标准（RFC 7519），用于在网络应用环境中安全地传递信息。JWT 可以用于用户身份验证和信息交换，具有良好的安全性和传输效率。
### 作用

- **用户身份验证**：通过 JWT，用户登录后会得到一个 token，后续请求中携带该 token 以证明用户身份。
- **信息安全性**：JWT 可以通过数字签名（如 HMAC 或 RSA）确保信息未被篡改。
- **无状态**：JWT 是自包含的，服务器无需保存会话状态，减轻服务器负担

### 配置和使用

- 一般将其书写在一个公共模块里面，并且用静态方法进行设置对应的配置
```java
public class JWTutil {

    // 定义用户名常量
    private static final String UserName = "admin";

    // 定义JWT签名的密钥
    private static final String key = "token_key";

    // 定义token的过期时间，单位为毫秒（这里设置为1分钟）
    public static final int token_time_out = 1000 * 60;

    // 生成JWT token的方法
    public static String getToken(String userId) {
        // 创建JWT生成器
        JWTCreator.Builder jwtBuilder = JWT.create();

        // 创建头部信息的Map
        Map<String, Object> headers = new HashMap<>();

        // 设置头部的类型为JWT
        headers.put("type", "jwt");
        // 设置算法为HS256
        headers.put("alg", "hs256");

        // 生成token，包含头部、载荷和签名
        String token = jwtBuilder
            // 设置JWT的头部
            .withHeader(headers)
            // 添加自定义声明，包含用户ID
            .withClaim("userId", userId)
            // 设置token的过期时间
            .withExpiresAt(new Date(System.currentTimeMillis() + token_time_out))
            // 设置token的签发时间
            .withIssuedAt(new Date(System.currentTimeMillis()))
            // 设置token的发行者
            .withIssuer(UserName)
            // 使用指定的密钥和算法进行签名
            .sign(Algorithm.HMAC256(key));

        // 返回生成的token
        return token;
    }
}
```

- 使用的依赖：
```xml
  <!-- jwt -->
  <dependency>
      <groupId>com.auth0</groupId>
      <artifactId>java-jwt</artifactId>
  </dependency>
```

- 拦截器的设置：
```java
@Component
public class GlobalFilterConfig implements GlobalFilter, Ordered {
    //application.yml配置文件中，设置token在redis中的过期时间
    @Value("${config.redisTimeout}")
    private Long redisTimeout;

    @Autowired
    private RedisTemplate redisTemplate;

    private static final String HEADER_NAME = "Acess-Token";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("============过滤器============");

        // 获取请求对象
        ServerHttpRequest request = exchange.getRequest();
        // 获取响应对象
        ServerHttpResponse response = exchange.getResponse();
        // 获取请求地址
        String url = request.getURI().getPath();
        // 获取token信息
        String token = request.getHeaders().getFirst(HEADER_NAME);

        // 判断是否为白名单请求，以及一些内置不需要验证的请求。(登录请求也包含其中)。
        // 如果当前请求中包含token令牌不为空的时候，也会继续验证Token的合法性，这样就能保证
        // Token中的用户信息被业务接口正常访问到了。而如果当token为空的时候，白名单的接口可以
        // 被网关直接转发，无需登录验证。当然被转发的接口，也无法获取到用户的token身份数据了。
        if (this.shouldNotFilter(url)) {
            return chain.filter(exchange);
        }
        if (StringUtils.isEmpty(token)) {
            return unAuthorize(exchange);
        }
        //验证redis中是否存在token
        if (!redisTemplate.hasKey(token)) {
            return unAuthorize(exchange);
        }

        //验证通过，刷新token过期时间
        redisTemplate.expire(token, redisTimeout, TimeUnit.SECONDS);
        String userId = String.valueOf(redisTemplate.opsForValue().get(token));
        System.out.println("============登录用户id：" + userId + "============");
        //把新的 exchange放回到过滤链
        ServerHttpRequest newRequest = request.mutate().header(HEADER_NAME, token).build();
        ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
        return chain.filter(newExchange);

    }
    // 返回未登录的自定义错误
    private Mono<Void> unAuthorize(ServerWebExchange exchange) {
        // 设置错误状态码为401
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        // 设置返回的信息为JSON类型
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        // 自定义错误信息
        String errorMsg = "{\"error\": \"" + "用户未登录或登录超时,请重新登录" + "\"}";
        // 将自定义错误响应写入响应体
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(errorMsg.getBytes())));
    }
    /**
     * 判断当前请求URL是否为白名单地址，以及一些内置的不用登录的接口，
     * @param url 请求的url。
     * @return 是返回true，否返回false。
     */
    private boolean shouldNotFilter(String url) {
       if (url.startsWith("/user/login")) {
           return true;
       }
       return false;
    }
```
