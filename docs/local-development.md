# Local development

## Requirements

- Docker Engine with Docker Compose
- Node.js 24 and npm 11 for direct frontend development
- Optional JDK 21; backend commands can run through Docker when the host JDK is older

## Start the complete stack

```bash
cp .env.example .env
docker compose up --build
```

The example credentials are for local development only. `.env` is ignored by Git.

## Load deterministic demo data

After the backend has applied Flyway migrations:

```bash
docker compose exec -T db \
  psql -U "${POSTGRES_USER:-rail_yard}" -d "${POSTGRES_DB:-rail_yard}" \
  < scripts/demo-data.sql
```

The script uses fixed synthetic UUIDs and is safe to rerun against a clean local demo database.

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

The Docker socket is mounted because Testcontainers creates an isolated PostgreSQL instance for integration tests.

## Frontend verification

```bash
npm --prefix frontend ci
npm --prefix frontend test -- --watch=false
npm --prefix frontend run build
```

## Reset local data

```bash
docker compose down --volumes
```

This removes the local PostgreSQL volume. Do not run it when the data must be retained.
