-- =============================================================================
-- Merchant Core Schema
-- Creates the merchant_core schema and all tables owned by this module.
-- Other modules must not write to these tables directly.
-- They may reference merchant_core.merchant.merchant_id as a foreign key.
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS merchant_core;


-- -----------------------------------------------------------------------------
-- merchant_core.merchant
-- The root identity record for a merchant.
-- Every other Merchant Core table links back here via merchant_id.
-- -----------------------------------------------------------------------------
CREATE TABLE merchant_core.merchant (
    merchant_id     UUID         NOT NULL,
    legal_name      VARCHAR(255) NOT NULL,
    display_name    VARCHAR(255) NOT NULL,
    merchant_status VARCHAR(50)  NOT NULL,  -- PENDING | ACTIVE | SUSPENDED | CLOSED
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL,

    CONSTRAINT pk_merchant PRIMARY KEY (merchant_id)
);

CREATE INDEX idx_merchant_legal_name      ON merchant_core.merchant (legal_name);
CREATE INDEX idx_merchant_display_name    ON merchant_core.merchant (display_name);
CREATE INDEX idx_merchant_merchant_status ON merchant_core.merchant (merchant_status);


-- -----------------------------------------------------------------------------
-- merchant_core.merchant_contact
-- A person who can be reached on behalf of a merchant.
-- A merchant may have more than one contact (e.g. owner and billing contact).
-- -----------------------------------------------------------------------------
CREATE TABLE merchant_core.merchant_contact (
    contact_id   UUID         NOT NULL,
    merchant_id  UUID         NOT NULL,
    full_name    VARCHAR(255) NOT NULL,
    email        VARCHAR(255) NOT NULL,
    phone        VARCHAR(50),               -- optional
    contact_role VARCHAR(50)  NOT NULL,     -- OWNER | ADMIN | BILLING
    active       BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_merchant_contact    PRIMARY KEY (contact_id),
    CONSTRAINT fk_contact_merchant_id FOREIGN KEY (merchant_id)
        REFERENCES merchant_core.merchant (merchant_id)
);

CREATE INDEX idx_merchant_contact_merchant_id ON merchant_core.merchant_contact (merchant_id);


-- -----------------------------------------------------------------------------
-- merchant_core.merchant_location
-- A physical or mailing address for a merchant.
-- A merchant may have more than one location (e.g. LEGAL and MAILING).
-- -----------------------------------------------------------------------------
CREATE TABLE merchant_core.merchant_location (
    location_id       UUID         NOT NULL,
    merchant_id       UUID         NOT NULL,
    location_type     VARCHAR(50)  NOT NULL,  -- LEGAL | MAILING | PHYSICAL
    address_line1     VARCHAR(255) NOT NULL,
    address_line2     VARCHAR(255),           -- optional
    city              VARCHAR(100) NOT NULL,
    state_or_province VARCHAR(100) NOT NULL,
    postal_code       VARCHAR(20)  NOT NULL,
    country_code      CHAR(2)      NOT NULL,  -- ISO 3166-1 alpha-2
    active            BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_merchant_location    PRIMARY KEY (location_id),
    CONSTRAINT fk_location_merchant_id FOREIGN KEY (merchant_id)
        REFERENCES merchant_core.merchant (merchant_id)
);

CREATE INDEX idx_merchant_location_merchant_id ON merchant_core.merchant_location (merchant_id);


-- -----------------------------------------------------------------------------
-- merchant_core.merchant_account
-- The acquiring account (MID) assigned to a merchant by a payment processor.
-- -----------------------------------------------------------------------------
CREATE TABLE merchant_core.merchant_account (
    merchant_account_id    UUID         NOT NULL,
    merchant_id            UUID         NOT NULL,
    processor_name         VARCHAR(100) NOT NULL,
    processor_merchant_ref VARCHAR(100) NOT NULL,  -- MID issued by the processor
    account_status         VARCHAR(50)  NOT NULL,  -- ACTIVE | FROZEN | CLOSED

    CONSTRAINT pk_merchant_account    PRIMARY KEY (merchant_account_id),
    CONSTRAINT fk_account_merchant_id FOREIGN KEY (merchant_id)
        REFERENCES merchant_core.merchant (merchant_id)
);

CREATE INDEX idx_merchant_account_merchant_id ON merchant_core.merchant_account (merchant_id);


-- -----------------------------------------------------------------------------
-- merchant_core.settlement_profile
-- The payout destination and funding cadence for a merchant's settlements.
-- The actual bank details are stored securely elsewhere; this table holds
-- only a human-readable summary (e.g. "Chase ****4321") and payout cadence.
-- -----------------------------------------------------------------------------
CREATE TABLE merchant_core.settlement_profile (
    settlement_profile_id UUID         NOT NULL,
    merchant_id           UUID         NOT NULL,
    funding_frequency     VARCHAR(50)  NOT NULL,   -- DAILY | WEEKLY | MONTHLY
    payout_method_summary VARCHAR(255) NOT NULL,
    active                BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_settlement_profile     PRIMARY KEY (settlement_profile_id),
    CONSTRAINT fk_settlement_merchant_id FOREIGN KEY (merchant_id)
        REFERENCES merchant_core.merchant (merchant_id)
);

CREATE INDEX idx_settlement_profile_merchant_id ON merchant_core.settlement_profile (merchant_id);
