\# CargoSphere Backend Architecture



\## Overview



CargoSphere backend is designed as a microservice-based system using Java Spring Boot.



The system will initially build and test each service independently. After the main services are stable, Eureka Service Registry and Spring Cloud Gateway will be added.



\## Architecture Flow



```text

React Frontend

&#x20;    |

&#x20;    v

Spring Cloud Gateway

&#x20;    |

&#x20;    v

Microservices

&#x20;    |

&#x20;    v

Neon PostgreSQL

