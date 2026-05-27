# API 接口与数据库表清单（仅扫描 src/ 与 frontend/）

> 扫描范围：
> - 后端：`d:/CS/SASS/project-new/--main/src/`
> - 前端：`d:/CS/SASS/project-new/--main/frontend/`
>
> 说明：
> - “API 接口”来自后端 Spring `@RestController` 的 `@RequestMapping/@GetMapping/...` 端点。
> - “数据库表”来自 JPA `@Entity/@Table` + Flyway 迁移 SQL `CREATE TABLE`。
> - “存储啥”按实体/表字段语义做归纳（不是逐列字典）。

---

## API 接口（后端）

### 认证 Auth（/api/v1/auth）
- [AuthController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/auth/controller/AuthController.java#L22-L53)
  - `POST /api/v1/auth/login`：账号密码登录，返回登录态/Token
  - `POST /api/v1/auth/wechat-login`：微信登录
  - `GET /api/v1/auth/me`：获取当前用户信息
  - `POST /api/v1/auth/change-password`：修改密码
  - `POST /api/v1/auth/logout`：退出登录（记录注销 token 等）

### 平台通用 Platform（/api/v1/platform）
- [PlatformController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/platform/controller/PlatformController.java#L71-L566)
  - 契约/权限/范围
    - `GET /contracts`：前后端契约（枚举/字典/能力清单）
    - `GET /student-ui-contract`：学生端 UI 元信息契约
    - `GET /users/me/permissions`：当前用户权限快照
    - `GET /users/me/student-scope`：当前用户可访问学生范围
    - `GET /users/me/student-scope/check-student`：校验是否可访问某学生
    - `GET /users/me/student-scope/check-range`：校验是否可访问某范围（年级/班级等）
    - `GET /roles`：角色列表
  - 安全/上传策略
    - `GET /security-policy`：获取安全策略
    - `PUT /security-policy`：更新安全策略
    - `GET /upload-policy`：获取上传治理策略
    - `PUT /upload-policy`：更新上传治理策略
  - 用户管理
    - `GET /users`：用户列表筛选
    - `GET /users/page`：用户分页
    - `GET /users/{userId}`：用户详情
    - `POST /users`：创建用户
    - `PUT /users/{userId}`：更新用户
    - `POST /users/{userId}/enabled`：启用/禁用
    - `POST /users/{userId}/unlock`：解锁
    - `POST /users/{userId}/reset-password`：重置密码
    - `POST /users/{userId}/wechat/unbind`：解绑微信
    - `GET /users/stats`：用户统计
  - 会话管理
    - `GET /sessions/page`：会话分页
    - `POST /sessions/{sessionId}/revoke`：强制下线会话
  - 学生检索
    - `GET /students/page`：学生分页检索
    - `GET /students/{studentId}`：学生详情
  - 文件上传/下载
    - `POST /files/upload`：上传文件并绑定业务
    - `GET /files/page`：上传记录分页
    - `POST /files/{id}/archive`：归档上传记录
    - `DELETE /files/{id}`：删除上传记录
    - `GET /files/{id}/download`：下载上传文件
  - 数据导入（平台侧）
    - `POST /import-tasks`：创建导入任务
    - `PUT /import-tasks/{taskId}`：更新导入任务状态
    - `GET /import-tasks/page`：导入任务分页
    - `GET /import-tasks/{taskId}/errors/page`：导入错误分页
    - `POST /import-tasks/{taskId}/errors`：登记导入错误
    - `POST /import-tasks/{taskId}/execution-result`：回填导入执行结果
    - `GET /import-tasks/{taskId}/receipt`：导入回执
  - 审计/通知发送（平台侧）
    - `GET /audit/admin-operation-logs/page`：操作日志分页
    - `GET /audit/login-logs/page`：登录日志分页
    - `GET /audit/approval-logs/{requestId}`：审批历史
    - `POST /notifications/send`：发送通知并生成发送记录
    - `GET /notifications/send-records`：通知发送记录列表
    - `GET /notifications/send-records/page`：通知发送记录分页
  - Excel 导入导出
    - `POST /users/import`：导入用户 Excel
    - `GET /users/export`：导出用户 Excel
    - `GET /users/stats/export`：导出用户统计 Excel
    - `POST /students/import`：导入学生 Excel
    - `GET /students/export`：导出学生 Excel
    - `POST /students/award-support/import`：导入学生奖助记录

### 学生端 Student（/api/v1/student）
- [StudentSelfController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/student/controller/StudentSelfController.java#L48-L265)
  - `GET /me`：本人学生档案
  - `GET /dashboard`：学生端首页数据
  - `GET /growth-suggestions`：成长建议
  - `GET /notices`：我的通知
  - `GET /certificates/requests`：我的证明申请
  - `GET /party-progress`：我的入党进度
  - `GET /party-progress/reminders`：我的入党提醒
  - `GET /knowledge/recommended`：推荐知识条目
  - `GET /policies/page`：政策分页（知识库政策）
  - `POST /qa-tickets`：创建问答工单
  - `GET /qa-tickets/page`：分页查看我的工单
  - `GET /qa-tickets/{id}`：工单详情（含消息）
- [StudentGrowthController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/student/controller/StudentGrowthController.java#L24-L71)
  - `GET /api/v1/student/growth/modules`：成长模块列表
  - `GET /api/v1/student/growth/archive`：本人成长档案汇总
  - `GET /api/v1/student/growth/{moduleCode}/records`：某模块记录列表
  - `GET /api/v1/student/growth/{moduleCode}/records/{id}`：记录详情
  - `POST /api/v1/student/growth/{moduleCode}/records`：新增记录
  - `PUT /api/v1/student/growth/{moduleCode}/records/{id}`：更新记录
  - `DELETE /api/v1/student/growth/{moduleCode}/records/{id}`：删除记录

### 知识库 Knowledge（/api/v1/knowledge）
- [KnowledgeController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/knowledge/controller/KnowledgeController.java#L18-L40)
  - `GET /api/v1/knowledge/search`：关键词搜索知识
  - `GET /api/v1/knowledge/templates`：列出模板型知识条目
  - `GET /api/v1/knowledge/{id}`：知识条目详情
- [TemplateDownloadController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/knowledge/controller/TemplateDownloadController.java#L34-L59)
  - `GET /templates/{fileName}`：下载内置模板（兼容路径）
  - `GET /api/v1/templates/{fileName}`：下载内置模板

### 通知 Notice（/api/v1/notices）
- [NoticeController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/notice/controller/NoticeController.java#L19-L53)
  - `GET /api/v1/notices/student/{studentId}`：某学生的定向通知列表
  - `POST /api/v1/notices/{noticeId}/read/student/{studentId}`：标记已读
  - `POST /api/v1/notices/read-all/student/{studentId}`：全部标记已读
  - `GET /api/v1/notices/unread-count/student/{studentId}`：未读数

### 证明申请/模板 Certificates
- [CertificateController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/certificate/controller/CertificateController.java#L26-L74)（/api/v1/certificates）
  - `GET /types`：可申请证明类型
  - `POST /requests`：提交证明申请
  - `GET /requests/student/{studentId}`：学生查看自己的申请列表
  - `GET /requests/{requestId}/history`：申请审批历史
  - `GET /requests/{requestId}/preview`：预览生成内容
  - `POST /requests/{requestId}/action`：学生侧动作（撤回/补充等）
- [CertificateTemplateController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/certificate/controller/CertificateTemplateController.java#L32-L173)（/api/v1/certificate-templates）
  - `GET /`：模板列表（管理端）
  - `GET /active`：启用模板列表（学生/老师可见）
  - `GET /type/{type}`：按类型筛选
  - `GET /{id}`：模板详情
  - `GET /{id}/download`：下载模板文件
  - `GET /code/{code}`：按 code 查询
  - `POST /`：创建模板
  - `PUT /{id}`：更新模板
  - `DELETE /{id}`：删除模板
  - `GET /{id}/preview/student/{studentId}`：按学生渲染预览
- [CertificateExportController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/certificate/controller/CertificateExportController.java#L37-L55)
  - `GET /exports/certificates/{id}.pdf`：预览导出 PDF（示例导出）

### 审批（后台）Approvals（/api/v1/admin/approvals）
- [AdminApprovalController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/admin/controller/AdminApprovalController.java#L30-L109)
  - `GET /`：审批任务列表
  - `GET /page`：审批任务分页
  - `GET /stats`：审批任务统计
  - `GET /{requestId}/history`：审批历史
  - `POST /{requestId}/action`：审批动作（通过/驳回）
  - `POST /{requestId}/assign`：转派审批（mock）
  - `POST /seed`：生成模拟审批任务（mock）

### 工作记录 Worklog（/api/v1/worklogs）
- [StudentWorkLogController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/worklog/controller/StudentWorkLogController.java#L35-L135)
  - `POST /`：创建工作记录
  - `PUT /{id}`：更新工作记录
  - `DELETE /{id}`：删除工作记录
  - `GET /student/{studentId}`：按学生列出记录
  - `GET /student/{studentId}/summary`：工作量汇总
  - `GET /{id}/actions`：操作流水
  - `GET /overview`：管理员总览
  - `GET /admin/filter`：后台筛选列表
  - `GET /admin/stats`：后台统计
  - `GET /admin/page`：后台分页
  - `GET /admin/export-metadata`：导出字段元数据

### 学业 Academic（/api/v1/academic）
- [AcademicProgramController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/academic/controller/AcademicProgramController.java#L17-L90)（/api/v1/academic/programs）
  - `GET /`：培养方案列表
  - `GET /{id}`：培养方案详情
  - `POST /`：创建培养方案
  - `PUT /{id}`：更新培养方案
  - `DELETE /{id}`：删除培养方案
  - `POST /{id}/modules`：添加模块
  - `DELETE /modules/{moduleId}`：删除模块
  - `POST /transcripts/student/{studentId}`：上传成绩单
  - `GET /transcripts/student/{studentId}`：获取成绩单
  - `GET /audit-report/student/{studentId}/program/{programId}`：培养方案审核报告
- [AcademicWarningController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/academic/controller/AcademicWarningController.java#L16-L29)
  - `GET /api/v1/academic/analysis/{studentId}`：学业预警分析

### 党团/入党 Party
- [PartyProgressController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/party/controller/PartyProgressController.java#L20-L45)（/api/v1/party-progress）
  - `GET /{studentId}`：入党进度概览
  - `GET /{studentId}/timeline`：阶段时间线
  - `GET /{studentId}/reminders`：提醒列表
- [PartyFlowController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/party/controller/PartyFlowController.java#L16-L54)（/api/v1/party/flows）
  - `GET /`：流程模板列表
  - `GET /{id}`：流程模板详情
  - `POST /`：创建流程模板
  - `POST /{id}/stages`：添加阶段
  - `DELETE /{id}`：删除模板
- [AdminPartyFlowConfigController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/party/controller/AdminPartyFlowConfigController.java#L27-L101)（/api/v1/admin/party-flows）
  - `GET /`：后台流程列表
  - `POST /`：创建流程
  - `PUT /{id}`：更新流程
  - `POST /{id}/copy`：复制流程
  - `DELETE /{id}`：删除流程
  - `GET /{id}/nodes`：流程节点列表
  - `POST /{id}/nodes`：新增节点
  - `PUT /nodes/{nodeId}`：更新节点
  - `POST /nodes/{nodeId}/move`：节点上移/下移
  - `DELETE /nodes/{nodeId}`：删除节点
- [PartyMaterialController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/party/controller/PartyMaterialController.java#L17-L93)（/api/v1/party-materials）
  - `POST /`：提交入党材料
  - `GET /student/{studentId}`：查询某学生材料提交记录
  - `GET /pending`：待审核材料列表
  - `POST /{id}/review`：审核材料
  - `POST /{id}/withdraw`：撤回提交
  - `POST /{id}/resubmit`：重新提交
  - `GET /logs/student/{studentId}`：材料相关操作日志
  - `GET /class-progress`：班级维度进度
  - `GET /reminders/student/{studentId}`：提醒发送历史
- [PartyQuizController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/party/controller/PartyQuizController.java#L17-L64)（/api/v1/party/quizzes）
  - `GET /banks`：题库列表
  - `GET /banks/{id}`：题库详情
  - `POST /banks`：创建题库
  - `POST /banks/{bankId}/questions`：题库添加题目
  - `POST /submit`：学生提交自测
  - `GET /records/student/{studentId}`：查询自测记录

### 荣誉 Honor
- [StudentHonorController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/honor/controller/StudentHonorController.java#L23-L63)（/api/v1/student/honors）
  - `GET /page`：学生侧荣誉展示分页
  - `GET /{id}`：荣誉展示详情
  - `GET /{id}/recipients`：获奖者列表
  - `GET /recipients/{recipientId}`：获奖者详情
- [AdminHonorController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/honor/controller/AdminHonorController.java#L44-L263)（/api/v1/admin/honors）
  - `GET /page`：后台荣誉展示分页
  - `GET /{id}`：后台展示详情
  - `POST /`：创建展示模块
  - `PUT /{id}`：更新展示模块
  - `DELETE /{id}`：删除展示模块
  - `GET /{id}/recipients/page`：获奖者分页
  - `POST /{id}/recipients`：新增获奖者
  - `POST /{id}/recipients/import`：批量导入获奖者
  - `GET /recipients/{recipientId}`：获奖者详情
  - `PUT /recipients/{recipientId}`：更新获奖者
  - `DELETE /recipients/{recipientId}`：删除获奖者
  - `POST /recipients/{recipientId}/members`：新增集体成员
  - `PUT /members/{memberId}`：更新成员
  - `DELETE /members/{memberId}`：删除成员
  - `POST /recipients/{recipientId}/attachments`：新增附件/照片
  - `PUT /attachments/{attachmentId}`：更新附件
  - `DELETE /attachments/{attachmentId}`：删除附件

### 后台：知识库/工单/通知/角色/流程/课程/统计等（/api/v1/admin/...）
- [AdminKnowledgeController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/admin/controller/AdminKnowledgeController.java#L35-L166)（/api/v1/admin/knowledge）：知识条目管理 + 附件
- [AdminQaTicketController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/admin/controller/AdminQaTicketController.java#L25-L78)（/api/v1/admin/qa-tickets）：工单管理（领取/回复/关闭/撤回）
- [AdminNoticeController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/admin/controller/AdminNoticeController.java#L28-L90)（/api/v1/admin/notices）：通知管理（发布/撤回）
- [AdminRoleController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/admin/controller/AdminRoleController.java#L24-L76)（/api/v1/admin/roles）：角色 CRUD + copy/toggle
- [AdminWorkflowConfigController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/admin/controller/AdminWorkflowConfigController.java#L27-L101)（/api/v1/admin/workflows）：工作流定义/节点配置
- [AdminWorkflowInstanceController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/admin/controller/AdminWorkflowInstanceController.java#L23-L60)（/api/v1/admin/workflow-instances）：工作流实例管理
- [AdminCourseController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/admin/controller/AdminCourseController.java#L27-L77)（/api/v1/admin/courses）：课程库管理
- [AdminTermCourseController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/admin/controller/AdminTermCourseController.java#L27-L77)（/api/v1/admin/term-courses）：开课（学期课程）管理
- [AdminAdvisorScopeController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/admin/controller/AdminAdvisorScopeController.java#L29-L82)（/api/v1/admin/advisor-scopes）：班主任/导师负责范围绑定
- [AdminImportTaskController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/admin/controller/AdminImportTaskController.java#L32-L101)（/api/v1/admin/import-tasks）：导入任务管理（含错误明细）
- [AdminOperationLogController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/admin/controller/AdminOperationLogController.java#L24-L121)（/api/v1/admin/operation-logs）：后台操作日志
- [AdminStatsController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/admin/controller/AdminStatsController.java#L12-L33)（/api/v1/admin/stats）：后台汇总统计
- [AdminSearchAnalyticsController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/admin/controller/AdminSearchAnalyticsController.java#L17-L32)（/api/v1/admin/search-analytics/summary）：搜索分析汇总
- [AdminPartyReminderController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/admin/controller/AdminPartyReminderController.java#L29-L86)（/api/v1/admin/party-reminders）：党务提醒任务
- [AdminCertTemplateController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/admin/controller/AdminCertTemplateController.java#L23-L62)（/api/v1/admin/cert-templates）：证书模板（旧体系）
- [StudentAdminController](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/student/controller/StudentAdminController.java#L36-L170)（/api/v1/admin/students）：学生管理（含画像/状态历史）

---

## 前端 API 调用封装（frontend/src/api）

> 前端主要通过 `frontend/src/api/request.js` 的请求封装调用后端；下面是前端维护的主要 API 模块入口（便于从前端反查端点）。

- [frontend/src/api/auth.js](file:///d:/CS/SASS/project-new/--main/frontend/src/api/auth.js)：登录/获取当前用户/修改密码等
- [frontend/src/api/platform.js](file:///d:/CS/SASS/project-new/--main/frontend/src/api/platform.js)：平台通用（用户/学生/文件/导入/日志/通知发送等）
- [frontend/src/api/student.js](file:///d:/CS/SASS/project-new/--main/frontend/src/api/student.js)：学生端相关（个人/成长/通知/工单等）
- [frontend/src/api/index.js](file:///d:/CS/SASS/project-new/--main/frontend/src/api/index.js)：聚合导出
- [frontend/src/api/request.js](file:///d:/CS/SASS/project-new/--main/frontend/src/api/request.js)：axios/拦截器/统一错误处理

---

## 数据库表（存储内容概览）

### 认证/用户/会话
- `user_account`：登录账号与安全状态（密码 hash、锁定、最后登录等）([UserAccount](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/auth/domain/UserAccount.java#L18))
- `sys_user`：系统用户主档（基础身份信息/扩展字段）([LatestUser](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/auth/domain/LatestUser.java#L17))
- `sys_user_auth`：认证方式（密码/微信 openid 等）([LatestUserAuth](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/auth/domain/LatestUserAuth.java#L13))
- `sys_user_role`：用户与角色关系映射 ([LatestUserRole](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/auth/domain/LatestUserRole.java#L8))
- `sys_role`：角色与权限集合（DDL 创建，JPA 未直接建模）([V23](file:///d:/CS/SASS/project-new/--main/src/main/resources/db/migration/V23__add_all_missing_tables.sql#L1-L13))
- `sys_student_ext`：学生扩展字段（专业/班级/政治面貌等）([LatestStudentExt](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/auth/domain/LatestStudentExt.java#L15))
- `user_session_record`：登录会话记录（设备、IP、过期时间等）([UserSessionRecord](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/auth/domain/UserSessionRecord.java#L15))
- `revoked_token_record`：注销/黑名单 token 记录（用于登出与强制失效）([RevokedTokenRecord](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/auth/domain/RevokedTokenRecord.java#L15))
- `login_audit_log`：登录审计日志（成功/失败原因、时间等）([LoginAuditLog](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/auth/domain/LoginAuditLog.java#L13))

### 学生档案/画像/范围
- `student_profile`：学生基础档案（学号、姓名、学院等）([StudentProfile](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/student/domain/StudentProfile.java#L13))
- `student_portrait`：学生画像（指标/标签/状态汇总）([StudentPortrait](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/student/domain/StudentPortrait.java#L13))
- `student_status_history`：学籍/状态变更历史（休学/复学/毕业等）([StudentStatusHistory](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/student/domain/StudentStatusHistory.java#L13))
- `advisor_scope_binding`：班主任/导师负责范围绑定（年级/班级等）([AdvisorScopeBinding](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/student/domain/AdvisorScopeBinding.java#L13))

### 学生成长（按模块分表）
- `student_award_support_record`：奖助学金/助学相关记录 ([StudentAwardSupportRecord](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/student/domain/StudentAwardSupportRecord.java#L15))
- `student_competition_record`：竞赛获奖/参赛记录 ([StudentCompetitionRecord](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/student/domain/StudentCompetitionRecord.java#L15))
- `student_innovation_entrepreneurship_record`：创新创业项目记录 ([StudentInnovationEntrepreneurshipRecord](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/student/domain/StudentInnovationEntrepreneurshipRecord.java#L15))
- `student_social_practice_record`：社会实践记录 ([StudentSocialPracticeRecord](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/student/domain/StudentSocialPracticeRecord.java#L15))
- `student_student_work_record`：学生工作/任职记录 ([StudentStudentWorkRecord](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/student/domain/StudentStudentWorkRecord.java#L15))
- `student_volunteer_service_record`：志愿服务记录 ([StudentVolunteerServiceRecord](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/student/domain/StudentVolunteerServiceRecord.java#L16))
- `student_skill_certificate_record`：技能证书/资质记录 ([StudentSkillCertificateRecord](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/student/domain/StudentSkillCertificateRecord.java#L15))

### 工作记录
- `student_work_log`：老师对学生的工作记录/跟进记录 ([StudentWorkLog](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/worklog/domain/StudentWorkLog.java#L15))
- `student_work_log_action_log`：工作记录操作流水（创建/修改/删除/状态变化）([StudentWorkLogActionLog](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/worklog/domain/StudentWorkLogActionLog.java#L13))

### 通知
- `notice`：通知主表（标题/内容/发布状态等）([Notice](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/notice/domain/Notice.java#L15))
- `notice_tag_dict`：通知标签字典 ([LatestNoticeTagDict](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/notice/domain/LatestNoticeTagDict.java#L13))
- `notice_item`：通知条目/版本化内容项 ([LatestNoticeItem](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/notice/domain/LatestNoticeItem.java#L15))
- `notice_item_tag`：通知条目与标签关联 ([LatestNoticeItemTag](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/notice/domain/LatestNoticeItemTag.java#L17))
- `notice_delivery`：通知投递批次/记录 ([LatestNoticeDelivery](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/notice/domain/LatestNoticeDelivery.java#L15))
- `notice_delivery_target`：投递目标（到人/到班/到群体等）([LatestNoticeDeliveryTarget](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/notice/domain/LatestNoticeDeliveryTarget.java#L15))
- `notice_item_keyword`：通知条目与关键词关联（DDL 创建）([V23](file:///d:/CS/SASS/project-new/--main/src/main/resources/db/migration/V23__add_all_missing_tables.sql))

### 知识库/检索/文件
- `knowledge_document`：知识文档（标题/摘要/内容/来源等）([KnowledgeDocument](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/knowledge/domain/KnowledgeDocument.java#L13))
- `kb_policy`：政策类知识条目（学生端政策列表来源）([LatestKnowledgePolicy](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/knowledge/domain/LatestKnowledgePolicy.java#L15))
- `kb_faq`：FAQ 问答库（问题/答案/来源政策等，DDL 创建）([V23](file:///d:/CS/SASS/project-new/--main/src/main/resources/db/migration/V23__add_all_missing_tables.sql#L155-L170))
- `kb_qa_ticket`：学生问答工单（问题/状态/指派等）([KnowledgeQaTicket](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/knowledge/domain/KnowledgeQaTicket.java#L17))
- `kb_qa_ticket_message`：工单消息（回复/撤回/附件等）([KnowledgeQaTicketMessage](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/knowledge/domain/KnowledgeQaTicketMessage.java#L17))
- `kb_keyword`：关键词字典（DDL 创建）([V23](file:///d:/CS/SASS/project-new/--main/src/main/resources/db/migration/V23__add_all_missing_tables.sql#L174-L179))
- `kb_policy_keyword`：政策-关键词关联（DDL 创建）([V23](file:///d:/CS/SASS/project-new/--main/src/main/resources/db/migration/V23__add_all_missing_tables.sql#L181-L190))
- `kb_faq_keyword`：FAQ-关键词关联（DDL 创建）([V23](file:///d:/CS/SASS/project-new/--main/src/main/resources/db/migration/V23__add_all_missing_tables.sql#L192-L199))
- `kb_keyword_search_history`：关键词检索历史（用户/范围/结果数等，DDL 创建）([V23](file:///d:/CS/SASS/project-new/--main/src/main/resources/db/migration/V23__add_all_missing_tables.sql#L201-L223))
- `search_query_log`：搜索查询日志（关键词/结果统计/来源）([SearchQueryLog](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/knowledge/domain/SearchQueryLog.java#L17))
- `file_object`：文件对象元数据（存储位置/文件名/大小/摘要等）([LatestFileObject](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/knowledge/domain/LatestFileObject.java#L15))
- `knowledge_attachment`：知识条目附件（关联 file_object 或上传记录）([KnowledgeAttachment](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/admin/domain/KnowledgeAttachment.java#L13))

### 平台治理/上传/通知发送记录
- `platform_system_setting`：系统设置键值（安全策略/上传策略等）([PlatformSystemSetting](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/platform/domain/PlatformSystemSetting.java#L13))
- `platform_file_upload_record`：上传记录（业务类型、归档、操作者等）([PlatformFileUploadRecord](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/platform/domain/PlatformFileUploadRecord.java#L13))
- `platform_notification_send_record`：通知发送记录（渠道、目标、状态、失败原因等）([PlatformNotificationSendRecord](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/platform/domain/PlatformNotificationSendRecord.java#L15))

### 导入任务/操作审计
- `data_import_task`：导入任务主表（类型/状态/操作者/文件等）([DataImportTask](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/admin/domain/DataImportTask.java#L13))
- `data_import_error_item`：导入错误明细（行号/字段/错误信息）([DataImportErrorItem](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/admin/domain/DataImportErrorItem.java#L13))
- `sys_operation_log`：系统操作审计（模块/traceId/结果等）([LatestSysOperationLog](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/admin/domain/LatestSysOperationLog.java#L17))
- `admin_operation_log`：后台操作日志（偏业务操作留痕）([AdminOperationLog](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/admin/domain/AdminOperationLog.java#L13))
- `audit_import_job`：审计导入作业（批处理导入审计）([LatestAuditImportJob](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/admin/domain/LatestAuditImportJob.java#L15))

### 证明/审批/工作流/事务（Affair/Workflow）
- `certificate_request`：证明申请主表（类型/原因/状态/学生）([CertificateRequest](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/certificate/domain/CertificateRequest.java#L15))
- `approval_action_log`：审批动作日志（每一步审批轨迹）([ApprovalActionLog](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/certificate/domain/ApprovalActionLog.java#L13))
- `certificate_template`：证明模板（模板文件/渲染配置等）([CertificateTemplate](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/certificate/domain/CertificateTemplate.java#L13))
- `cert_template_keyword`：证书/证明模板与关键词关联（DDL 创建）([V21](file:///d:/CS/SASS/project-new/--main/src/main/resources/db/migration/V21__add_affair_and_workflow_tables.sql#L1-L120))
- `cert_generated_file`：生成后的证明/证书文件记录（定位/元数据，DDL 创建）([V21](file:///d:/CS/SASS/project-new/--main/src/main/resources/db/migration/V21__add_affair_and_workflow_tables.sql#L1-L120))
- `wf_definition`：工作流定义（流程元数据/状态）([LatestWorkflowDefinition](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/certificate/domain/LatestWorkflowDefinition.java#L17))
- `wf_node`：工作流节点（节点配置/顺序）([LatestWorkflowNode](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/certificate/domain/LatestWorkflowNode.java#L17))
- `wf_instance`：工作流实例（业务单据关联/当前状态）([LatestWorkflowInstance](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/certificate/domain/LatestWorkflowInstance.java#L15))
- `wf_task`：工作流任务（待办/处理人/截止等）([LatestWorkflowTask](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/certificate/domain/LatestWorkflowTask.java#L15))
- `wf_task_action`：任务动作（同意/驳回/转派等记录）([LatestWorkflowTaskAction](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/certificate/domain/LatestWorkflowTaskAction.java#L18))
- `affair_request`：事务申请统一入口（不同事项的申请单）([LatestAffairRequest](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/certificate/domain/LatestAffairRequest.java#L15))
- `cert_template`：证书模板（旧体系）([LatestCertTemplate](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/knowledge/domain/LatestCertTemplate.java#L17))
- `cert_application`：证书申请（旧体系）([LatestCertApplication](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/certificate/domain/LatestCertApplication.java#L13))

### 党团/入党
- `party_flow`：入党流程（后台配置）([LatestPartyFlow](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/party/domain/LatestPartyFlow.java#L17))
- `party_flow_node`：流程节点（后台配置）([LatestPartyFlowNode](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/party/domain/LatestPartyFlowNode.java#L17))
- `party_flow_template`：流程模板（学生侧可见）([PartyFlowTemplate](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/party/domain/PartyFlowTemplate.java#L13))
- `party_flow_stage`：模板阶段 ([PartyFlowStage](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/party/domain/PartyFlowStage.java#L13))
- `party_student_progress`：学生流程进度（当前节点/截止等）([LatestPartyStudentProgress](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/party/domain/LatestPartyStudentProgress.java#L15))
- `party_progress_record`：进度推进记录（阶段推进历史）([PartyProgressRecord](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/party/domain/PartyProgressRecord.java#L15))
- `party_reminder_task`：提醒任务（到期时间/状态/渠道）([LatestPartyReminderTask](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/party/domain/LatestPartyReminderTask.java#L15))
- `party_material_submission`：入党材料提交（文件/状态/审核意见）([PartyMaterialSubmission](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/party/domain/PartyMaterialSubmission.java#L15))
- `party_action_log`：党务动作日志（提交/审核/撤回等）([PartyActionLog](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/party/domain/PartyActionLog.java#L15))
- `party_question_bank`：自测题库 ([PartyQuestionBank](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/party/domain/PartyQuestionBank.java#L13))
- `party_question`：题目明细 ([PartyQuestion](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/party/domain/PartyQuestion.java#L13))
- `party_quiz_record`：自测记录（得分/答案等）([PartyQuizRecord](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/party/domain/PartyQuizRecord.java#L15))

### 荣誉
- `honor_showcase`：荣誉展示模块（奖项/展示期/可见性）([HonorShowcase](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/honor/domain/HonorShowcase.java#L15))
- `honor_recipient`：获奖者（个人/集体，简介/事迹等）([HonorRecipient](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/honor/domain/HonorRecipient.java#L15))
- `honor_recipient_member`：集体获奖成员明细 ([HonorRecipientMember](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/honor/domain/HonorRecipientMember.java#L13))
- `honor_recipient_attachment`：获奖者附件/照片 ([HonorRecipientAttachment](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/honor/domain/HonorRecipientAttachment.java#L13))

### 学业（含部分 DDL 快照表）
- `academic_program`：培养方案 ([AcademicProgram](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/academic/domain/AcademicProgram.java#L13))
- `academic_program_module`：培养方案模块 ([AcademicProgramModule](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/academic/domain/AcademicProgramModule.java#L13))
- `academic_transcript`：成绩单主表 ([AcademicTranscript](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/academic/domain/AcademicTranscript.java#L15))
- `academic_transcript_item`：成绩单明细（课程/学分/成绩）([AcademicTranscriptItem](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/academic/domain/AcademicTranscriptItem.java#L13))
- `academic_warning_record`：学业预警记录（预警等级/原因）([AcademicWarningRecord](file:///d:/CS/SASS/project-new/--main/src/main/java/edu/ruc/platform/academic/domain/AcademicWarningRecord.java#L13))
- `aca_*`：学业侧同步/快照表族（培养方案/成绩/开课/审核报告等；部分由 DDL 创建、部分由 Latest* 实体映射）  
  - 入口参考：`V20__add_academic_tables.sql`（[V20](file:///d:/CS/SASS/project-new/--main/src/main/resources/db/migration/V20__add_academic_tables.sql#L1-L172)）

