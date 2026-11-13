# Spring Boot 4 迁移说明

> 生成日期：2026-11-13
> 新工程：`D:\AAAprojects\RuoYi-Vue4`（原工程 `D:\AAAprojects\RuoYi-Vue` 保持不动）
> 目标：与朋友那套「RuoYi 3.9.2 + Spring Boot 4.0.3」对齐，便于后续交换/合并代码

---

## 一、版本矩阵

| 组件 | 原（Boot 2.5.15） | 新（Boot 4.0.3） |
|---|---|---|
| Spring Boot | 2.5.15 | **4.0.3** |
| Spring Framework | 5.3.39（手工覆盖） | 7.0.5（由 Boot 管理） |
| Spring Security | 5.7.14（手工覆盖） | 7.0.x（由 Boot 管理） |
| 内嵌 Tomcat | 9.0.112 | 11.0.18 |
| JDK / 字节码 | 1.8 | **17**（用 JDK21 编译/运行也可以） |
| Servlet API | javax.servlet | **jakarta.servlet** |
| MyBatis Starter | 经 pagehelper 传递（2.x） | **mybatis-spring-boot-starter 4.0.1** |
| PageHelper | 1.4.7 | **2.1.1** |
| Druid | druid-spring-boot-starter | **druid-spring-boot-4-starter 1.2.28** |
| 接口文档 | springfox 3.0.0 | **springdoc-openapi 3.0.2** |
| Jackson | Jackson 2 | **Jackson 3（tools.jackson）+ 兼容注解** |
| MySQL 驱动 | mysql-connector-java | **mysql-connector-j 9.6.0** |
| Quartz | quartz 2.3.2 | **Spring Boot starter-quartz（2.5.2）** |
| JWT | jjwt 0.9.1 | jjwt 0.9.1 + jaxb-api 2.3.1（保持代码不变） |

---

## 二、为什么不是"改一个版本号"

Boot 2.5 → 4.0 跨过了 Boot 3 的大断代，有 4 个硬性不兼容点：

1. **javax → jakarta**：所有 `javax.servlet` / `javax.validation` / `javax.annotation` 必须换成 `jakarta.*`（本工程 62 个文件）。
2. **Spring Security 7**：`antMatchers` 已被删除，必须用 `requestMatchers`；`DaoAuthenticationProvider` 无参构造被移除，`AuthenticationManager` 改为从 `AuthenticationConfiguration` 获取。
3. **Jackson 3**：Boot 4 默认注入的是 `tools.jackson.databind.ObjectMapper`，不再是 `com.fasterxml.jackson.databind.ObjectMapper`。
4. **Boot 4 模块化拆分**：starter 改名/拆分，自动配置变成独立模块，必须显式引入：
   - `spring-boot-starter-web` → `spring-boot-starter-webmvc`
   - `spring-boot-starter-aop` → `spring-boot-starter-aspectj`
   - Jackson 自动配置 → `spring-boot-jackson`
   - Cache 自动配置 → `spring-boot-starter-cache`
   - `DataSourceAutoConfiguration` 包路径 → `org.springframework.boot.jdbc.autoconfigure`

---

## 三、本次实际改动清单

### 1. POM

| 文件 | 改动 |
|---|---|
| `pom.xml` | java 17、boot 4.0.3、删除 spring-framework/security/tomcat/logback 手工覆盖；新增 mybatis 4.0.1、pagehelper 2.1.1、druid-4 starter、springdoc 3.0.2、jaxb-api；compiler 插件升到 3.13.0 并加 `<parameters>true</parameters>` |
| `ruoyi-framework/pom.xml` | webmvc / aspectj / druid-spring-boot-4-starter |
| `ruoyi-admin/pom.xml` | 移除 springfox + swagger-models，改为 springdoc；mysql 驱动改 `com.mysql:mysql-connector-j` |
| `ruoyi-common/pom.xml` | `javax.servlet-api` → `jakarta.servlet-api`；新增 `spring-boot-jackson`、`spring-boot-starter-cache` |
| `ruoyi-system/pom.xml` | 显式声明 mybatis-spring-boot-starter 4.0.1 |
| `ruoyi-quartz/pom.xml` | 直接依赖 quartz → `spring-boot-starter-quartz` |
| `ruoyi-generator/pom.xml` | druid-spring-boot-4-starter |

### 2. 代码

| 文件 | 改动 |
|---|---|
| 62 个 Java 文件 | `javax.servlet/validation/annotation` → `jakarta.*`（`javax.sql`、`javax.imageio`、`javax.net.ssl` 保持不变） |
| `SecurityConfig` | `requestMatchers` 重写；`AuthenticationManager` 改由 `AuthenticationConfiguration` 提供；保留 `/api/unified/**`、`/api/ext/**` 白名单 |
| `SwaggerConfig` | springfox Docket → springdoc `OpenAPI` + `SecurityScheme` |
| `TestController` | swagger2 注解 → `@Tag/@Operation/@Schema` |
| `ApplicationConfig` | 删除 `Jackson2ObjectMapperBuilderCustomizer`（Boot 4 已移包），时区改由 yml 配置 |
| `DruidConfig` | 包名改 `com.alibaba.druid.spring.boot4.autoconfigure.*`；`javax.servlet.FilterConfig` → jakarta |
| `LoginUser.getAuthorities()` | 由 `return null` 改为返回空集合（Security 7 会对 null 调 `size()` 直接 NPE） |
| `ExtAuthInterceptor`、`CompareServiceImpl` | 注入 `tools.jackson.databind.ObjectMapper`（Jackson 3） |
| `RuoYiApplication` | `DataSourceAutoConfiguration` 改到 `org.springframework.boot.jdbc.autoconfigure` |

### 3. 配置

| 文件 | 改动 |
|---|---|
| `application.yml` | `spring.redis` → `spring.data.redis`；删除 `swagger.*`，新增 `springdoc.*`；新增 `spring.jackson.time-zone/date-format` |
| `application-druid.yml` | 无需改动（连接串、账号密码原样保留） |

---

## 四、构建与运行

```powershell
# 必须用 JDK17+，本机用 D:\jdk21
$env:JAVA_HOME = 'D:\jdk21\jdk-21.0.2'

cd D:\AAAprojects\RuoYi-Vue4
D:\apache-maven-3.9.6\bin\mvn.cmd clean package -DskipTests

# 默认 8080（与旧版一致，前端无需改动）
D:\jdk21\jdk-21.0.2\bin\java.exe -jar ruoyi-admin\target\ruoyi-admin.jar

# 与旧版并存调试时换端口
D:\jdk21\jdk-21.0.2\bin\java.exe -jar ruoyi-admin\target\ruoyi-admin.jar --server.port=8081
```

> 旧的 `D:\JDK`（1.8）**不能运行**新 jar，只能用于旧工程。

---

## 五、验证结果（2026-11-13 实机）

| 项目 | 结果 |
|---|---|
| 启动 | Boot 4.0.3 / Spring 7.0.5 / Tomcat 11.0.18 / JDK21，11.3s 启动成功 |
| 外部登录 | `test001/Test1234` → 200 |
| 外部比对 | 黄忆琳 → MATCHED/YES/YES/VOCATIONAL；郭梦璐 → TRANSFER_ONLY/match=2 |
| 无 Token | 401 |
| 管理员统一登录 | 验证码（Redis 取值）→ type=admin，JWT 签发正常 |
| 管理员比对 | `/compare/person` → 200 MATCHED |
| 管理端列表 | ipUser=3、ipAuth=3、accessLog=43 |
| 权限 | `/getInfo` → roles=admin、permissions=1（`*:*:*`） |
| Excel 导出 | `/compare/accessLog/export` → xlsx 8291 字节 |
| 接口文档 | `/v3/api-docs` 200、`/swagger-ui/index.html` 200 |
| 退出 | `/logout` 200 |
| 访问日志 | 新增 3 条，中文/脱敏/结果码均正常 |

---

## 六、已知差异与后续建议

1. **Jackson 2 仍在 classpath**：`common` 里的 `SensitiveJsonSerializer`（`@Sensitive` 脱敏）还是 Jackson 2 实现，且当前工程**没有任何字段使用 `@Sensitive`**，属于死代码。建议后续要么迁移到 Jackson 3，要么直接删除，避免和 Boot 4 的 Jackson 3 混用。
2. **接口文档**：`/swagger-ui/index.html` 由 springdoc 提供，前端「系统工具 → 接口文档」的 iframe 地址正好可用，无需改前端。
3. **前端零改动**：接口路径、JSON 结构、Token 机制都没变，`RuoYi-Vue2` 直接对接新后端即可。
4. **`spring-boot-jackson` 与 `spring-boot-starter-cache` 必须显式保留**，删掉会分别报 `ObjectMapper` 缺失、`CacheManager` 缺失。
5. **JWT 依赖 jaxb-api**：jjwt 0.9.1 在 JDK17+ 需要 `javax.xml.bind`，已加入 `ruoyi-common`；后端已实测登录/鉴权正常。
6. 生产运行请用 JDK17/21，别再用 8。

---

## 七、目录与共存

```
D:\AAAprojects\RuoYi-Vue     旧：Boot 2.5.15 / JDK8，8080（保持可用，可回滚）
D:\AAAprojects\RuoYi-Vue4    新：Boot 4.0.3 / JDK17+，默认 8080；并存时用 --server.port=8081
D:\AAAprojects\RuoYi-Vue2    前端：两套后端共用，不需要改
```

切换时只需：停旧后端 → 用 JDK21 启动新 jar（8080）→ 前端照常访问。
