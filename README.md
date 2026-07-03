\# CargoSphere Backend



CargoSphere is a cargo and shipment management system built using Java Spring Boot microservices.



\## Tech Stack



\- Java 21

\- Spring Boot

\- Spring Data JPA / Hibernate

\- PostgreSQL 16

\- Neon PostgreSQL

\- Flyway

\- REST API

\- Eureka Service Discovery

\- Spring Cloud Gateway

\- JUnit 5

\- Mockito

\- Postman

\- GitHub Actions

\- Docker



\## Backend Architecture



This backend is planned as a microservice-based system using a backend monorepo structure.



\## Services



| Service | Responsibility |

|---|---|

| auth-service | Users, roles, authentication |

| shipment-service | Shipments, cargo details, shipment events |

| container-service | Container types and shipment container allocations |

| document-service | Shipment document checklist and verification status |

| payment-service | Shipment payment records |

| audit-service | Audit logs |

| service-registry | Eureka server |

| api-gateway | Central API entry point |



\## Database Plan



Neon project name:



```text

cargosphere

