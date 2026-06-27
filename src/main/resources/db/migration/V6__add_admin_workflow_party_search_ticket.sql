CREATE TABLE wf_definition (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    wf_code VARCHAR(64) NOT NULL UNIQUE,
    wf_name VARCHAR(128) NOT NULL,
    wf_type VARCHAR(32) NOT NULL,
    business_type VARCHAR(64) NOT NULL,
    is_active INTEGER NOT NULL DEFAULT 1,
    is_deleted INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE wf_node (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    wf_id BIGINT NOT NULL,
    seq_no INTEGER NOT NULL,
    node_name VARCHAR(128) NOT NULL,
    approver_role VARCHAR(32),
    approver_user_id BIGINT,
    sla_hours INTEGER,
    allow_reject BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted INTEGER NOT NULL DEFAULT 0,
    UNIQUE (wf_id, seq_no)
);

CREATE INDEX idx_wf_node_wf ON wf_node(wf_id, seq_no);

CREATE TABLE party_flow (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    flow_code VARCHAR(64) NOT NULL UNIQUE,
    flow_name VARCHAR(128) NOT NULL,
    flow_type VARCHAR(32) NOT NULL,
    is_active INTEGER NOT NULL DEFAULT 1,
    is_deleted INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE party_flow_node (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    flow_id BIGINT NOT NULL,
    seq_no INTEGER NOT NULL,
    node_code VARCHAR(64) NOT NULL,
    node_name VARCHAR(128) NOT NULL,
    description TEXT,
    expected_days INTEGER,
    reminder_offset_days INTEGER,
    overdue_days INTEGER,
    is_deleted INTEGER NOT NULL DEFAULT 0,
    UNIQUE (flow_id, seq_no),
    UNIQUE (flow_id, node_code)
);

CREATE INDEX idx_party_flow_node_flow ON party_flow_node(flow_id, seq_no);

CREATE TABLE search_query_log (
    id BIGSERIAL PRIMARY KEY,
    keyword VARCHAR(200) NOT NULL,
    result_count INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_search_query_log_created ON search_query_log(created_at);

CREATE TABLE kb_qa_ticket (
    id BIGSERIAL PRIMARY KEY,
    ask_user_id BIGINT,
    ask_username VARCHAR(64),
    ask_name VARCHAR(64),
    question_text TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    matched_faq_id BIGINT,
    handled_by BIGINT,
    handled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_kb_qa_ticket_status ON kb_qa_ticket(status, created_at);

CREATE TABLE kb_qa_ticket_message (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    actor_name VARCHAR(64),
    actor_role VARCHAR(32),
    message_text TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_kb_qa_ticket_message_ticket ON kb_qa_ticket_message(ticket_id, created_at);

