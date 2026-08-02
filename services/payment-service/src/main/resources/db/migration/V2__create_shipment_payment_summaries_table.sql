CREATE TABLE payment_schema.shipment_payment_summaries
(
    id BIGSERIAL PRIMARY KEY,

    shipment_id BIGINT NOT NULL,

    estimated_amount NUMERIC(15,2) NOT NULL,

    base_amount NUMERIC(15,2) NOT NULL,

    charges NUMERIC(15,2) NOT NULL,

    taxes NUMERIC(15,2) NOT NULL,

    discount NUMERIC(15,2) NOT NULL,

    final_amount NUMERIC(15,2) NOT NULL,

    paid_amount NUMERIC(15,2) NOT NULL,

    balance_amount NUMERIC(15,2) NOT NULL,

    currency VARCHAR(3) NOT NULL DEFAULT 'INR',

    payment_method VARCHAR(30) NOT NULL,

    confirmation_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    confirmed_by BIGINT,

    confirmed_at TIMESTAMP,

    remarks VARCHAR(500),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_summary_estimated_amount
        CHECK (estimated_amount >= 0),

    CONSTRAINT chk_summary_base_amount
        CHECK (base_amount >= 0),

    CONSTRAINT chk_summary_charges
        CHECK (charges >= 0),

    CONSTRAINT chk_summary_taxes
        CHECK (taxes >= 0),

    CONSTRAINT chk_summary_discount
        CHECK (discount >= 0),

    CONSTRAINT chk_summary_final_amount
        CHECK(final_amount >= 0),

    CONSTRAINT chk_summary_paid_amount
        CHECK (paid_amount >= 0),

    CONSTRAINT chk_summary_paid_not_exceed_final
        CHECK (paid_amount <= final_amount),

    CONSTRAINT chk_summary_balance_amount
        CHECK (balance_amount >= 0),

    CONSTRAINT chk_summary_payment_method
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

    CONSTRAINT chk_summary_confirmation_status
        CHECK (
            confirmation_status IN (
                                    'DRAFT',
                                    'CONFIRMED'
                )
            ),

    CONSTRAINT uk_shipment_payment_summary
        UNIQUE (shipment_id)
);



CREATE INDEX idx_payment_summary_confirmation_status
    ON payment_schema.shipment_payment_summaries (confirmation_status);

CREATE INDEX idx_payment_summary_created_at
    ON payment_schema.shipment_payment_summaries (created_at);