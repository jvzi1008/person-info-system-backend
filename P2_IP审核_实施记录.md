# P2 实施记录：IP 审核 + 密码提示修复

> 日期：2026-11-19
> 前置：P1（身份统一）已完成

## 一、本次解决的问题

| 反馈 | 原因 | 处理 |
|---|---|---|
| 普通用户首次登录提示「密码还是初始密码」 | 迁移账号的 `pwd_update_date` 为空，触发 `sys.account.initPasswordModify=1` 的初始密码提醒 | 回填 `pwd_update_date`，迁移脚本同步补 |
| 提示改密码但没有修改入口 | 普通用户被限制在 `/ext/*`，进不了个人中心 | 外部布局顶栏新增「修改密码」弹窗（调 `/system/user/profile/updatePwd`） |
| 用户添加 IP 不审核就直接生效 | P2 尚未实施 | 自助添加改为待审核；管理端增加审核入口 |
| 管理员首页看不到管理/日志菜单 | 接口 `getRouters` 返回正常，当前浏览器实测菜单齐全；截图页面是旧缓存/折叠状态 | 硬刷新（Ctrl+F5）即可；无需改代码 |

## 二、IP 审核实现

**状态机**：用户提交 → `audit_status=0`（待审，不生效）→ 管理员通过 `audit_status=1`（生效）/ 驳回 `audit_status=2`（停用）。

- 用户自助添加：`audit_status=0`、`status=0`、`audit_remark=用户自助添加，待管理员审核`；待审上限 3 条；有效 IP 上限仍为 10 条（只统计已通过）。
- 首次登录自动绑定：仍免审（`audit_status=1`、`audit_by=system`）。
- 管理员新增：默认 `audit_status=1`，`audit_by` 为当前管理员。
- 生效判定：`status=0 AND audit_status=1`（`isIpAuthorized` / `selectActiveByUserId` 已按此过滤）。
- 新增接口：`PUT /compare/ipAuth/audit`（`compare:ipAuth:edit`），通过时置 `status=0`，驳回时置 `status=1` 并要求填写原因。

**管理端页面**：IP 授权页新增「审核状态」筛选与列、「审核」按钮与通过/驳回弹窗；用户下拉改读 `/compare/ipAuth/userOptions`（`sys_user` 已通过审核的普通用户）。

**用户端页面**：状态列展示「待审核 / 已驳回 / 有效 / 停用」；添加成功提示改为「IP已提交，待管理员审核」。

## 三、验证结果

| 场景 | 结果 |
|---|---|
| test001 添加 127.0.0.2 | 返回「IP已提交，待管理员审核」，列表 `audit_status=0` |
| 管理端待审核筛选 | 命中 1 条 |
| 管理员通过 | `audit_status=1`、`status=0`，用户端显示有效 |
| 管理员驳回（填原因） | `audit_status=2`、`status=1`，用户端显示已驳回 |
| 管理员新增 IP | 默认 `audit_status=1` |
| test001 `/getInfo` | `isDefaultModifyPwd=false`、`isPasswordExpired=false`，不再弹初始密码 |
| 普通用户顶栏「修改密码」 | 弹窗正常渲染（原密码/新密码/确认密码） |
| 管理端 IP 页用户下拉 | 显示 `test001` 等普通用户，不再读 `biz_ip_user` |
| 前端 build:prod / 后端编译 | 均通过 |

## 四、待办

- P3：删除 `biz_ip_user` 与旧「接入用户」代码、我的登录日志页面、注册/登录限流。
