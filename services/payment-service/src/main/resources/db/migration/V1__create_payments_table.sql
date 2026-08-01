CREATE SCHEMA IF NOT EXISTS payment_schema;

CREATE TABLE payment_schema.payments
(
    id                    BIGSERIAL PRIMARY KEY,

    shipment_id           BIGINT         NOT NULL,
    user_id               BIGINT         NOT NULL,

    amount                NUMERIC(15, 2) NOT NULL,
    currency              VARCHAR(3)     NOT NULL DEFAULT 'INR',

    payment_method        VARCHAR(30)    NOT NULL,
    payment_status        VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    payment_type          VARCHAR(20)    NOT NULL,

    transaction_reference VARCHAR(100),
    due_date              DATE,
    paid_date             DATE,
    remarks               VARCHAR(500),

    created_at            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_payment_amount_positive
        CHECK (amount > 0),

    CONSTRAINT chk_payment_method
        CHECK (
            payment_method IN (
                'UPI',
                'BANK_TRANSFER',
                'CREDIT_CARD',
                'DEBIT_CARD',
                'CASH',
                'OTHER'
            )
        ),

    CONSTRAINT chk_payment_status
        CHECK (
            payment_status IN (
                'PENDING',
                'PAID',
                'FAILED',
                'REFUNDED',
                'CANCELLED'
            )
        ),

    CONSTRAINT chk_payment_type
        CHECK (
            payment_type IN (
                'FULL',
                'PARTIAL',
                'ADVANCE',
                'BALANCE'
            )
        ),

    CONSTRAINT uk_payment_transaction_reference
        UNIQUE (transaction_reference)
);

CREATE INDEX idx_payments_shipment_id
    ON payment_schema.payments (shipment_id);

CREATE INDEX idx_payments_user_id
    ON payment_schema.payments (user_id);

CREATE INDEX idx_payments_status
    ON payment_schema.payments (payment_status);

CREATE INDEX idx_payments_created_at
    ON payment_schema.payments (created_at);