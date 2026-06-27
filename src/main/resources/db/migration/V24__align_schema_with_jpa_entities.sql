-- Align Flyway-managed schema with JPA @Entity mappings.
-- Target: Postgres/Kingbase. Convert timestamptz columns to timestamp to match LocalDateTime.

-- knowledge_document: align with edu.ruc.platform.knowledge.domain.KnowledgeDocument
ALTER TABLE knowledge_document
    ADD COLUMN IF NOT EXISTS summary VARCHAR(500);

ALTER TABLE knowledge_document
    ALTER COLUMN content TYPE TEXT;

ALTER TABLE knowledge_document
    ALTER COLUMN title TYPE VARCHAR(200);

ALTER TABLE knowledge_document
    ALTER COLUMN category TYPE VARCHAR(100);

ALTER TABLE knowledge_document
    ALTER COLUMN official_url TYPE VARCHAR(500);

ALTER TABLE knowledge_document
    ALTER COLUMN source_file_name TYPE VARCHAR(255);

ALTER TABLE knowledge_document
    ALTER COLUMN audience_scope TYPE VARCHAR(64);

ALTER TABLE knowledge_document
    ALTER COLUMN updated_by TYPE VARCHAR(64);

-- V20/V21/V22/V23 used "timestamp(6) with time zone" in multiple tables, while JPA uses LocalDateTime.
-- Convert them to "timestamp(6) without time zone" using UTC to avoid environment-dependent shifts.

ALTER TABLE wf_instance
    ALTER COLUMN started_at TYPE timestamp(6) USING started_at AT TIME ZONE 'UTC',
    ALTER COLUMN finished_at TYPE timestamp(6) USING finished_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE wf_task
    ALTER COLUMN due_at TYPE timestamp(6) USING due_at AT TIME ZONE 'UTC',
    ALTER COLUMN completed_at TYPE timestamp(6) USING completed_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE wf_task_action
    ALTER COLUMN action_at TYPE timestamp(6) USING action_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC';

ALTER TABLE affair_request
    ALTER COLUMN submitted_at TYPE timestamp(6) USING submitted_at AT TIME ZONE 'UTC',
    ALTER COLUMN closed_at TYPE timestamp(6) USING closed_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE cert_template
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE cert_template_keyword
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC';

ALTER TABLE cert_application
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE cert_generated_file
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC';

ALTER TABLE aca_program
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE aca_program_module
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE aca_course
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE aca_term
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE aca_term_course
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE aca_module_course
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC';

ALTER TABLE aca_transcript
    ALTER COLUMN parsed_at TYPE timestamp(6) USING parsed_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE aca_transcript_item
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC';

ALTER TABLE aca_audit_report
    ALTER COLUMN generated_at TYPE timestamp(6) USING generated_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE aca_audit_missing
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC';

ALTER TABLE aca_course_recommendation
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC';

ALTER TABLE audit_import_job
    ALTER COLUMN started_at TYPE timestamp(6) USING started_at AT TIME ZONE 'UTC',
    ALTER COLUMN finished_at TYPE timestamp(6) USING finished_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE sys_role
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE sys_user
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE sys_user_role
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC';

ALTER TABLE sys_user_auth
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE sys_student_ext
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE sys_operation_log
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC';

ALTER TABLE file_object
    ALTER COLUMN uploaded_at TYPE timestamp(6) USING uploaded_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE kb_policy
    ALTER COLUMN published_at TYPE timestamp(6) USING published_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE kb_faq
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE kb_keyword
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC';

ALTER TABLE kb_policy_keyword
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC';

ALTER TABLE kb_faq_keyword
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC';

ALTER TABLE kb_keyword_search_history
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC';

ALTER TABLE party_student_progress
    ALTER COLUMN started_at TYPE timestamp(6) USING started_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_node_at TYPE timestamp(6) USING updated_node_at AT TIME ZONE 'UTC',
    ALTER COLUMN next_deadline_at TYPE timestamp(6) USING next_deadline_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE party_reminder_task
    ALTER COLUMN due_at TYPE timestamp(6) USING due_at AT TIME ZONE 'UTC',
    ALTER COLUMN sent_at TYPE timestamp(6) USING sent_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE notice_tag_dict
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE notice_item
    ALTER COLUMN publish_at TYPE timestamp(6) USING publish_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE notice_item_tag
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC';

ALTER TABLE notice_delivery
    ALTER COLUMN scheduled_at TYPE timestamp(6) USING scheduled_at AT TIME ZONE 'UTC',
    ALTER COLUMN sent_at TYPE timestamp(6) USING sent_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE notice_delivery_target
    ALTER COLUMN sent_at TYPE timestamp(6) USING sent_at AT TIME ZONE 'UTC',
    ALTER COLUMN read_at TYPE timestamp(6) USING read_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE timestamp(6) USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp(6) USING updated_at AT TIME ZONE 'UTC';
