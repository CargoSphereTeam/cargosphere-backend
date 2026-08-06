UPDATE shipment_schema.shipments
SET status = 'BOOKED',
    updated_at = CURRENT_TIMESTAMP
WHERE status = 'CREATED'
  AND processing_stage <> 'PENDING_ADMIN_REVIEW';
