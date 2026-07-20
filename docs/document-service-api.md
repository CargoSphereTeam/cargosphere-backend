# CargoSphere Document Service API



## Overview



The Document Service manages shipment document checklists and document verification status.



Current scope:



- Create document checklist entries

- Retrieve document entries

- Retrieve documents shipment-wise

- Update verification status

- Delete document entries

- No physical PDF/file upload is included in the current scope



## Technology



- Java 21

- Spring Boot 3.5.16

- Spring Web

- Spring Data JPA

- PostgreSQL

- Flyway

- Maven

- Lombok



## Service Configuration



| Property | Value |

|---|---|

| Service name | `document-service` |

| Port | `8084` |

| Base URL | `http://localhost:8084/api/documents` |

| Database schema | `document\_schema` |

| Flyway location | `classpath:db/migration` |



Database credentials are provided through environment variables:



```text

DOCUMENT\_DB\_URL

DOCUMENT\_DB\_USERNAME

DOCUMENT\_DB\_PASSWORD

```



Credentials must never be hardcoded or committed.



## API Endpoints



| Method | Endpoint | Description |

|---|---|---|

| GET | `/api/documents/health` | Check service health |

| POST | `/api/documents` | Create a document checklist entry |

| GET | `/api/documents` | Get all document entries |

| GET | `/api/documents/{id}` | Get a document by ID |

| GET | `/api/documents/shipment/{shipmentId}` | Get documents by shipment ID |

| PUT | `/api/documents/{id}/verification` | Update verification status |

| DELETE | `/api/documents/{id}` | Delete a document entry |



## Health Check



### Request



```http

GET /api/documents/health

```



### Success Response



Status: `200 OK`



## Create Document



### Request



```http

POST /api/documents

Content-Type: application/json

```



```json

{

&#x20; "shipmentId": 1001,

&#x20; "documentType": "COMMERCIAL\_INVOICE",

&#x20; "required": true,

&#x20; "remarks": "Required for customs clearance"

}

```



### Validation



- `shipmentId` is required and must be positive.

- `documentType` is required and cannot exceed 100 characters.

- `required` is mandatory.

- `remarks` cannot exceed 500 characters.

- The combination of `shipmentId` and `documentType` must be unique.



### Success Response



Status: `201 Created`



```json

{

&#x20; "id": 1,

&#x20; "shipmentId": 1001,

&#x20; "documentType": "COMMERCIAL\_INVOICE",

&#x20; "required": true,

&#x20; "verificationStatus": "PENDING",

&#x20; "verifiedBy": null,

&#x20; "verifiedAt": null,

&#x20; "remarks": "Required for customs clearance",

&#x20; "createdAt": "2026-07-20T18:00:00",

&#x20; "updatedAt": "2026-07-20T18:00:00"

}

```



## Get All Documents



### Request



```http

GET /api/documents

```



### Success Response



Status: `200 OK`



The response is an array of document objects.



## Get Document by ID



### Request



```http

GET /api/documents/{id}

```



Example:



```http

GET /api/documents/1

```



### Success Response



Status: `200 OK`



### Not Found Response



Status: `404 Not Found`



```json

{

&#x20; "error": "Resource Not Found",

&#x20; "message": "Document not found with ID: 1",

&#x20; "validationErrors": null

}

```



## Get Documents by Shipment ID



### Request



```http

GET /api/documents/shipment/{shipmentId}

```



Example:



```http

GET /api/documents/shipment/1001

```



### Success Response



Status: `200 OK`



The response is an array containing all document checklist entries for the shipment.



## Update Document Verification



Only `VERIFIED` and `REJECTED` are accepted for verification updates. `PENDING` cannot be submitted through this endpoint.



### Request



```http

PUT /api/documents/{id}/verification

Content-Type: application/json

```



```json

{

&#x20; "verificationStatus": "VERIFIED",

&#x20; "verifiedBy": 501,

&#x20; "remarks": "Document verified successfully"

}

```



### Rejected Example



```json

{

&#x20; "verificationStatus": "REJECTED",

&#x20; "verifiedBy": 501,

&#x20; "remarks": "Document details are incorrect"

}

```



### Success Response



Status: `200 OK`



The response contains the updated document object. `verifiedAt` is set automatically by the service.



## Delete Document



### Request



```http

DELETE /api/documents/{id}

```



Example:



```http

DELETE /api/documents/1

```



### Success Response



Status: `204 No Content`



## Verification Status Values



| Status | Description |

|---|---|

| `PENDING` | Document is awaiting verification |

| `VERIFIED` | Document has been verified |

| `REJECTED` | Document has been rejected |



## Error Responses



| Status | Meaning |

|---|---|

| `400 Bad Request` | Invalid request or validation failure |

| `404 Not Found` | Document does not exist |

| `409 Conflict` | Duplicate shipment/document-type combination |

| `500 Internal Server Error` | Unexpected server error |



## Database Migration



Flyway migration:



```text

db/migration/V1\_\_create\_documents\_table.sql

```



The migration creates:



- `document\_schema`

- `documents` table

- Unique shipment/document-type constraint

- Verification-status constraint

- Shipment and verification-status indexes

- Automatic `updated\_at` trigger



No direct foreign key is created to another microservice's database tables.



## Testing



Implemented tests:



- Mockito unit tests for `DocumentServiceImpl`

- MockMvc controller tests

- Request validation test

- Not-found handling test

- Delete endpoint test

- Invalid verification-status test



Maven verification:



```powershell

.\\mvnw.cmd test

```



Latest result:



```text

Tests run: 9

Failures: 0

Errors: 0

Skipped: 1

BUILD SUCCESS

```



The skipped Spring context test requires shared `DOCUMENT\_DB\_\*` environment variables.

