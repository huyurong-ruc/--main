INSERT INTO student_status_history (student_id, from_status, to_status, changed_to_major, reason, changed_by, changed_by_role)
VALUES
    (10001, NULL, 'ACTIVE', NULL, '初始建档', '系统管理员', 'SUPER_ADMIN')
ON CONFLICT DO NOTHING;
