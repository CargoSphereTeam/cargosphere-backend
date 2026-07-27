# CargoSphere Master Postman Collection

This folder contains the master API collection for the CargoSphere backend.

## Files

- `CargoSphere-Backend.postman_collection.json`
- `CargoSphere-Local.postman_environment.json`

## API base URL

All business requests use the API Gateway:

```text
http://localhost:8080
```

The container-service health request uses `http://localhost:8083` directly because the current gateway routes expose `/api/container-types/**` and `/api/container-allocations/**`, while the health controller uses `/api/containers/health`.

## Import into Postman

1. Open Postman.
2. Select **Import**.
3. Import both JSON files.
4. Select the **CargoSphere Local** environment.

## Required environment setup

Set these two values before running the collection:

```text
adminEmail
adminPassword
```

They must belong to an existing `ROLE_ADMIN` account.

Do not commit exported environments containing real passwords or JWTs. The provided environment contains no real credentials or tokens.

## Recommended service startup order

1. service-registry
2. audit-service
3. auth-service
4. shipment-service
5. container-service
6. document-service
7. payment-service
8. api-gateway

## Recommended run order

Run folders from top to bottom:

1. `00 - Health Checks`
2. `01 - Authentication`
3. `02 - Shipments`
4. `03 - Container Types`
5. `04 - Container Allocations`
6. `05 - Documents`
7. `06 - Payments`
8. `07 - Audit Logs`
9. `08 - Cleanup`

`Register Fresh Client` generates unique values and stores:

- client email
- pickup, delivery and due dates
- container type code
- transaction reference
- request ID

Login requests automatically store `clientToken` and `adminToken`. Create requests save generated entity IDs for later requests.

## Roles used

- `ROLE_CLIENT`: create and read owned shipments, add cargo, list container types, create documents, create and view own payments.
- `ROLE_ADMIN`: administration, status updates, verification, allocations, audit queries and cleanup.
- `ROLE_SERVICE`: internal audit ingestion only. The `/api/audits/internal` request is intentionally excluded from the master collection.

## Enum examples used

- Shipment type: `ROAD`
- Shipment status: `IN_TRANSIT`
- Cargo type: `ELECTRONICS`
- Verification status: `VERIFIED`
- Payment method: `UPI`
- Payment type: `FULL`
- Payment status: `PAID`
- Audit action: `PAYMENT_CREATED`
- Audit entity type: `PAYMENT`

## Important rerun behaviour

Run `Register Fresh Client` at the start of every new flow. It refreshes unique values so email, container code and transaction reference conflicts are avoided.

The cleanup folder deletes the generated document, allocation and container type. It does not delete users, shipments, payments or audit records because those delete endpoints are not present in the supplied controllers.
