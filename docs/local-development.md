# Local development

## Requirements

- Docker Engine with Docker Compose (Compose v2.20+)
- Node.js 24 and npm 11 for direct frontend development
- Optional JDK 21; backend commands can run through Docker when the host JDK is older (the host in this repo only has JDK 17)

## Start the complete stack

```bash
cp .env.example .env
docker compose up --build
```

If port 5432 is already in use on the host, override the host port:

```bash
POSTGRES_PORT=55432 docker compose up --build
```

The example credentials are for local development only. `.env` is ignored by Git.

After the stack is healthy, the following endpoints are available:

| Service | URL |
|---|---|
| Frontend operations shell | <http://localhost:4200/occupancy> |
| Frontend nginx health | <http://localhost:4200/healthz> |
| Backend health | <http://localhost:8080/actuator/health> |
| OpenAPI JSON | <http://localhost:8080/v3/api-docs> |
| Swagger UI | <http://localhost:8080/swagger-ui/index.html> |
| Frontend → backend proxy | <http://localhost:4200/api/yards> |

## Load deterministic demo data

After the backend has applied Flyway migrations:

```bash
docker compose exec -T db \
  psql -U "${POSTGRES_USER:-rail_yard}" -d "${POSTGRES_DB:-rail_yard}" \
  < scripts/demo-data.sql
```

The script uses fixed synthetic UUIDs and is safe to rerun against a clean local demo database. It inserts one yard, five tracks with capabilities, two trains, and one reservation.

## Backend verification without a host JDK 21

```bash
docker run --rm --network host \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v "$PWD/backend:/workspace" \
  -v rail-yard-maven-cache:/root/.m2 \
  -w /workspace \
  maven:3.9-eclipse-temurin-21 \
  ./mvnw verify
```

The Docker socket is mounted because Testcontainers creates an isolated PostgreSQL instance for the integration tests. The local Maven cache is mounted into a named volume `rail-yard-maven-cache` so subsequent runs reuse the dependency downloads.

## Frontend verification

```bash
npm --prefix frontend ci
npm --prefix frontend test -- --watch=false
npm --prefix frontend run build
npm --prefix frontend audit --omit=dev --audit-level=high
```

## Quick smoke checks against the running stack

```bash
# Health
curl -s http://127.0.0.1:8080/actuator/health
curl -s http://127.0.0.1:4200/healthz

# Create a yard, then attempt a duplicate to see the 409 envelope
curl -s -X POST http://127.0.0.1:8080/api/yards \
  -H 'Content-Type: application/json' \
  -d '{"code":"APX-1","name":"Alpha Yard","location":"Pune","timeZone":"Asia/Kolkata"}'
curl -s -X POST http://127.0.0.1:8080/api/yards \
  -H 'Content-Type: application/json' \
  -d '{"code":"APX-1","name":"Alpha Yard","location":"Pune","timeZone":"Asia/Kolkata"}'
```

## Reset local data

```bash
docker compose down --volumes
```

This removes the local PostgreSQL volume. Do not run it when the data must be retained. After editing any Flyway migration, the volume must be reset so the new checksum is accepted.
