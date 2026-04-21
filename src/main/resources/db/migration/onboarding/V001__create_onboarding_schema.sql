-- =============================================================================
-- Onboarding Schema
-- Creates the onboarding schema and all tables owned by this module.
-- Other modules must not write to these tables directly.
-- Merchant Core identity is referenced only by merchant_id UUID value.
-- There are no foreign keys to merchant_core tables.
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS onboarding;


-- -----------------------------------------------------------------------------
-- onboarding.merchant_application
-- Root workflow record for a merchant onboarding application.
--
-- version_no supports optimistic locking: the application layer reads the
-- current version and must include it when writing. If another process
-- updated the row first, the version will not match and the write is rejected.
-- This prevents two concurrent reviewers from overwriting each other's changes.
--
-- merchant_id is null until a Merchant Core record has been reserved.
-- prospect_reference is used before that reservation. At least one must be set.
-- No foreign key to merchant_core.merchant is added here — Onboarding must
-- not couple its schema to another module's tables.
-- -----------------------------------------------------------------------------
CREATE TABLE onboarding.merchant_application (
    application_id       UUID                     NOT NULL,
    merchant_id          UUID,                              -- null until Merchant Core record is reserved
    prospect_reference   VARCHAR(120),                      -- null once merchant_id is assigned
    application_status   VARCHAR(40)              NOT NULL,
    requested_products   TEXT,                              -- stored as a serialised list
    submitted_at         TIMESTAMP WITH TIME ZONE,
    assigned_analyst_id  UUID,
    target_go_live_date  DATE,
    version_no           BIGINT                   NOT NULL DEFAULT 0,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_merchant_application
        PRIMARY KEY (application_id),

    -- At least one of merchant_id or prospect_reference must identify the applicant.
    CONSTRAINT chk_application_has_identifier
        CHECK (merchant_id IS NOT NULL OR (prospect_reference IS NOT NULL AND prospect_reference <> '')),

    CONSTRAINT chk_application_status
        CHECK (application_status IN (
            'DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'NEEDS_INFO',
            'APPROVED', 'REJECTED', 'ACTIVATED'
        ))
);

CREATE INDEX idx_merchant_application_merchant_id       ON onboarding.merchant_application (merchant_id);
CREATE INDEX idx_merchant_application_status            ON onboarding.merchant_application (application_status);
CREATE INDEX idx_merchant_application_analyst_id        ON onboarding.merchant_application (assigned_analyst_id);


-- -----------------------------------------------------------------------------
-- onboarding.application_review
-- A formal review checkpoint for a merchant application.
-- A single application may have many reviews of different types over time.
-- -----------------------------------------------------------------------------
CREATE TABLE onboarding.application_review (
    review_id             UUID                     NOT NULL,
    application_id        UUID                     NOT NULL,
    review_type           VARCHAR(60)              NOT NULL,
    review_status         VARCHAR(40)              NOT NULL,
    reviewer_id           UUID,                              -- null while PENDING
    review_notes_summary  TEXT,                              -- null while PENDING
    decided_at            TIMESTAMP WITH TIME ZONE,          -- null while PENDING
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_application_review
        PRIMARY KEY (review_id),

    CONSTRAINT fk_review_application_id
        FOREIGN KEY (application_id)
        REFERENCES onboarding.merchant_application (application_id),

    CONSTRAINT chk_review_type
        CHECK (review_type IN (
            'BUSINESS_PROFILE', 'DOCUMENT_CHECK', 'RISK_REVIEW', 'OPERATIONS_REVIEW'
        )),

    CONSTRAINT chk_review_status
        CHECK (review_status IN ('PENDING', 'PASSED', 'FAILED', 'NEEDS_INFO'))
);

CREATE INDEX idx_application_review_application_id ON onboarding.application_review (application_id);


-- -----------------------------------------------------------------------------
-- onboarding.activation_decision
-- The governed approval or rejection decision for a merchant application.
-- Records who made the decision, when, and why.
-- An application may have at most one effective decision, but the schema
-- does not enforce that — the application layer governs that rule.
-- -----------------------------------------------------------------------------
CREATE TABLE onboarding.activation_decision (
    decision_id             UUID                     NOT NULL,
    application_id          UUID                     NOT NULL,
    decision_type           VARCHAR(40)              NOT NULL,
    decision_reason_summary TEXT,
    decided_by              UUID                     NOT NULL,
    decided_at              TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_activation_decision
        PRIMARY KEY (decision_id),

    CONSTRAINT fk_decision_application_id
        FOREIGN KEY (application_id)
        REFERENCES onboarding.merchant_application (application_id),

    CONSTRAINT chk_decision_type
        CHECK (decision_type IN ('APPROVED', 'REJECTED'))
);

CREATE INDEX idx_activation_decision_application_id ON onboarding.activation_decision (application_id);
CREATE INDEX idx_activation_decision_decided_by     ON onboarding.activation_decision (decided_by);


-- -----------------------------------------------------------------------------
-- onboarding.required_document
-- Tracks the fulfilment state of each document requirement for an application.
--
-- latest_document_reference_id is a pointer into the shared file/document
-- boundary. This table does not store file bytes, storage keys, signed URLs,
-- bucket names, or AV scan implementation details. Those are owned by the
-- shared storage and file infrastructure.
-- -----------------------------------------------------------------------------
CREATE TABLE onboarding.required_document (
    required_document_id          UUID                     NOT NULL,
    application_id                UUID                     NOT NULL,
    document_type                 VARCHAR(60)              NOT NULL,
    requirement_status            VARCHAR(40)              NOT NULL,
    latest_document_reference_id  UUID,                              -- null until a document is received
    rejection_reason_summary      TEXT,
    received_at                   TIMESTAMP WITH TIME ZONE,
    created_at                    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at                    TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_required_document
        PRIMARY KEY (required_document_id),

    CONSTRAINT fk_required_document_application_id
        FOREIGN KEY (application_id)
        REFERENCES onboarding.merchant_application (application_id),

    CONSTRAINT chk_document_type
        CHECK (document_type IN (
            'BUSINESS_LICENSE', 'OWNER_ID', 'BANK_LETTER',
            'TAX_DOCUMENT', 'SIGNED_APPLICATION', 'PROCESSING_STATEMENT'
        )),

    CONSTRAINT chk_requirement_status
        CHECK (requirement_status IN (
            'PENDING', 'RECEIVED', 'SCAN_PENDING', 'ACCEPTED', 'REJECTED', 'SCAN_BLOCKED'
        ))
);

CREATE INDEX idx_required_document_application_id ON onboarding.required_document (application_id);


-- -----------------------------------------------------------------------------
-- onboarding.application_timeline
-- Ordered history of workflow events for a merchant application.
-- This is Onboarding's own timeline — it is not the global audit log or outbox.
-- It captures what happened to this application and when, for review and support.
-- -----------------------------------------------------------------------------
CREATE TABLE onboarding.application_timeline (
    timeline_id     UUID                     NOT NULL,
    application_id  UUID                     NOT NULL,
    timeline_type   VARCHAR(80)              NOT NULL,
    summary         TEXT                     NOT NULL,
    actor_id        UUID,                              -- null for system-generated events
    occurred_at     TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_application_timeline
        PRIMARY KEY (timeline_id),

    CONSTRAINT fk_timeline_application_id
        FOREIGN KEY (application_id)
        REFERENCES onboarding.merchant_application (application_id)
);

CREATE INDEX idx_application_timeline_application_id ON onboarding.application_timeline (application_id);
CREATE INDEX idx_application_timeline_occurred_at    ON onboarding.application_timeline (occurred_at);
