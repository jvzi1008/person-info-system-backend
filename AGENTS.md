# 人员信息管理系统 — Boot 4 后端

> 本文件是给 AI 编码 Agent 的项目说明（AGENTS.md）。放在项目根目录，会话工作目录在项目内时自动加载。
> 最后更新：2026-11-13

## 这是什么

RuoYi-Vue 3.9.2 二次开发的人员信息比对系统，后端已从 Spring Boot 2.5.15 迁移到 **4.0.3**。

- 同目录关系：`D:\AAAprojects\RuoYi-Vue` 是 Boot 2.5.15 旧版（基线，勿随意改）；本目录 `D:\AAAprojects\RuoYi-Vue4` 是 Boot 4 新版。
- 前端：`D:\AAAprojects\RuoYi-Vue2`（Vue2 + Element UI），**两套后端共用，不需要改**。
- 业务核心：外部用户（邮箱注册）+ IP 白名单 + 人员信息比对 + 访问日志。

## 技术栈

| 项 | 版本 |
|---|---|
| Spring Boot / Spring / Security | 4.0.3 / 7.0.5 / 7.x |
| JDK（编译+运行） | 17+，本机用 `D:\jdk21\jdk-21.0.2` |
| Tomcat | 11.0.18（内嵌） |
| MyBatis Starter / PageHelper | 4.0.1 / 2.1.1 |
| Druid | `druid-spring-boot-4-starter` 1.2.28 |
| 接口文档 | springdoc 3.0.2（`/v3/api-docs`、`/swagger-ui/index.html`） |
| Jackson | Jackson 3（`tools.jackson`），注解仍用 `com.fasterxml.jackson.annotation` |
| MySQL / Redis | 8.0.39 / 6379 |

## 目录与关键文件

```
com.ruoyi.compare                     业务包（ruoyi-system）
  domain / dto / vo / mapper / service / impl / utils / exception
ruoyi-system/src/main/resources/mapper/compare/*.xml
ruoyi-admin/.../web/controller/compare/    Controller（ExtAuth/ExtCompare/UnifiedLogin/BizIpUser/BizIpAuth/BizCompareAccessLog/BizCompare）
ruoyi-admin/.../web/interceptor/ExtAuthInterceptor.java   外部接口鉴权
ruoyi-admin/.../web/config/ExtWebMvcConfig.java
ruoyi-framework/.../config/SecurityConfig.java            Spring Security 7 配置
ruoyi-framework/.../config/DruidConfig.java               Boot 4 包名
ruoyi-admin/src/main/resources/application.yml            spring.data.redis / springdoc / jackson
ruoyi-admin/src/main/resources/application-druid.yml      table_import 连接
```

业务规则主要在 `CompareServiceImpl`：状态集合、学校关键词、任一否即否、姓名一致性、`queryStatus/reviewReasons/inSchoolStatus/targetStudentStatus` 与兼容布尔字段。

## 构建与运行

```powershell
# 构建（必须 JDK17+；clean 前先停掉正在跑的 jar，否则 target 被占用）
$env:JAVA_HOME = 'D:\jdk21\jdk-21.0.2'
D:\apache-maven-3.9.6\bin\mvn.cmd clean package -DskipTests

# 运行（8080 与前端约定一致；并存调试用 8081）
D:\jdk21\jdk-21.0.2\bin\java.exe -jar ruoyi-admin\target\ruoyi-admin.jar
D:\jdk21\jdk-21.0.2\bin\java.exe -jar ruoyi-admin\target\ruoyi-admin.jar --server.port=8081
```

依赖：MySQL 3306（库 `table_import`）、Redis 6379。`D:\JDK`（1.8）**不能**运行本 jar。

## 前后端联调

- 前端开发服 82，代理 `/dev-api` → `http://localhost:8080`（`vue.config.js` 写死 baseUrl）。
- 统一登录页 `/login`，管理员进 `/index`，外部用户进 `/ext/compare`。
- 浏览器 history 模式；非浏览器请求需带 `Accept: text/html` 才会回退 index.html。

## 账号与接口

| 用途 | 值 |
|---|---|
| 管理员 | `admin` / `admin123`（登录要验证码） |
| 普通用户测试账号 | `test001` / `Test1234` |
| 统一登录 | `POST /login`（所有人同一套 JWT，`Admin-Token`） |
| 注册 | `POST /register`（邮箱+密码+验证码，注册后 `audit_status=0` 待审核） |
| 审核 | `PUT /system/user/audit`（`system:user:audit`，1通过 2驳回） |
| 人员比对 | `POST /compare/person`（普通用户与管理员共用，`compare:person:query`） |
| 我的 IP | `GET/POST /compare/my/ip`、`DELETE /compare/my/ip/{id}`（仅本人） |
| 我的登录日志 | `GET /compare/my/loginLog`（仅本人，精确匹配；前端 `/ext/loginLog`） |
| 日志导出 | `POST /compare/accessLog/export` |

> 外部 `X-Ext-Token` 与 `/api/ext/**` 已删除；普通用户角色 `normal` 只能访问 `/ext/*` 与比对接口。

## 改动流程（必须遵守）

1. 改前端页面 → 改/加接口 → 需要时加库表与菜单 SQL → 后端 `clean package` → 前端 `npm run build:prod` → 冒烟 → 更新执行记录。
2. 权限串改动要同步 `sys_menu`（3000–3015，目前只授权 role_id=1）。
3. 不要修改 `application-druid.yml` 里的库名；不要清空 `table_import`。
4. 改完把结果追加到 `C:\Users\Administrator\OneDrive\Desktop\人员信息比对平台_执行记录.txt`。

## Boot 4 迁移注意（踩过的坑）

- `spring-boot-starter-web` → `spring-boot-starter-webmvc`；`starter-aop` → `starter-aspectj`。
- 必须保留 `spring-boot-jackson`（否则无 `ObjectMapper` Bean）与 `spring-boot-starter-cache`（否则 `@EnableCaching` 报无 `CacheManager`）。
- `DataSourceAutoConfiguration` 在 `org.springframework.boot.jdbc.autoconfigure`。
- Security 7 用 `requestMatchers`；`LoginUser.getAuthorities()` 不能返回 null。
- 注入 `ObjectMapper` 要用 `tools.jackson.databind.ObjectMapper`。
- `spring.redis.*` 已改为 `spring.data.redis.*`。
- `SensitiveJsonSerializer` 仍是 Jackson 2 死代码（无字段使用），不要据此判断脱敏生效。

## 冒烟清单

```powershell
# 外部登录 + 比对
$b = @{loginName='test001';password='Test1234'} | ConvertTo-Json -Compress
$r = Invoke-RestMethod -Uri 'http://127.0.0.1:8080/api/ext/auth/login' -Method Post -ContentType 'application/json; charset=utf-8' -Body ([Text.Encoding]::UTF8.GetBytes($b))
$h = @{'X-Ext-Token'=$r.token}
Invoke-RestMethod -Uri 'http://127.0.0.1:8080/api/ext/compare/person' -Method Post -Headers $h -ContentType 'application/json; charset=utf-8' -Body ([Text.Encoding]::UTF8.GetBytes('{"idCardNo":"511781200911035540","name":"黄忆琳"}'))
```

预期：`queryStatus=MATCHED`、`inSchoolStatus=YES`、`studentType=VOCATIONAL`。
管理员登录验证码可从 Redis 取：`captcha_codes:<uuid>`（uuid 来自 `/captchaImage`）。

## 相关文档

- 迁移说明：`SPRING-BOOT4-迁移说明.md`（本目录）
- 架构梳理：`C:\Users\Administrator\OneDrive\文档\deepseek-harness\default-workspace\人员信息管理系统_架构梳理_20261113.md`
- 执行记录：`C:\Users\Administrator\OneDrive\Desktop\人员信息比对平台_执行记录.txt`
- 启动方法：`C:\Users\Administrator\OneDrive\Desktop\人员信息管理系统_启动方法.txt`
