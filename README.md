# ms-banking-deposit (MVP)

Backend microservices for banking deposit (MVP), local via Docker Compose.

## Stack
- Java 17, Spring Boot 3, Gradle 8.12
- Postgres, Kafka/Zookeeper, Kafka Connect (Debezium), Kong
- Liquibase, JPA/Hibernate, Bean Validation, Actuator

## Quick start
1. Build product-service image (others TBD):
   - `docker compose build product-service`
2. Start infra + product-service:
   - `docker compose up -d`
3. Register Debezium connector (after Connect is healthy):
   - `curl -X POST http://localhost:8083/connectors -H 'Content-Type: application/json' \
      -d @infra/debezium/connectors/product-outbox.json`
4. Call API via Kong:
   - `POST http://localhost:8000/api/products`

## Services (MVP in repo)
- product-service: REST, Liquibase, Outbox, CQRS
- account-service: read model, Kafka consumer
- calculation-service: rate calculation
- notify-service: Kafka consumer + email mock
- mock-api-service: external API mock

## Docs
- Architecture: `docs/architecture.md`
- BA document: `docs/ba.md`
- Postman guide: `docs/postman.md`
- Postman collection: `postman/ms-banking-deposit.postman_collection.json`

## Running Tests
If network access to `services.gradle.org` is blocked, create Gradle wrapper files manually:

```bash
# Remove existing broken wrapper
rm -f gradlew gradlew.bat gradle/wrapper/gradle-wrapper.jar

# Use containerized Gradle to regenerate wrapper
docker run --rm -v "$PWD":"$PWD" -w "$PWD" gradle:8.12.0-jdk17 \
  gradle wrapper --gradle-version 8.12.0 --no-daemon

# Run tests
chmod +x ./gradlew
./gradlew test
```

Integration tests require Docker daemon (will skip automatically if unavailable).

## Profiles
- `docker`: service points to `postgres`, `kafka` containers.

## Notes
- Postgres init creates DB `product_db` user `product/product`.
- Kong runs in DB-less (declarative) mode with `kong/kong.yml`.
