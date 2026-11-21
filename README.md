# 人员信息管理系统 — 后端

> 基于 RuoYi-Vue 3.9.2 二次开发的人员信息比对系统后端服务
> Spring Boot 4.0.3 · JDK 17 · MyBatis · MySQL 8 · Redis

---

## 项目简介

面向教育管理场景的人员信息核验服务。核心能力：

- **人员信息比对**：按证件号查询在校状态、学生类型、转学记录
- **统一身份**：管理员与普通用户同在 `sys_user`，通过角色区分权限
- **注册审核**：邮箱注册 → 管理员审核 → 方可登录
- **IP 白名单**：每个用户限定可访问的 IP（支持 CIDR），自助添加需审核
- **访问留痕**：每次比对写入日志，支持查询、导出、自动归档

---

## 技术栈

| 组件 | 版本 |
|---|---|
| Spring Boot | 4.0.3 |
| Spring Framework | 7.0.x |
| Spring Security | 7.x |
| 内嵌 Tomcat | 11.x |
| JDK | 17+（本机用 JDK21） |
| MyBatis Spring Boot Starter | 4.0.1 |
| PageHelper | 2.1.1 |
| Druid | druid-spring-boot-4-starter 1.2.28 |
| 接口文档 | springdoc 3.0.2（`/v3/api-docs`） |
| Jackson | 3（`tools.jackson`） |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 6+ |

> ⚠️ Spring Boot 4 需要 JDK 17 或更高版本，**不能用 JDK 8**。

---

## 模块结构

```
ruoyi-admin       启动模块 + Controller（含 web/controller/compare）
ruoyi-framework   Security 配置、JWT、登录服务、限流切面
ruoyi-system      业务 Service/Mapper，核心包 com.ruoyi.compare
ruoyi-common      通用工具、实体、注解
ruoyi-quartz      定时任务（Quartz）
ruoyi-generator   代码生成
```

人员比对业务集中在 `ruoyi-system/src/main/java/com/ruoyi/compare`：

```
domain/          实体（BizIpAuth、BizCompareAccessLog）
domain/dto       入参（CompareQueryBody、ExtAddIpBody）
domain/vo        出参（CompareResult、StudentResultRow、TransferRow）
mapper/          Mapper 接口
service/         业务实现
  impl/CompareServiceImpl   比对规则
  MyIpService               我的 IP
  NormalUserLoginService    首登绑定 IP
  CompareLogArchiveService  日志归档
utils/ExtIpUtils 客户端 IP 与 CIDR 匹配
```

---

## 快速开始

### 1. 环境准备

- JDK 17+
- Maven 3.9+
- MySQL 8
- Redis 6+

### 2. 建库并导入脚本

```sql
CREATE DATABASE table_import DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;
```

在 `sql/` 目录按顺序导入：

| 顺序 | 文件 | 说明 |
|---|---|---|
| 1 | `ry_20260417.sql` | 若依基础表 |
| 2 | `quartz.sql` | 定时任务表 |
| 3 | `p1_identity_unify.sql` | 用户审核字段、普通用户角色 |
| 4 | `p1_migrate_biz_users.sql` | 存量账号迁移（新库可跳过） |
| 5 | `p4_log_archive.sql` | 访问日志归档表 |
| 6 | `p5_menu_adjust.sql` | 菜单顺序 + 用户审核入口 |
| 7 | `p5_test_dataset.sql` | 测试表结构（可选） |
| 8 | `p5_test_data.sql` | 20 名虚拟学生（可选） |

### 3. 修改配置

`ruoyi-admin/src/main/resources/application-druid.yml`：

```yaml
url: jdbc:mysql://localhost:3306/table_import?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
username: root
password: 你的密码
```

`application.yml` 中确认 Redis 地址与端口。

### 4. 编译运行

```powershell
mvn clean package -DskipTests
java -jar ruoyi-admin/target/ruoyi-admin.jar
```

默认端口 8080。

> 修改 `ruoyi-system` / `ruoyi-framework` 后必须 `clean package`，
> 只执行 `package` 会导致 jar 内嵌子模块未更新。

---

## 关键配置

```yaml
compare:
  # 数据源：prod=正式表，test=测试表
  dataset: prod
  # 仅有转学记录、无学生结果时是否视为在读
  transfer-as-inschool: true
  log:
    enabled: true
    retention-days: 90
    archive-cron: 0 30 3 * * ?
```

用测试数据启动：

```powershell
java -jar ruoyi-admin/target/ruoyi-admin.jar --compare.dataset=test
```

---

## 接口概览

| 接口 | 权限 | 说明 |
|---|---|---|
| `POST /login` | 匿名 | 登录（需验证码） |
| `POST /register` | 匿名 | 邮箱注册（注册后待审核） |
| `POST /compare/person` | `compare:person:query` | 人员信息比对（限流 30 次/分钟/用户） |
| `GET /compare/my/ip` | 登录 | 我的 IP 列表 |
| `POST /compare/my/ip` | 登录 | 添加 IP（待审核） |
| `GET /compare/my/loginLog` | 登录 | 我的登录日志 |
| `PUT /system/user/audit` | `system:user:audit` | 用户审核 |
| `PUT /compare/ipAuth/audit` | `compare:ipAuth:edit` | IP 审核 |
| `POST /compare/accessLog/export` | `compare:accessLog:export` | 导出访问日志 |

---

## 默认账号

| 账号 | 密码 | 说明 |
|---|---|---|
| admin | admin123 | 管理员 |
| test001 | Test1234 | 普通用户（已通过审核） |

---

## 文档

| 文档 | 说明 |
|---|---|
| `SPRING-BOOT4-迁移说明.md` | Boot 2.5 → 4.0 的完整迁移记录 |
| `P1_身份统一_实施记录.md` | 用户并入 sys_user、统一 JWT |
| `P2_IP审核_实施记录.md` | IP 授权审核流程 |
| `P3_登录日志与清理_实施记录.md` | 我的登录日志、清理旧代码 |
| `P4_安全加固与归档_实施记录.md` | 限流、改密校验、日志归档 |
| `P5_逻辑修正与菜单_实施记录.md` | 比对逻辑修正、测试数据集、菜单 |
| `AGENTS.md` | 开发约定与踩坑记录 |

---

## 说明

- 本项目基于 [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue)（MIT）二次开发，
  保留了其用户、角色、菜单、日志等基础能力。
- 数据库密码等敏感信息请勿提交到公开仓库，正式部署请使用独立账号。
- 前端仓库：`person-info-system-frontend`
