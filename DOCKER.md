# CargoSphere Docker Package

This package adds Docker support for all eight backend modules:

- service-registry
- api-gateway
- auth-service
- shipment-service
- container-service
- document-service
- payment-service
- audit-service

## Apply the package

Extract this ZIP into the root of:

```text
C:\Users\dnyan\OneDrive\Documents\cargosphere-backend
```

Allow files to merge into the existing `services` directories. The package
adds `Dockerfile` and `.dockerignore` files; it does not overwrite Java source
code or application configuration.

## Prepare environment variables

From the repository root:

```powershell
Copy-Item .\.env.example .\.env
notepad .\.env
```

Replace all placeholders with real Neon credentials, JWT secret and internal
audit API key. Never commit `.env`.

Append the contents of `gitignore-docker-snippet.txt` to the root `.gitignore`.

## Validate Compose

```powershell
docker compose config
```

This must complete without errors.

## Build and start

```powershell
docker compose up --build -d
```

Or:

```powershell
.\scripts\docker-start.ps1
```

## Check status

```powershell
docker compose ps
```

## Follow logs

```powershell
docker compose logs -f --tail=200
```

## URLs

- API Gateway: http://localhost:8080
- Central Swagger UI: http://localhost:8080/swagger-ui.html
- Eureka dashboard: http://localhost:8761
- Auth service: http://localhost:8081
- Shipment service: http://localhost:8082
- Container service: http://localhost:8083
- Document service: http://localhost:8084
- Payment service: http://localhost:8085
- Audit service: http://localhost:8086

## Stop

```powershell
docker compose down
```

To also remove locally built images:

```powershell
docker compose down --rmi local
```

## Important design note

The Compose file uses your existing Neon PostgreSQL databases. It does not
start a local PostgreSQL container. Each service receives its existing database
environment variables from `.env`.
