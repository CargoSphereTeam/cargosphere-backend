ALTER TABLE payment_schema.shipment_payment_summaries
    DROP CONSTRAINT chk_summary_confirmation_status;

ALTER TABLE payment_schema.shipment_payment_summaries
    ADD CONSTRAINT chk_summary_confirmation_status
        CHECK (confirmation_status IN ('DRAFT', 'APPROVED', 'CONFIRMED'));
