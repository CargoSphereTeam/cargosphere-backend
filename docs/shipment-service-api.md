# CargoSphere Shipment Service API Documentation

## Service Details

| Item | Value |
|---|---|
| Service Name | shipment-service |
| Port | 8082 |
| Base URL | http://localhost:8082 |
| Database Schema | shipment_schema |
| Database Managed By | Flyway |

---

## Health APIs

### 1. Shipment Service Health

**GET**

```http
/api/shipments/health
```

**Success Response**

```json
{
  "service": "shipment-service",
  "status": "UP"
}
```

---

### 2. Actuator Health

**GET**

```http
/actuator/health
```

**Success Response**

```json
{
  "status": "UP"
}
```

---

## Shipment APIs

### 3. Create Shipment

**POST**

```http
/api/shipments
```

**Request Body**

```json
{
  "clientUserId": 1,
  "originLocation": "Mumbai",
  "destinationLocation": "Pune",
  "shipmentType": "ROAD",
  "expectedPickupDate": "2026-08-01",
  "expectedDeliveryDate": "2026-08-05"
}
```

**Success Status**

```http
201 Created
```

**Success Response**

```json
{
  "id": 1,
  "shipmentNumber": "CS-20260719-ABC12345",
  "clientUserId": 1,
  "originLocation": "Mumbai",
  "destinationLocation": "Pune",
  "shipmentType": "ROAD",
  "status": "CREATED",
  "expectedPickupDate": "2026-08-01",
  "expectedDeliveryDate": "2026-08-05",
  "createdAt": "2026-07-19T19:00:00",
  "updatedAt": "2026-07-19T19:00:00"
}
```

---

### 4. Get All Shipments

**GET**

```http
/api/shipments
```

**Success Status**

```http
200 OK
```

**Success Response**

```json
[
  {
    "id": 1,
    "shipmentNumber": "CS-20260719-ABC12345",
    "clientUserId": 1,
    "originLocation": "Mumbai",
    "destinationLocation": "Pune",
    "shipmentType": "ROAD",
    "status": "CREATED",
    "expectedPickupDate": "2026-08-01",
    "expectedDeliveryDate": "2026-08-05",
    "createdAt": "2026-07-19T19:00:00",
    "updatedAt": "2026-07-19T19:00:00"
  }
]
```

---

### 5. Get Shipment By ID

**GET**

```http
/api/shipments/{shipmentId}
```

**Example**

```http
/api/shipments/1
```

**Success Status**

```http
200 OK
```

**Success Response**

```json
{
  "id": 1,
  "shipmentNumber": "CS-20260719-ABC12345",
  "clientUserId": 1,
  "originLocation": "Mumbai",
  "destinationLocation": "Pune",
  "shipmentType": "ROAD",
  "status": "CREATED",
  "expectedPickupDate": "2026-08-01",
  "expectedDeliveryDate": "2026-08-05",
  "createdAt": "2026-07-19T19:00:00",
  "updatedAt": "2026-07-19T19:00:00"
}
```

---

### 6. Get Shipment By Shipment Number

**GET**

```http
/api/shipments/number/{shipmentNumber}
```

**Example**

```http
/api/shipments/number/CS-20260719-ABC12345
```

**Success Status**

```http
200 OK
```

---

### 7. Get Shipments By Client User ID

**GET**

```http
/api/shipments/client/{clientUserId}
```

**Example**

```http
/api/shipments/client/1
```

**Success Status**

```http
200 OK
```

---

## Cargo Detail APIs

### 8. Add Cargo Detail

**POST**

```http
/api/shipments/{shipmentId}/cargo-details
```

**Example**

```http
/api/shipments/1/cargo-details
```

**Request Body**

```json
{
  "cargoName": "Electronics Box",
  "cargoDescription": "Laptop accessories",
  "cargoType": "ELECTRONICS",
  "weightKg": 25.50,
  "volumeCbm": 1.20,
  "quantity": 2,
  "fragile": true,
  "hazardous": false
}
```

**Success Status**

```http
201 Created
```

**Success Response**

```json
{
  "id": 1,
  "shipmentId": 1,
  "cargoName": "Electronics Box",
  "cargoDescription": "Laptop accessories",
  "cargoType": "ELECTRONICS",
  "weightKg": 25.50,
  "volumeCbm": 1.20,
  "quantity": 2,
  "fragile": true,
  "hazardous": false,
  "createdAt": "2026-07-19T19:05:00",
  "updatedAt": "2026-07-19T19:05:00"
}
```

---

### 9. Get Cargo Details By Shipment ID

**GET**

```http
/api/shipments/{shipmentId}/cargo-details
```

**Example**

```http
/api/shipments/1/cargo-details
```

**Success Status**

```http
200 OK
```

**Success Response**

```json
[
  {
    "id": 1,
    "shipmentId": 1,
    "cargoName": "Electronics Box",
    "cargoDescription": "Laptop accessories",
    "cargoType": "ELECTRONICS",
    "weightKg": 25.50,
    "volumeCbm": 1.20,
    "quantity": 2,
    "fragile": true,
    "hazardous": false,
    "createdAt": "2026-07-19T19:05:00",
    "updatedAt": "2026-07-19T19:05:00"
  }
]
```

---

## Shipment Status API

### 10. Update Shipment Status

**PATCH**

```http
/api/shipments/{shipmentId}/status
```

**Example**

```http
/api/shipments/1/status
```

**Request Body**

```json
{
  "status": "IN_TRANSIT"
}
```

**Success Status**

```http
200 OK
```

**Success Response**

```json
{
  "id": 1,
  "shipmentNumber": "CS-20260719-ABC12345",
  "clientUserId": 1,
  "originLocation": "Mumbai",
  "destinationLocation": "Pune",
  "shipmentType": "ROAD",
  "status": "IN_TRANSIT",
  "expectedPickupDate": "2026-08-01",
  "expectedDeliveryDate": "2026-08-05",
  "createdAt": "2026-07-19T19:00:00",
  "updatedAt": "2026-07-19T19:10:00"
}
```

---

## Shipment Event APIs

### 11. Get Shipment Events

**GET**

```http
/api/shipments/{shipmentId}/events
```

**Example**

```http
/api/shipments/1/events
```

**Success Status**

```http
200 OK
```

**Success Response**

```json
[
  {
    "id": 3,
    "shipmentId": 1,
    "eventType": "IN_TRANSIT",
    "eventDescription": "Shipment status updated to IN_TRANSIT",
    "eventLocation": null,
    "eventTime": "2026-07-19T19:10:00",
    "createdAt": "2026-07-19T19:10:00",
    "updatedAt": "2026-07-19T19:10:00"
  },
  {
    "id": 2,
    "shipmentId": 1,
    "eventType": "CARGO_ADDED",
    "eventDescription": "Cargo added: Electronics Box",
    "eventLocation": "Mumbai",
    "eventTime": "2026-07-19T19:05:00",
    "createdAt": "2026-07-19T19:05:00",
    "updatedAt": "2026-07-19T19:05:00"
  },
  {
    "id": 1,
    "shipmentId": 1,
    "eventType": "CREATED",
    "eventDescription": "Shipment created",
    "eventLocation": "Mumbai",
    "eventTime": "2026-07-19T19:00:00",
    "createdAt": "2026-07-19T19:00:00",
    "updatedAt": "2026-07-19T19:00:00"
  }
]
```

---

## Valid Enum Values

### Shipment Type

```text
ROAD
RAIL
SEA
AIR
```

### Shipment Status

```text
CREATED
BOOKED
IN_TRANSIT
DELIVERED
CANCELLED
```

### Cargo Type

```text
GENERAL
FRAGILE
HAZARDOUS
PERISHABLE
LIQUID
HEAVY
ELECTRONICS
OTHER
```

### Shipment Event Type

```text
CREATED
CARGO_ADDED
BOOKED
PICKED_UP
DEPARTED
IN_TRANSIT
ARRIVED
OUT_FOR_DELIVERY
DELIVERED
CANCELLED
DELAYED
CONTAINER_ALLOCATED
```

---

## Error Response Format

### Validation Error Example

**Status**

```http
400 Bad Request
```

**Response**

```json
{
  "timestamp": "2026-07-19T19:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/shipments",
  "validationErrors": {
    "clientUserId": "Client user id is required",
    "originLocation": "Origin location is required",
    "destinationLocation": "Destination location is required",
    "shipmentType": "Shipment type is required"
  }
}
```

---

### Not Found Error Example

**Status**

```http
404 Not Found
```

**Response**

```json
{
  "timestamp": "2026-07-19T19:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Shipment not found with id: 999",
  "path": "/api/shipments/999",
  "validationErrors": null
}
```

---

## Testing Notes

- Full write/read API testing should be done first on `cargosphere_test`.
- Only safe GET validation should be done on `cargosphere_dev` unless the team agrees.
- `cargosphere_test` may contain repeated test records.
- IDs may skip numbers because PostgreSQL sequences do not guarantee continuous IDs.
- Do not manually edit Flyway-managed tables.
- Do not delete `shipment_schema.flyway_schema_history`.

---

## API Testing Order

Recommended testing order:

1. Health Check
2. Create Shipment
3. Get Shipment By ID
4. Get All Shipments
5. Add Cargo Detail
6. Get Cargo Details
7. Update Shipment Status
8. Get Shipment Events
9. Validation Error Test
10. Not Found Error Test

---

## Postman Base URL

For local testing:

```text
http://localhost:8082
```

Example full endpoint:

```http
http://localhost:8082/api/shipments
```