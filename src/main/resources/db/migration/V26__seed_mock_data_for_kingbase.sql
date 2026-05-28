-- Consolidated and verified mock seed data for the kingbase profile.
-- This migration replaces historical ad-hoc INSERT statements that were mixed
-- into earlier DDL migrations.

-- Shared password hash for the default demo password: 123456

INSERT INTO sys_role (id, role_code, role_name, permissions, is_active, is_deleted, created_at, updated_at)
VALUES
    (1, 'student', '普通学生', NULL, 1, 0, TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00'),
    (2, 'cadre', '班团骨干', NULL, 1, 0, TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00'),
    (3, 'teacher_admin', '管理老师', NULL, 1, 0, TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00'),
    (4, 'college_leader', '学院领导', NULL, 1, 0, TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_account (
    id, created_at, updated_at, username, password_hash, role, enabled, wechat_open_id,
    password_reset_required, failed_login_attempts, locked_until
)
VALUES
    (1, TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 'admin', '$2a$10$P.q4GFPLojG3ZA7bIeA1l.LJ4PqsbhI1HURejAr6.nGMT7oW9gZF6', 'SUPER_ADMIN', TRUE, NULL, FALSE, 0, NULL),
    (20001, TIMESTAMP '2026-03-20 09:05:00', TIMESTAMP '2026-03-20 09:05:00', 'teacher01', '$2a$10$P.q4GFPLojG3ZA7bIeA1l.LJ4PqsbhI1HURejAr6.nGMT7oW9gZF6', 'COUNSELOR', TRUE, NULL, FALSE, 0, NULL),
    (20002, TIMESTAMP '2026-03-20 09:10:00', TIMESTAMP '2026-03-20 09:10:00', 'advisor01', '$2a$10$P.q4GFPLojG3ZA7bIeA1l.LJ4PqsbhI1HURejAr6.nGMT7oW9gZF6', 'CLASS_ADVISOR', TRUE, NULL, FALSE, 0, NULL),
    (10002, TIMESTAMP '2026-03-20 09:15:00', TIMESTAMP '2026-03-20 09:15:00', '2023100002', '$2a$10$P.q4GFPLojG3ZA7bIeA1l.LJ4PqsbhI1HURejAr6.nGMT7oW9gZF6', 'LEAGUE_SECRETARY', TRUE, NULL, FALSE, 0, NULL),
    (10001, TIMESTAMP '2026-03-20 09:20:00', TIMESTAMP '2026-03-20 09:20:00', '2023100001', '$2a$10$P.q4GFPLojG3ZA7bIeA1l.LJ4PqsbhI1HURejAr6.nGMT7oW9gZF6', 'STUDENT', TRUE, 'mock-open-id-10001', FALSE, 0, NULL)
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_user (id, student_no, full_name, status, ext_json, created_at, updated_at, is_deleted)
VALUES
    (1, 'admin', '系统管理员', 'active', '{"grade":"admin"}', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 0),
    (20001, 'teacher01', '胡浩老师', 'active', '{"grade":"2023"}', TIMESTAMP '2026-03-20 09:05:00', TIMESTAMP '2026-03-20 09:05:00', 0),
    (20002, 'advisor01', '王老师', 'active', '{"grade":"2023"}', TIMESTAMP '2026-03-20 09:10:00', TIMESTAMP '2026-03-20 09:10:00', 0),
    (10002, '2023100002', '李四', 'active', '{"grade":"2023"}', TIMESTAMP '2026-03-20 09:15:00', TIMESTAMP '2026-03-20 09:15:00', 0),
    (10001, '2023100001', '张三', 'active', '{"grade":"2023"}', TIMESTAMP '2026-03-20 09:20:00', TIMESTAMP '2026-03-20 09:20:00', 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_user_role (id, user_id, role_id, created_at)
VALUES
    (1, 1, 4, TIMESTAMP '2026-03-20 09:00:00'),
    (2, 20001, 3, TIMESTAMP '2026-03-20 09:05:00'),
    (3, 20002, 3, TIMESTAMP '2026-03-20 09:10:00'),
    (4, 10002, 2, TIMESTAMP '2026-03-20 09:15:00'),
    (5, 10001, 1, TIMESTAMP '2026-03-20 09:20:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_user_auth (id, student_no, login_method, wechat_openid, password_hash, created_at, updated_at, is_deleted)
VALUES
    (1, 'admin', 'password', NULL, '$2a$10$P.q4GFPLojG3ZA7bIeA1l.LJ4PqsbhI1HURejAr6.nGMT7oW9gZF6', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 0),
    (2, 'teacher01', 'password', NULL, '$2a$10$P.q4GFPLojG3ZA7bIeA1l.LJ4PqsbhI1HURejAr6.nGMT7oW9gZF6', TIMESTAMP '2026-03-20 09:05:00', TIMESTAMP '2026-03-20 09:05:00', 0),
    (3, 'advisor01', 'password', NULL, '$2a$10$P.q4GFPLojG3ZA7bIeA1l.LJ4PqsbhI1HURejAr6.nGMT7oW9gZF6', TIMESTAMP '2026-03-20 09:10:00', TIMESTAMP '2026-03-20 09:10:00', 0),
    (4, '2023100002', 'password', NULL, '$2a$10$P.q4GFPLojG3ZA7bIeA1l.LJ4PqsbhI1HURejAr6.nGMT7oW9gZF6', TIMESTAMP '2026-03-20 09:15:00', TIMESTAMP '2026-03-20 09:15:00', 0),
    (5, '2023100001', 'password', NULL, '$2a$10$P.q4GFPLojG3ZA7bIeA1l.LJ4PqsbhI1HURejAr6.nGMT7oW9gZF6', TIMESTAMP '2026-03-20 09:20:00', TIMESTAMP '2026-03-20 09:20:00', 0),
    (6, '2023100001', 'wechat', 'mock-open-id-10001', NULL, TIMESTAMP '2026-03-20 09:20:00', TIMESTAMP '2026-03-20 09:20:00', 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_student_ext (
    student_no, major_name, grade_year, class_name, political_status, party_status,
    id_card_hash, home_address_hash, phone_hash, gpa, ext_json, created_at, updated_at, is_deleted
)
VALUES
    ('2023100001', '计算机类', 2023, '计科一班', '共青团员', '积极分子',
     '110101199901011212', '北京市海淀区学院路1号', '13800125678', 3.82,
     '{"advisor":"advisor01|王老师","collegeName":"信息学院","degreeLevel":"本科"}',
     TIMESTAMP '2026-03-20 09:20:00', TIMESTAMP '2026-03-20 09:20:00', 0),
    ('2023100002', '计算机类', 2023, '计科二班', '共青团员', '团支书',
     '130101199901013434', '河北省石家庄市示例地址', '13900121234', 3.45,
     '{"advisor":"advisor02|赵老师","collegeName":"信息学院","degreeLevel":"本科"}',
     TIMESTAMP '2026-03-20 09:15:00', TIMESTAMP '2026-03-20 09:15:00', 0)
ON CONFLICT (student_no) DO NOTHING;

INSERT INTO student_profile (
    id, created_at, updated_at, student_no, name, college_name, major, grade, class_name,
    degree_level, encrypted_id_card_no, encrypted_phone, email, graduated, advisor_scope
)
VALUES
    (10001, TIMESTAMP '2026-03-20 09:20:00', TIMESTAMP '2026-03-20 09:20:00', '2023100001', '张三', '信息学院', '计算机类', '2023级', '计科一班', '本科', '****************12', '********5678', 'zhangsan@example.edu', FALSE, 'advisor01|王老师'),
    (10002, TIMESTAMP '2026-03-20 09:15:00', TIMESTAMP '2026-03-20 09:15:00', '2023100002', '李四', '信息学院', '计算机类', '2023级', '计科二班', '本科', '****************34', '********1234', 'lisi@example.edu', FALSE, 'advisor02|赵老师')
ON CONFLICT (id) DO NOTHING;

INSERT INTO student_portrait (
    id, created_at, updated_at, student_id, gender, ethnicity, honors, scholarships, competitions,
    social_practice, volunteer_service, research_experience, discipline_records, leadership_roles,
    daily_performance, gpa, grade_rank, major_rank, credits_earned, career_orientation,
    remarks, updated_by, data_source, public_visible
)
VALUES
    (1, TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 10001, '男', '汉族', '国奖,校优', '国家奖学金', '数学建模',
     '支教实践', '20小时', '导师课题参与', NULL, '班长、团支书', '表现良好', 3.82, 12, 5, 98, '升学',
     '可作为榜样展示', '胡浩老师', '老师维护', TRUE)
ON CONFLICT (student_id) DO NOTHING;

INSERT INTO advisor_scope_binding (
    id, advisor_username, advisor_name, grade, class_name, student_id, created_at, updated_at
)
VALUES
    (1, 'advisor01', '王老师', '2023级', '计科一班', 10001, TIMESTAMP '2026-03-20 09:10:00', TIMESTAMP '2026-03-20 09:10:00'),
    (2, 'advisor02', '赵老师', '2023级', '计科二班', 10002, TIMESTAMP '2026-03-20 09:12:00', TIMESTAMP '2026-03-20 09:12:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO student_status_history (
    id, created_at, updated_at, student_id, from_status, to_status, changed_to_major, reason, changed_by, changed_by_role
)
VALUES
    (21, TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 10001, NULL, 'ACTIVE', NULL, '初始建档', '系统管理员', 'SUPER_ADMIN')
ON CONFLICT (id) DO NOTHING;

INSERT INTO student_award_support_record (
    id, created_at, updated_at, student_id, assessment_academic_year, award_name, batch_name, award_level, award_grade, award_amount, award_type
)
VALUES
    (101, TIMESTAMP '2026-05-01 10:00:00', TIMESTAMP '2026-05-01 10:00:00', 10001, '2024-2025', '国家奖学金', '第一批', '国家级', '一等奖', 8000.00, '奖学金')
ON CONFLICT (id) DO NOTHING;

INSERT INTO student_competition_record (
    id, created_at, updated_at, student_id, award_date, competition_name, competition_level, competition_grade, competition_category, organizer, advisor_teacher_info, remarks
)
VALUES
    (102, TIMESTAMP '2026-05-02 09:30:00', TIMESTAMP '2026-05-02 09:30:00', 10001, DATE '2025-11-18', '全国大学生数学建模竞赛', '国家级', '二等奖', '学科竞赛', '教育部高教司', '李老师', '团队核心成员')
ON CONFLICT (id) DO NOTHING;

INSERT INTO student_innovation_entrepreneurship_record (
    id, created_at, updated_at, student_id, start_date, end_date, project_code, project_name, college_name, project_status,
    project_level, completion_grade, participant_role, project_type, project_batch, participant_count, advisor_teacher
)
VALUES
    (103, TIMESTAMP '2026-05-03 14:20:00', TIMESTAMP '2026-05-03 14:20:00', 10001, DATE '2025-03-01', DATE '2025-12-20', 'CX2025-01', '校园服务智能问答平台',
     '信息学院', '已结项', '校级', '优秀', '负责人', '创新训练', '2025年度', 5, '胡老师')
ON CONFLICT (id) DO NOTHING;

INSERT INTO student_social_practice_record (
    id, created_at, updated_at, student_id, practice_start_date, practice_end_date, practice_team_name, practice_theme, practice_location, practice_team_level, advisor_teacher
)
VALUES
    (104, TIMESTAMP '2026-05-04 11:15:00', TIMESTAMP '2026-05-04 11:15:00', 10001, DATE '2025-07-10', DATE '2025-07-18', '乡村振兴实践团', '数字助农', '河北保定', '校级', '王老师')
ON CONFLICT (id) DO NOTHING;

INSERT INTO student_student_work_record (
    id, created_at, updated_at, student_id, start_date, end_date, organization_name, position_name, work_description
)
VALUES
    (105, TIMESTAMP '2026-05-05 08:50:00', TIMESTAMP '2026-05-05 08:50:00', 10001, DATE '2024-09-01', DATE '2025-06-30', '学生会', '学习部部长', '负责活动策划与执行')
ON CONFLICT (id) DO NOTHING;

INSERT INTO student_volunteer_service_record (
    id, created_at, updated_at, student_id, service_date, service_project, service_location, service_duration_hours, service_organization_name
)
VALUES
    (106, TIMESTAMP '2026-05-06 16:40:00', TIMESTAMP '2026-05-06 16:40:00', 10001, DATE '2025-12-05', '迎新志愿服务', '通州校区', 8.00, '青年志愿者协会')
ON CONFLICT (id) DO NOTHING;

INSERT INTO student_skill_certificate_record (
    id, created_at, updated_at, student_id, certificate_name, obtained_date, certificate_level, description
)
VALUES
    (107, TIMESTAMP '2026-05-07 13:10:00', TIMESTAMP '2026-05-07 13:10:00', 10001, '英语六级证书', DATE '2025-02-20', '国家级', '成绩 580 分')
ON CONFLICT (id) DO NOTHING;

INSERT INTO file_object (
    id, purpose, original_name, mime_type, size_bytes, sha256, storage_provider, storage_path,
    uploaded_by, uploaded_at, created_at, updated_at, is_deleted
)
VALUES
    (5001, 'cert_template', 'study-certificate.pdf', 'application/pdf', 20480, 'sha-template-5001', 'local', '/templates/cert/study-certificate.pdf', 1, TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 0),
    (5002, 'cert_template', 'party-member-certificate.pdf', 'application/pdf', 22528, 'sha-template-5002', 'local', '/templates/cert/party-member-certificate.pdf', 1, TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 0),
    (5003, 'cert_template', 'difficulty-certificate.pdf', 'application/pdf', 19456, 'sha-template-5003', 'local', '/templates/cert/difficulty-certificate.pdf', 1, TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 0),
    (5004, 'cert_template', 'transcript.pdf', 'application/pdf', 24576, 'sha-template-5004', 'local', '/templates/cert/transcript.pdf', 1, TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 0),
    (5005, 'cert_template', 'internship-certificate.pdf', 'application/pdf', 21504, 'sha-template-5005', 'local', '/templates/cert/internship-certificate.pdf', 1, TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 0),
    (5101, 'knowledge_attachment', 'party-process.pdf', 'application/pdf', 1024, 'sha-knowledge-5101', 'local', '/uploads/knowledge/2/party-process.pdf', 1, TIMESTAMP '2026-03-22 11:00:00', TIMESTAMP '2026-03-22 11:00:00', TIMESTAMP '2026-03-22 11:00:00', 0)
ON CONFLICT (id) DO NOTHING;


INSERT INTO knowledge_attachment (
    id, created_at, updated_at, knowledge_id, file_name, content_type, file_size, storage_path, uploaded_by
)
VALUES
    (51, TIMESTAMP '2026-03-22 11:00:00', TIMESTAMP '2026-03-22 11:00:00', 2, 'party-process.pdf', 'application/pdf', 1024, '/uploads/knowledge/2/party-process.pdf', '胡浩老师')
ON CONFLICT (id) DO NOTHING;

INSERT INTO notice (
    id, created_at, updated_at, title, summary, tag, target_grade, target_major, target_graduate_only, publish_time
)
VALUES
    (1, TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', '先锋奖章与奖学金咨询入口', '统一解答先锋奖章、吴玉章奖学金、优秀毕业生等标准问题。', '奖助学金,知识库', NULL, NULL, FALSE, TIMESTAMP '2026-03-20 09:00:00'),
    (2, TIMESTAMP '2026-03-21 11:00:00', TIMESTAMP '2026-03-21 11:00:00', '入党入团流程说明更新', '已补充固定流程、时间线和常见问题入口。', '党团事务,流程', NULL, NULL, FALSE, TIMESTAMP '2026-03-21 11:00:00'),
    (3, TIMESTAMP '2026-03-22 10:30:00', TIMESTAMP '2026-03-22 10:30:00', '2023级计算机类就业信息汇总', '面向 2023 级计算机类学生的就业与实习通知。', '就业,实习,计算机类', '2023级', '计算机类', FALSE, TIMESTAMP '2026-03-22 10:30:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO notice_tag_dict (id, tag_code, tag_name, created_at, updated_at, is_deleted)
VALUES
    (1, 'scholarship', '奖助学金', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 0),
    (2, 'knowledge', '知识库', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 0),
    (3, 'party', '党团事务', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 0),
    (4, 'process', '流程', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 0),
    (5, 'job', '就业', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 0),
    (6, 'internship', '实习', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 0),
    (7, 'computer', '计算机类', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO notice_item (
    id, title, content, source_type, source_name, source_url, attachment_file_id,
    publish_at, created_by, ext_json, created_at, updated_at, is_deleted
)
VALUES
    (1, '先锋奖章与奖学金咨询入口', '统一解答先锋奖章、吴玉章奖学金、优秀毕业生等标准问题。', 'manual', '系统管理员', NULL, NULL, TIMESTAMP '2026-03-20 09:00:00', 1, NULL, TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 0),
    (2, '入党入团流程说明更新', '已补充固定流程、时间线和常见问题入口。', 'manual', '系统管理员', NULL, NULL, TIMESTAMP '2026-03-21 11:00:00', 1, NULL, TIMESTAMP '2026-03-21 11:00:00', TIMESTAMP '2026-03-21 11:00:00', 0),
    (3, '2023级计算机类就业信息汇总', '面向 2023 级计算机类学生的就业与实习通知。', 'manual', '系统管理员', NULL, NULL, TIMESTAMP '2026-03-22 10:30:00', 1, NULL, TIMESTAMP '2026-03-22 10:30:00', TIMESTAMP '2026-03-22 10:30:00', 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO notice_item_tag (id, notice_id, tag_id, created_at)
VALUES
    (1, 1, 1, TIMESTAMP '2026-03-20 09:00:00'),
    (2, 1, 2, TIMESTAMP '2026-03-20 09:00:00'),
    (3, 2, 3, TIMESTAMP '2026-03-21 11:00:00'),
    (4, 2, 4, TIMESTAMP '2026-03-21 11:00:00'),
    (5, 3, 5, TIMESTAMP '2026-03-22 10:30:00'),
    (6, 3, 6, TIMESTAMP '2026-03-22 10:30:00'),
    (7, 3, 7, TIMESTAMP '2026-03-22 10:30:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO notice_delivery (
    id, notice_id, channel, target_rule_json, status, scheduled_at, sent_at,
    created_by, ext_json, created_at, updated_at
)
VALUES
    (1, 1, 'miniprogram', '{}', 'done', TIMESTAMP '2026-03-20 08:55:00', TIMESTAMP '2026-03-20 09:00:00', 1, NULL, TIMESTAMP '2026-03-20 08:55:00', TIMESTAMP '2026-03-20 09:00:00'),
    (2, 2, 'miniprogram', '{}', 'done', TIMESTAMP '2026-03-21 10:50:00', TIMESTAMP '2026-03-21 11:00:00', 1, NULL, TIMESTAMP '2026-03-21 10:50:00', TIMESTAMP '2026-03-21 11:00:00'),
    (3, 3, 'miniprogram', '{"gradeYears":["2023"],"majors":["计算机类"]}', 'done', TIMESTAMP '2026-03-22 10:20:00', TIMESTAMP '2026-03-22 10:30:00', 1, NULL, TIMESTAMP '2026-03-22 10:20:00', TIMESTAMP '2026-03-22 10:30:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO notice_delivery_target (
    id, delivery_id, target_user_id, status, sent_at, error_message, is_read, read_at, created_at, updated_at
)
VALUES
    (1, 1, 10001, 'sent', TIMESTAMP '2026-03-20 09:00:00', NULL, FALSE, NULL, TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00'),
    (2, 1, 10002, 'sent', TIMESTAMP '2026-03-20 09:00:00', NULL, FALSE, NULL, TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00'),
    (3, 2, 10001, 'sent', TIMESTAMP '2026-03-21 11:00:00', NULL, FALSE, NULL, TIMESTAMP '2026-03-21 11:00:00', TIMESTAMP '2026-03-21 11:00:00'),
    (4, 2, 10002, 'sent', TIMESTAMP '2026-03-21 11:00:00', NULL, FALSE, NULL, TIMESTAMP '2026-03-21 11:00:00', TIMESTAMP '2026-03-21 11:00:00'),
    (5, 3, 10001, 'sent', TIMESTAMP '2026-03-22 10:30:00', NULL, FALSE, NULL, TIMESTAMP '2026-03-22 10:30:00', TIMESTAMP '2026-03-22 10:30:00'),
    (6, 3, 10002, 'sent', TIMESTAMP '2026-03-22 10:30:00', NULL, FALSE, NULL, TIMESTAMP '2026-03-22 10:30:00', TIMESTAMP '2026-03-22 10:30:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO platform_notification_send_record (
    id, created_at, updated_at, title, channel, target_type, target_description,
    status, recipient_count, triggered_by, sent_at, extension_channels
)
VALUES
    (1, TIMESTAMP '2026-03-23 10:00:00', TIMESTAMP '2026-03-23 10:00:00', '奖学金材料提交通知', 'IN_APP', 'GRADE', '2023级', 'SENT', 320, '系统管理员', TIMESTAMP '2026-03-23 10:00:00', 'EMAIL,WECHAT'),
    (2, TIMESTAMP '2026-03-23 15:30:00', TIMESTAMP '2026-03-23 15:30:00', '证明审批结果通知', 'IN_APP', 'SELF', 'studentId=10001', 'SENT', 1, '胡浩老师', TIMESTAMP '2026-03-23 15:30:00', 'EMAIL,WECHAT')
ON CONFLICT (id) DO NOTHING;

INSERT INTO platform_file_upload_record (
    id, created_at, updated_at, biz_type, biz_id, file_name, content_type, file_size, storage_path, uploaded_by_id, uploaded_by
)
VALUES
    (5001, TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 'CERT_TEMPLATE', 1, 'study-certificate.pdf', 'application/pdf', 20480, '/templates/cert/study-certificate.pdf', 1, '系统管理员'),
    (5002, TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 'CERT_TEMPLATE', 2, 'party-member-certificate.pdf', 'application/pdf', 22528, '/templates/cert/party-member-certificate.pdf', 1, '系统管理员'),
    (5003, TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 'CERT_TEMPLATE', 3, 'difficulty-certificate.pdf', 'application/pdf', 19456, '/templates/cert/difficulty-certificate.pdf', 1, '系统管理员'),
    (5004, TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 'CERT_TEMPLATE', 4, 'transcript.pdf', 'application/pdf', 24576, '/templates/cert/transcript.pdf', 1, '系统管理员'),
    (5005, TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 'CERT_TEMPLATE', 5, 'internship-certificate.pdf', 'application/pdf', 21504, '/templates/cert/internship-certificate.pdf', 1, '系统管理员'),
    (5501, TIMESTAMP '2026-03-21 09:00:00', TIMESTAMP '2026-03-21 09:00:00', 'ACA_TRANSCRIPT', 10001, 'transcript-10001.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 24576, '/uploads/academic/10001/transcript.xlsx', 20001, '胡浩老师')
ON CONFLICT (id) DO NOTHING;

INSERT INTO certificate_template (
    id, template_code, template_name, certificate_type, template_content, template_file_path,
    output_format, is_active, description, updated_by, created_at, updated_at
)
VALUES
    (1, 'CERT_001', '在读证明模板', '在读证明', '兹证明{{studentName}}同学（学号：{{studentNo}}）系我院{{majorName}}专业{{gradeYear}}级学生，当前学籍状态为在读。', '/templates/cert/study-certificate.pdf', 'PDF', TRUE, '用于学生在读状态证明', '系统管理员', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00'),
    (2, 'CERT_002', '党员身份证明模板', '党员身份证明', '兹证明{{studentName}}同学（学号：{{studentNo}}）系我院{{majorName}}专业学生，该生于{{joinDate}}加入中国共产党，当前党组织关系在我院。', '/templates/cert/party-member-certificate.pdf', 'PDF', TRUE, '用于党员身份证明', '系统管理员', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00'),
    (3, 'CERT_003', '困难认定证明模板', '困难认定证明', '兹证明{{studentName}}同学（学号：{{studentNo}}）系我院{{majorName}}专业{{gradeYear}}级学生，经学院认定，该生家庭经济困难等级为{{difficultyLevel}}。', '/templates/cert/difficulty-certificate.pdf', 'PDF', TRUE, '用于学生困难认定证明', '系统管理员', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00'),
    (4, 'CERT_004', '成绩单模板', '成绩单', '兹证明{{studentName}}同学（学号：{{studentNo}}）在我院{{majorName}}专业学习期间，各科成绩如下：{{grades}}', '/templates/cert/transcript.pdf', 'PDF', TRUE, '用于学生成绩证明', '系统管理员', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00'),
    (5, 'CERT_005', '实习证明模板', '实习证明', '兹证明{{studentName}}同学（学号：{{studentNo}}）于{{startDate}}至{{endDate}}在{{companyName}}实习，实习岗位为{{position}}。', '/templates/cert/internship-certificate.pdf', 'PDF', TRUE, '用于学生实习证明', '系统管理员', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO cert_template (
    id, template_code, template_name, file_id, output_format, is_active, created_by,
    ext_json, created_at, updated_at, is_deleted
)
VALUES
    (1, 'CERT_001', '在读证明模板', 5001, 'pdf', 1, 1, '{"certificateType":"在读证明"}', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 0),
    (2, 'CERT_002', '党员身份证明模板', 5002, 'pdf', 1, 1, '{"certificateType":"党员身份证明"}', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 0),
    (3, 'CERT_003', '困难认定证明模板', 5003, 'pdf', 1, 1, '{"certificateType":"困难认定证明"}', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 0),
    (4, 'CERT_004', '成绩单模板', 5004, 'pdf', 1, 1, '{"certificateType":"成绩单"}', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 0),
    (5, 'CERT_005', '实习证明模板', 5005, 'pdf', 1, 1, '{"certificateType":"实习证明"}', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO certificate_request (
    id, created_at, updated_at, student_id, certificate_type, status, reason, generated_pdf_path
)
VALUES
    (1001, TIMESTAMP '2026-03-20 10:30:00', TIMESTAMP '2026-03-20 10:30:00', 10001, '在读证明', 'PENDING', '奖学金申请材料需要', NULL),
    (1002, TIMESTAMP '2026-03-19 15:00:00', TIMESTAMP '2026-03-20 09:00:00', 10002, '党员身份证明', 'COUNSELOR_APPROVED', '组织关系转接', '/exports/certificates/1002.pdf')
ON CONFLICT (id) DO NOTHING;

INSERT INTO wf_definition (
    id, created_at, updated_at, wf_code, wf_name, wf_type, business_type, is_active, is_deleted
)
VALUES
    (1, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 'AFFAIR_READ_CERT', '在读证明申请流', 'CERTIFICATE', 'affair_request', 1, 0),
    (4, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 'AFFAIR_PARTY_IDENTITY_CERT', '党员身份证明申请流', 'CERTIFICATE', 'affair_request', 1, 0),
    (5, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 'AFFAIR_DIFFICULTY_CERT', '困难认定证明申请流', 'CERTIFICATE', 'affair_request', 1, 0),
    (6, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 'AFFAIR_TRANSCRIPT_CERT', '成绩单申请流', 'CERTIFICATE', 'affair_request', 1, 0),
    (7, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 'AFFAIR_INTERNSHIP_CERT', '实习证明申请流', 'CERTIFICATE', 'affair_request', 1, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO wf_node (
    id, created_at, updated_at, wf_id, seq_no, node_name, approver_role, approver_user_id, sla_hours, allow_reject, is_deleted
)
VALUES
    (11, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 1, 1, '提交申请', 'STUDENT', NULL, 0, FALSE, 0),
    (12, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 1, 2, '辅导员审批', 'COUNSELOR', 20001, 24, TRUE, 0),
    (13, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 1, 3, '教务处审批', 'COLLEGE_ADMIN', 1, 48, TRUE, 0),
    (14, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 1, 4, '完成', 'SYSTEM', NULL, 0, FALSE, 0),
    (41, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 4, 1, '提交申请', 'STUDENT', NULL, 0, FALSE, 0),
    (42, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 4, 2, '辅导员审批', 'COUNSELOR', 20001, 24, TRUE, 0),
    (43, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 4, 3, '学院党务终审', 'COLLEGE_ADMIN', 1, 48, TRUE, 0),
    (44, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 4, 4, '完成', 'SYSTEM', NULL, 0, FALSE, 0),
    (51, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 5, 1, '提交申请', 'STUDENT', NULL, 0, FALSE, 0),
    (52, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 5, 2, '辅导员审批', 'COUNSELOR', 20001, 24, TRUE, 0),
    (53, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 5, 3, '资助中心复核', 'COLLEGE_ADMIN', 1, 48, TRUE, 0),
    (54, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 5, 4, '完成', 'SYSTEM', NULL, 0, FALSE, 0),
    (61, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 6, 1, '提交申请', 'STUDENT', NULL, 0, FALSE, 0),
    (62, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 6, 2, '辅导员审批', 'COUNSELOR', 20001, 24, TRUE, 0),
    (63, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 6, 3, '教务处审批', 'COLLEGE_ADMIN', 1, 48, TRUE, 0),
    (64, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 6, 4, '完成', 'SYSTEM', NULL, 0, FALSE, 0),
    (71, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 7, 1, '提交申请', 'STUDENT', NULL, 0, FALSE, 0),
    (72, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 7, 2, '辅导员审批', 'COUNSELOR', 20001, 24, TRUE, 0),
    (73, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 7, 3, '实践教学审核', 'COLLEGE_ADMIN', 1, 48, TRUE, 0),
    (74, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 7, 4, '完成', 'SYSTEM', NULL, 0, FALSE, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO affair_request (
    id, requester_user_id, request_type, title, purpose, remark, payload_json, status,
    submitted_at, closed_at, created_at, updated_at, is_deleted
)
VALUES
    (1001, 10001, 'certificate', '在读证明申请', '奖学金申请材料需要', '奖学金申请材料需要', '{"certificateType":"在读证明"}', 'submitted', TIMESTAMP '2026-03-20 10:30:00', NULL, TIMESTAMP '2026-03-20 10:30:00', TIMESTAMP '2026-03-20 10:30:00', 0),
    (1002, 10002, 'certificate', '党员身份证明申请', '组织关系转接', '组织关系转接', '{"certificateType":"党员身份证明"}', 'in_review', TIMESTAMP '2026-03-19 15:00:00', NULL, TIMESTAMP '2026-03-19 15:00:00', TIMESTAMP '2026-03-20 09:00:00', 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO cert_application (
    id, request_id, template_id, generated_pdf_file_id, student_snapshot_json, created_at, updated_at
)
VALUES
    (1001, 1001, 1, NULL, '{"studentNo":"2023100001","fullName":"张三"}', TIMESTAMP '2026-03-20 10:30:00', TIMESTAMP '2026-03-20 10:30:00'),
    (1002, 1002, 2, NULL, '{"studentNo":"2023100002","fullName":"李四"}', TIMESTAMP '2026-03-19 15:00:00', TIMESTAMP '2026-03-20 09:00:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO wf_instance (
    id, wf_id, business_table, business_id, status, started_by, started_at, finished_at, created_at, updated_at
)
VALUES
    (1, 1, 'campus.affair_request', 1001, 'running', 10001, TIMESTAMP '2026-03-20 10:30:00', NULL, TIMESTAMP '2026-03-20 10:30:00', TIMESTAMP '2026-03-20 10:30:00'),
    (2, 4, 'campus.affair_request', 1002, 'running', 10002, TIMESTAMP '2026-03-19 15:00:00', NULL, TIMESTAMP '2026-03-19 15:00:00', TIMESTAMP '2026-03-20 09:00:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO wf_task (
    id, wf_instance_id, wf_node_id, assignee_user_id, status, due_at, completed_at, created_at, updated_at
)
VALUES
    (1201, 1, 12, 20001, 'pending', TIMESTAMP '2026-03-21 10:30:00', NULL, TIMESTAMP '2026-03-20 10:30:00', TIMESTAMP '2026-03-20 10:30:00'),
    (4201, 2, 42, 20001, 'approved', TIMESTAMP '2026-03-20 15:00:00', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-19 15:00:00', TIMESTAMP '2026-03-20 09:00:00'),
    (4202, 2, 43, 1, 'pending', TIMESTAMP '2026-03-22 09:00:00', NULL, TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO wf_task_action (
    id, wf_task_id, actor_user_id, action, action_comment, action_at, created_at
)
VALUES
    (3001, 4201, 20001, 'approve', '初审通过', TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO approval_action_log (
    id, created_at, updated_at, request_id, operator_id, operator_name, operator_role, action, from_status, to_status, comment
)
VALUES
    (3001, TIMESTAMP '2026-03-20 09:00:00', TIMESTAMP '2026-03-20 09:00:00', 1002, 20001, '胡浩老师', 'COUNSELOR', 'approve', 'PENDING', 'COUNSELOR_APPROVED', '初审通过')
ON CONFLICT (id) DO NOTHING;

INSERT INTO party_flow (
    id, created_at, updated_at, flow_code, flow_name, flow_type, is_active, is_deleted
)
VALUES
    (1, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 'PARTY_JOIN', '入党流程', 'PARTY', 1, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO party_flow_node (
    id, created_at, updated_at, flow_id, seq_no, node_code, node_name, description, expected_days, reminder_offset_days, overdue_days, is_deleted
)
VALUES
    (101, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 1, 1, 'APPLICANT', '入党申请人', '提交申请书并完成基础登记', 30, 7, 30, 0),
    (102, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 1, 2, 'PARTY_CLASS', '党课学习小组', '完成党课学习与考核', 30, 7, 30, 0),
    (103, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 1, 3, 'ACTIVIST', '积极分子', '培养期满 3 个月提交思想汇报', 90, 7, 90, 0),
    (104, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 1, 4, 'DEVELOPMENT_TARGET', '发展对象', '按学期节点完成推优与答辩', 90, 14, 90, 0),
    (105, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 1, 5, 'PROBATIONARY', '预备党员', '支部审批通过后进入预备期', 180, 14, 180, 0),
    (106, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 1, 6, 'FORMAL', '正式党员', '预备期满后转正', 365, 30, 365, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO party_student_progress (
    id, student_user_id, flow_id, current_node_id, status, started_at, updated_node_at, next_deadline_at, created_at, updated_at, is_deleted
)
VALUES
    (1, 10001, 1, 103, 'in_progress', TIMESTAMP '2026-04-27 00:00:00', TIMESTAMP '2026-04-27 00:00:00', TIMESTAMP '2026-07-26 00:00:00', TIMESTAMP '2026-04-27 00:00:00', TIMESTAMP '2026-04-27 00:00:00', 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO party_reminder_task (
    id, progress_id, node_id, due_at, channel, status, sent_at, created_at, updated_at
)
VALUES
    (1, 1, 103, TIMESTAMP '2026-07-26 00:00:00', 'miniprogram', 'pending', NULL, TIMESTAMP '2026-05-01 09:00:00', TIMESTAMP '2026-05-01 09:00:00'),
    (2, 1, 104, TIMESTAMP '2026-10-27 00:00:00', 'email', 'pending', NULL, TIMESTAMP '2026-05-01 09:05:00', TIMESTAMP '2026-05-01 09:05:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO party_progress_record (
    id, created_at, updated_at, student_id, current_stage, stage_start_date, completed_actions, next_action
)
VALUES
    (1, TIMESTAMP '2026-04-27 00:00:00', TIMESTAMP '2026-04-27 00:00:00', 10001, '积极分子', DATE '2026-04-27', '已提交入党申请书；已参加党课学习小组；已完成基础培训', '满培养期后进入发展对象推优准备')
ON CONFLICT (id) DO NOTHING;

INSERT INTO kb_qa_ticket (
    id, ask_user_id, ask_username, ask_name, question_text, status, matched_faq_id, handled_by, handled_at, created_at, updated_at
)
VALUES
    (101, 10001, '2023100001', '张三', '流程节点如何管理？', 'OPEN', NULL, NULL, NULL, TIMESTAMP '2026-03-22 09:00:00', TIMESTAMP '2026-03-22 09:00:00'),
    (102, 10002, '2023100002', '李四', '关于学校专业的要求', 'IN_PROGRESS', NULL, 20001, TIMESTAMP '2026-03-22 10:00:00', TIMESTAMP '2026-03-22 09:30:00', TIMESTAMP '2026-03-22 10:00:00'),
    (103, 10001, '2023100001', '王五', '实习证明怎么开', 'CLOSED', NULL, 20001, TIMESTAMP '2026-03-22 11:00:00', TIMESTAMP '2026-03-22 10:30:00', TIMESTAMP '2026-03-22 11:00:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO kb_qa_ticket_message (id, ticket_id, actor_name, actor_role, message_text, created_at)
VALUES
    (1001, 102, '赵老师', 'COUNSELOR', '您好，关于专业的要求是以学院最新培养方案和教务通知为准。', TIMESTAMP '2026-03-22 10:05:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO academic_warning_record (
    id, created_at, updated_at, student_id, module_name, required_credits, earned_credits, recommended_courses
)
VALUES
    (1, TIMESTAMP '2026-03-21 09:00:00', TIMESTAMP '2026-03-21 09:00:00', 10001, '专业核心课', 18, 12, '数据结构、操作系统'),
    (2, TIMESTAMP '2026-03-21 09:05:00', TIMESTAMP '2026-03-21 09:05:00', 10001, '通识选修', 8, 4, '艺术鉴赏、社会研究方法')
ON CONFLICT (id) DO NOTHING;

INSERT INTO aca_program (
    id, program_code, major_name, grade_year, version_name, description, is_active, created_by, ext_json,
    created_at, updated_at, is_deleted
)
VALUES
    (1, 'CS-2023', '计算机类', 2023, '2023版', '2023级计算机类培养方案', 1, 1, NULL, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO aca_program_module (
    id, program_id, module_code, module_name, module_type, required_credits, created_at, updated_at, is_deleted
)
VALUES
    (1, 1, 'CORE', '专业核心课', 'required', 18.00, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 0),
    (2, 1, 'GENERAL', '通识选修', 'general', 8.00, TIMESTAMP '2026-03-01 00:00:00', TIMESTAMP '2026-03-01 00:00:00', 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO aca_course (
    id, course_code, course_name, credits, course_type, created_at, updated_at, is_deleted
)
VALUES
    (1, 'CS101', '计算机导论', 3.00, 'required', TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0),
    (2, 'CS201', '数据结构', 4.00, 'required', TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0),
    (3, 'CS301', '操作系统', 4.00, 'required', TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0),
    (4, 'GE201', '艺术鉴赏', 2.00, 'general', TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0),
    (5, 'GE202', '社会研究方法', 2.00, 'general', TIMESTAMP '2024-01-01 00:00:00', TIMESTAMP '2024-01-01 00:00:00', 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO aca_term (
    id, term_code, term_name, academic_year, semester_no, start_date, end_date, is_current, created_at, updated_at, is_deleted
)
VALUES
    (1, '2024-2025-1', '2024-2025学年第一学期', '2024-2025', 1, DATE '2024-09-01', DATE '2025-01-20', 1, TIMESTAMP '2024-09-01 00:00:00', TIMESTAMP '2024-09-01 00:00:00', 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO aca_term_course (
    id, term_id, course_id, teaching_class_code, course_code, course_name, teacher_name, course_location,
    credits, total_hours, created_at, updated_at, is_deleted
)
VALUES
    (1, 1, 2, 'CS201-01', 'CS201', '数据结构', '李老师', '教学楼B201', 4.00, 64, TIMESTAMP '2024-09-01 00:00:00', TIMESTAMP '2024-09-01 00:00:00', 0),
    (2, 1, 3, 'CS301-01', 'CS301', '操作系统', '王老师', '教学楼B301', 4.00, 64, TIMESTAMP '2024-09-01 00:00:00', TIMESTAMP '2024-09-01 00:00:00', 0),
    (3, 1, 4, 'GE201-01', 'GE201', '艺术鉴赏', '张老师', '教学楼C201', 2.00, 32, TIMESTAMP '2024-09-01 00:00:00', TIMESTAMP '2024-09-01 00:00:00', 0),
    (4, 1, 5, 'GE202-01', 'GE202', '社会研究方法', '赵老师', '教学楼C301', 2.00, 32, TIMESTAMP '2024-09-01 00:00:00', TIMESTAMP '2024-09-01 00:00:00', 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO aca_transcript (
    id, student_user_id, source_file_id, parse_status, parsed_at, total_credits, gpa, created_at, updated_at
)
VALUES
    (1, 10001, 5501, 'parsed', TIMESTAMP '2026-03-21 09:00:00', 16.00, 3.82, TIMESTAMP '2026-03-21 09:00:00', TIMESTAMP '2026-03-21 09:00:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO aca_audit_report (
    id, student_user_id, program_id, transcript_id, status, missing_module_count, generated_at, created_at, updated_at
)
VALUES
    (1, 10001, 1, 1, 'generated', 2, TIMESTAMP '2026-03-21 09:10:00', TIMESTAMP '2026-03-21 09:10:00', TIMESTAMP '2026-03-21 09:10:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO aca_audit_missing (id, report_id, module_id, missing_credits, created_at)
VALUES
    (1, 1, 1, 6.00, TIMESTAMP '2026-03-21 09:10:00'),
    (2, 1, 2, 4.00, TIMESTAMP '2026-03-21 09:10:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO aca_course_recommendation (id, report_id, module_id, course_id, recommendation_reason, created_at)
VALUES
    (1, 1, 1, 1, '优先补修数据结构基础课程', TIMESTAMP '2026-03-21 09:10:00'),
    (2, 1, 1, 2, '核心模块缺口需优先补齐操作系统相关课程', TIMESTAMP '2026-03-21 09:10:00'),
    (3, 1, 2, 3, '通识选修可补修艺术鉴赏', TIMESTAMP '2026-03-21 09:10:00'),
    (4, 1, 2, 4, '通识选修可补修社会研究方法', TIMESTAMP '2026-03-21 09:10:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO login_audit_log (
    id, created_at, updated_at, user_id, username, role, action, result, detail
)
VALUES
    (5001, TIMESTAMP '2026-03-24 09:00:00', TIMESTAMP '2026-03-24 09:00:00', 1, 'admin', 'SUPER_ADMIN', 'LOGIN', 'SUCCESS', NULL),
    (5002, TIMESTAMP '2026-03-24 10:00:00', TIMESTAMP '2026-03-24 10:00:00', 10001, '2023100001', 'STUDENT', 'LOGOUT', 'SUCCESS', NULL)
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_session_record (
    id, created_at, updated_at, token_hash, user_id, username, role, login_at, logout_at, active
)
VALUES
    (8001, TIMESTAMP '2026-03-24 09:00:00', TIMESTAMP '2026-03-24 09:00:00', 'mock-session-admin-8001', 1, 'admin', 'SUPER_ADMIN', TIMESTAMP '2026-03-24 09:00:00', NULL, TRUE),
    (8002, TIMESTAMP '2026-03-24 09:30:00', TIMESTAMP '2026-03-24 10:00:00', 'mock-session-student-8002', 10001, '2023100001', 'STUDENT', TIMESTAMP '2026-03-24 09:30:00', TIMESTAMP '2026-03-24 10:00:00', FALSE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO student_work_log (
    id, created_at, updated_at, student_id, student_name, category, title, description,
    workload_score, work_date, recorder_name, recorder_role
)
VALUES
    (501, TIMESTAMP '2026-03-10 18:00:00', TIMESTAMP '2026-03-10 18:00:00', 10001, '张三', '党团事务', '党课学习小组签到组织', '负责签到与材料整理', 3, DATE '2026-03-10', '胡浩老师', 'COUNSELOR'),
    (502, TIMESTAMP '2026-03-15 20:00:00', TIMESTAMP '2026-03-15 20:00:00', 10001, '张三', '学生工作', '活动通知收集', '汇总班级反馈情况', 2, DATE '2026-03-15', '李四', 'LEAGUE_SECRETARY')
ON CONFLICT (id) DO NOTHING;

INSERT INTO student_work_log_action_log (
    id, created_at, updated_at, work_log_id, operator_id, operator_name, operator_role, action, detail
)
VALUES
    (901, TIMESTAMP '2026-03-10 18:00:00', TIMESTAMP '2026-03-10 18:00:00', 501, 20001, '胡浩老师', 'COUNSELOR', 'CREATE', '创建工作记录'),
    (902, TIMESTAMP '2026-03-15 20:00:00', TIMESTAMP '2026-03-15 20:00:00', 502, 10002, '李四', 'LEAGUE_SECRETARY', 'CREATE', '创建工作记录')
ON CONFLICT (id) DO NOTHING;

INSERT INTO data_import_task (
    id, created_at, updated_at, task_type, file_name, status, total_rows, success_rows, failed_rows, owner_id, owner_name, error_summary
)
VALUES
    (1, TIMESTAMP '2026-03-20 16:00:00', TIMESTAMP '2026-03-20 16:00:00', 'STUDENT_PROFILE', 'students-info.xlsx', 'SUCCESS', 1086, 1086, 0, 1, '系统管理员', NULL),
    (2, TIMESTAMP '2026-03-23 09:00:00', TIMESTAMP '2026-03-23 09:00:00', 'KNOWLEDGE_BASE', 'party-knowledge.xlsx', 'PARTIAL_SUCCESS', 28, 26, 2, 20001, '胡浩老师', '2 行数据校验失败')
ON CONFLICT (id) DO NOTHING;

INSERT INTO data_import_error_item (
    id, created_at, updated_at, task_id, row_number, field_name, error_message, raw_value
)
VALUES
    (81, TIMESTAMP '2026-03-23 09:05:00', TIMESTAMP '2026-03-23 09:05:00', 2, 7, 'title', '标题为空', NULL),
    (82, TIMESTAMP '2026-03-23 09:06:00', TIMESTAMP '2026-03-23 09:06:00', 2, 12, 'officialUrl', 'URL 格式不合法', 'htp://bad-url')
ON CONFLICT (id) DO NOTHING;

INSERT INTO admin_operation_log (
    id, created_at, updated_at, operator_id, operator_name, operator_role, module, action, target, result, detail
)
VALUES
    (1, TIMESTAMP '2024-03-28 14:30:25', TIMESTAMP '2024-03-28 14:30:25', 1, '张老师', 'ADMIN', 'USER', 'UPDATE', '编辑用户', 'SUCCESS', '编辑用户'),
    (2, TIMESTAMP '2024-03-28 14:25:18', TIMESTAMP '2024-03-28 14:25:18', 2, '李老师', 'ADMIN', 'APPROVAL', 'APPROVE', '通过审批', 'SUCCESS', '通过审批'),
    (3, TIMESTAMP '2024-03-28 14:20:42', TIMESTAMP '2024-03-28 14:20:42', 3, '王老师', 'ADMIN', 'KNOWLEDGE', 'PUBLISH', '发布政策', 'SUCCESS', '发布政策'),
    (4, TIMESTAMP '2024-03-28 14:15:30', TIMESTAMP '2024-03-28 14:15:30', 4, '赵老师', 'ADMIN', 'NOTICE', 'SEND', '发送通知', 'FAILED', '发送通知失败'),
    (5, TIMESTAMP '2024-03-28 14:10:15', TIMESTAMP '2024-03-28 14:10:15', 5, '钱老师', 'ADMIN', 'NOTICE', 'SEND', '发送通知', 'FAILED', '发送通知失败'),
    (6, TIMESTAMP '2026-03-23 10:10:00', TIMESTAMP '2026-03-23 10:10:00', 20001, '胡浩老师', 'COUNSELOR', 'APPROVAL', 'APPROVE', '证明申请#1001', 'SUCCESS', 'fromStatus=PENDING,toStatus=COUNSELOR_APPROVED')
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_operation_log (
    id, module_code, business_type, business_id, operation_type, operation_desc, operator_user_id,
    trace_id, request_uri, request_method, request_ip, user_agent, log_level, result_status,
    error_message, ext_json, created_at
)
VALUES
    (1, 'approval', 'cert_application', 1001, 'approve', '证明申请#1001', 20001,
     'trace-approval-1001', '/api/v1/admin/approvals/1001/actions', 'POST', '192.168.1.110', NULL, 'audit', 'success',
     NULL, '{"operatorName":"胡浩老师","operatorRole":"COUNSELOR","target":"证明申请#1001"}', TIMESTAMP '2026-03-23 10:10:00'),
    (2, 'knowledge', 'kb_policy', 299, 'publish', '辅导员内部口径说明', 1,
     'trace-knowledge-299', '/api/v1/admin/knowledge/299/publish', 'POST', '192.168.1.100', NULL, 'audit', 'success',
     NULL, '{"operatorName":"系统管理员","operatorRole":"SUPER_ADMIN","target":"知识条目#299"}', TIMESTAMP '2026-03-22 11:05:00')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('sys_role', 'id'), COALESCE((SELECT MAX(id) FROM sys_role), 1), true);
SELECT setval(pg_get_serial_sequence('user_account', 'id'), COALESCE((SELECT MAX(id) FROM user_account), 1), true);
SELECT setval(pg_get_serial_sequence('sys_user', 'id'), COALESCE((SELECT MAX(id) FROM sys_user), 1), true);
SELECT setval(pg_get_serial_sequence('sys_user_role', 'id'), COALESCE((SELECT MAX(id) FROM sys_user_role), 1), true);
SELECT setval(pg_get_serial_sequence('sys_user_auth', 'id'), COALESCE((SELECT MAX(id) FROM sys_user_auth), 1), true);
SELECT setval(pg_get_serial_sequence('student_profile', 'id'), COALESCE((SELECT MAX(id) FROM student_profile), 1), true);
SELECT setval(pg_get_serial_sequence('student_portrait', 'id'), COALESCE((SELECT MAX(id) FROM student_portrait), 1), true);
SELECT setval(pg_get_serial_sequence('advisor_scope_binding', 'id'), COALESCE((SELECT MAX(id) FROM advisor_scope_binding), 1), true);
SELECT setval(pg_get_serial_sequence('student_status_history', 'id'), COALESCE((SELECT MAX(id) FROM student_status_history), 1), true);
SELECT setval(pg_get_serial_sequence('student_award_support_record', 'id'), COALESCE((SELECT MAX(id) FROM student_award_support_record), 1), true);
SELECT setval(pg_get_serial_sequence('student_competition_record', 'id'), COALESCE((SELECT MAX(id) FROM student_competition_record), 1), true);
SELECT setval(pg_get_serial_sequence('student_innovation_entrepreneurship_record', 'id'), COALESCE((SELECT MAX(id) FROM student_innovation_entrepreneurship_record), 1), true);
SELECT setval(pg_get_serial_sequence('student_social_practice_record', 'id'), COALESCE((SELECT MAX(id) FROM student_social_practice_record), 1), true);
SELECT setval(pg_get_serial_sequence('student_student_work_record', 'id'), COALESCE((SELECT MAX(id) FROM student_student_work_record), 1), true);
SELECT setval(pg_get_serial_sequence('student_volunteer_service_record', 'id'), COALESCE((SELECT MAX(id) FROM student_volunteer_service_record), 1), true);
SELECT setval(pg_get_serial_sequence('student_skill_certificate_record', 'id'), COALESCE((SELECT MAX(id) FROM student_skill_certificate_record), 1), true);
SELECT setval(pg_get_serial_sequence('knowledge_document', 'id'), COALESCE((SELECT MAX(id) FROM knowledge_document), 1), true);
SELECT setval(pg_get_serial_sequence('file_object', 'id'), COALESCE((SELECT MAX(id) FROM file_object), 1), true);
SELECT setval(pg_get_serial_sequence('kb_policy', 'id'), COALESCE((SELECT MAX(id) FROM kb_policy), 1), true);
SELECT setval(pg_get_serial_sequence('knowledge_attachment', 'id'), COALESCE((SELECT MAX(id) FROM knowledge_attachment), 1), true);
SELECT setval(pg_get_serial_sequence('notice', 'id'), COALESCE((SELECT MAX(id) FROM notice), 1), true);
SELECT setval(pg_get_serial_sequence('notice_tag_dict', 'id'), COALESCE((SELECT MAX(id) FROM notice_tag_dict), 1), true);
SELECT setval(pg_get_serial_sequence('notice_item', 'id'), COALESCE((SELECT MAX(id) FROM notice_item), 1), true);
SELECT setval(pg_get_serial_sequence('notice_item_tag', 'id'), COALESCE((SELECT MAX(id) FROM notice_item_tag), 1), true);
SELECT setval(pg_get_serial_sequence('notice_delivery', 'id'), COALESCE((SELECT MAX(id) FROM notice_delivery), 1), true);
SELECT setval(pg_get_serial_sequence('notice_delivery_target', 'id'), COALESCE((SELECT MAX(id) FROM notice_delivery_target), 1), true);
SELECT setval(pg_get_serial_sequence('platform_notification_send_record', 'id'), COALESCE((SELECT MAX(id) FROM platform_notification_send_record), 1), true);
SELECT setval(pg_get_serial_sequence('platform_file_upload_record', 'id'), COALESCE((SELECT MAX(id) FROM platform_file_upload_record), 1), true);
SELECT setval(pg_get_serial_sequence('certificate_template', 'id'), COALESCE((SELECT MAX(id) FROM certificate_template), 1), true);
SELECT setval(pg_get_serial_sequence('cert_template', 'id'), COALESCE((SELECT MAX(id) FROM cert_template), 1), true);
SELECT setval(pg_get_serial_sequence('certificate_request', 'id'), COALESCE((SELECT MAX(id) FROM certificate_request), 1), true);
SELECT setval(pg_get_serial_sequence('wf_definition', 'id'), COALESCE((SELECT MAX(id) FROM wf_definition), 1), true);
SELECT setval(pg_get_serial_sequence('wf_node', 'id'), COALESCE((SELECT MAX(id) FROM wf_node), 1), true);
SELECT setval(pg_get_serial_sequence('affair_request', 'id'), COALESCE((SELECT MAX(id) FROM affair_request), 1), true);
SELECT setval(pg_get_serial_sequence('cert_application', 'id'), COALESCE((SELECT MAX(id) FROM cert_application), 1), true);
SELECT setval(pg_get_serial_sequence('wf_instance', 'id'), COALESCE((SELECT MAX(id) FROM wf_instance), 1), true);
SELECT setval(pg_get_serial_sequence('wf_task', 'id'), COALESCE((SELECT MAX(id) FROM wf_task), 1), true);
SELECT setval(pg_get_serial_sequence('wf_task_action', 'id'), COALESCE((SELECT MAX(id) FROM wf_task_action), 1), true);
SELECT setval(pg_get_serial_sequence('approval_action_log', 'id'), COALESCE((SELECT MAX(id) FROM approval_action_log), 1), true);
SELECT setval(pg_get_serial_sequence('party_flow', 'id'), COALESCE((SELECT MAX(id) FROM party_flow), 1), true);
SELECT setval(pg_get_serial_sequence('party_flow_node', 'id'), COALESCE((SELECT MAX(id) FROM party_flow_node), 1), true);
SELECT setval(pg_get_serial_sequence('party_student_progress', 'id'), COALESCE((SELECT MAX(id) FROM party_student_progress), 1), true);
SELECT setval(pg_get_serial_sequence('party_reminder_task', 'id'), COALESCE((SELECT MAX(id) FROM party_reminder_task), 1), true);
SELECT setval(pg_get_serial_sequence('party_progress_record', 'id'), COALESCE((SELECT MAX(id) FROM party_progress_record), 1), true);
SELECT setval(pg_get_serial_sequence('kb_qa_ticket', 'id'), COALESCE((SELECT MAX(id) FROM kb_qa_ticket), 1), true);
SELECT setval(pg_get_serial_sequence('kb_qa_ticket_message', 'id'), COALESCE((SELECT MAX(id) FROM kb_qa_ticket_message), 1), true);
SELECT setval(pg_get_serial_sequence('academic_warning_record', 'id'), COALESCE((SELECT MAX(id) FROM academic_warning_record), 1), true);
SELECT setval(pg_get_serial_sequence('aca_program', 'id'), COALESCE((SELECT MAX(id) FROM aca_program), 1), true);
SELECT setval(pg_get_serial_sequence('aca_program_module', 'id'), COALESCE((SELECT MAX(id) FROM aca_program_module), 1), true);
SELECT setval(pg_get_serial_sequence('aca_course', 'id'), COALESCE((SELECT MAX(id) FROM aca_course), 1), true);
SELECT setval(pg_get_serial_sequence('aca_term', 'id'), COALESCE((SELECT MAX(id) FROM aca_term), 1), true);
SELECT setval(pg_get_serial_sequence('aca_term_course', 'id'), COALESCE((SELECT MAX(id) FROM aca_term_course), 1), true);
SELECT setval(pg_get_serial_sequence('aca_transcript', 'id'), COALESCE((SELECT MAX(id) FROM aca_transcript), 1), true);
SELECT setval(pg_get_serial_sequence('aca_audit_report', 'id'), COALESCE((SELECT MAX(id) FROM aca_audit_report), 1), true);
SELECT setval(pg_get_serial_sequence('aca_audit_missing', 'id'), COALESCE((SELECT MAX(id) FROM aca_audit_missing), 1), true);
SELECT setval(pg_get_serial_sequence('aca_course_recommendation', 'id'), COALESCE((SELECT MAX(id) FROM aca_course_recommendation), 1), true);
SELECT setval(pg_get_serial_sequence('login_audit_log', 'id'), COALESCE((SELECT MAX(id) FROM login_audit_log), 1), true);
SELECT setval(pg_get_serial_sequence('user_session_record', 'id'), COALESCE((SELECT MAX(id) FROM user_session_record), 1), true);
SELECT setval(pg_get_serial_sequence('student_work_log', 'id'), COALESCE((SELECT MAX(id) FROM student_work_log), 1), true);
SELECT setval(pg_get_serial_sequence('student_work_log_action_log', 'id'), COALESCE((SELECT MAX(id) FROM student_work_log_action_log), 1), true);
SELECT setval(pg_get_serial_sequence('data_import_task', 'id'), COALESCE((SELECT MAX(id) FROM data_import_task), 1), true);
SELECT setval(pg_get_serial_sequence('data_import_error_item', 'id'), COALESCE((SELECT MAX(id) FROM data_import_error_item), 1), true);
SELECT setval(pg_get_serial_sequence('admin_operation_log', 'id'), COALESCE((SELECT MAX(id) FROM admin_operation_log), 1), true);
SELECT setval(pg_get_serial_sequence('sys_operation_log', 'id'), COALESCE((SELECT MAX(id) FROM sys_operation_log), 1), true);
