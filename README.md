# Product Catalog

A starter README with best practices for the `product-catalog` project. The goal is to provide a complete, organized base you can fill in as the project grows.

---

## Table of contents

- [Description](#description)
- [Badges](#badges)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the application](#running-the-application)
- [Actuator (observability)](#actuator-observability)
- [Database and JPA](#database-and-jpa)
- [Running with Docker (optional)](#running-with-docker-optional)
- [Project structure](#project-structure)
- [Development and tests](#development-and-tests)
- [CI / CD](#ci--cd)
- [Contributing](#contributing)
- [License](#license)
- [Maintenance / Contacts](#maintenance--contacts)
- [Roadmap and next steps](#roadmap-and-next-steps)
- [FAQ / Troubleshooting](#faq--troubleshooting)

---

## Description

Product catalog project (skeleton). This README serves as a reference to:
- document setup and local run instructions,
- list useful endpoints (e.g. Actuator),
- guide JPA/DB configuration when you decide to enable it,
- and keep contribution best practices.

Fill the domain-specific sections (API, models, example data) as the project evolves.

## Badges

Add badges (build, coverage, docker image, license) here once CI is configured.

Examples:

- build: `![build](https://img.shields.io/badge/build-pending-lightgrey)`
- license: `![license](https://img.shields.io/badge/license-MIT-blue)`

---

## Prerequisites

- JDK 25 (recommended). Spring Boot 4 requires Java 17+.
- Maven (or use the included `./mvnw` wrapper).
- Git.
- IDE with Lombok plugin installed and annotation processing enabled (IntelliJ/Eclipse/VS Code).

Optional:
- Docker (to run containers)
- jq (to format JSON in curl examples)

---

## Installation

Clone the repository:

```bash
git clone https://github.com/tiagoolileite/product-catalog.git
cd product-catalog
```

Build the application (skip long-running tests):

```bash
./mvnw -DskipTests package
```

Or run directly with the Spring Boot plugin:

```bash
./mvnw spring-boot:run
```

---

## Configuration

The application uses `src/main/resources/application.properties` for basic configuration. Here are helpful examples to start without a database:

```properties
# Minimal example to run without a datasource (useful during initial development)
spring.application.name=product-catalog
# If you are temporarily disabling JPA/DB autoconfigure
# spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration

# Actuator: expose common endpoints
management.endpoints.web.exposure.include=health,info,metrics,env,conditions,loggers
management.endpoint.health.show-details=when-authorized
```

Note: if you disable auto-configuration via properties, remove any exclusion annotations from `@SpringBootApplication` when you enable JPA.

### Environment variables

You can use environment variables to configure the datasource and active profiles. Example (`.env` file):

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/product_catalog
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
SPRING_PROFILES_ACTIVE=local
```

Example in macOS/Linux terminal:

```bash
export DB_PASSWORD=postgres
export DB_USER=postgres
export DB_URL=jdbc:postgresql://localhost:5432/product-catalog
./mvnw spring-boot:run
```

---

## Running the application

Run in development mode (logs in terminal):

```bash
./mvnw spring-boot:run
```

Run the packaged JAR:

```bash
./mvnw -DskipTests package
java -jar target/product-catalog-0.0.1-SNAPSHOT.jar
```

---

## Actuator (observability)

Ready-to-run curl examples to test Actuator on port 8080 (replace credentials/tokens if needed):

1) List exposed endpoints (`/actuator`)

```bash
curl -i -sS --fail http://localhost:8080/actuator
```

2) Health

```bash
curl -i -sS --fail http://localhost:8080/actuator/health | jq .
# without jq: curl -sS http://localhost:8080/actuator/health | python -m json.tool
```

3) Info

```bash
curl -i -sS --fail http://localhost:8080/actuator/info | jq .
```

4) Metrics

```bash
curl -i -sS --fail http://localhost:8080/actuator/metrics | jq .
curl -i -sS --fail http://localhost:8080/actuator/metrics/jvm.memory.used | jq .
```

5) If Actuator is protected with Basic Auth

```bash
curl -i -sS --fail -u admin:secret http://localhost:8080/actuator/health | jq .
```

6) With Bearer token

```bash
curl -i -sS --fail -H "Authorization: Bearer YOUR_TOKEN_HERE" http://localhost:8080/actuator/health | jq .
```

7) Only HTTP status (useful in CI)

```bash
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/actuator/health
```

Tip: if you receive 404, the `spring-boot-starter-actuator` dependency may be missing from `pom.xml`, or the endpoint is not included in `management.endpoints.web.exposure.include`.

---

## Database and JPA

This project intends to use JPA, but you can start without a database to develop other parts of the application.

When you enable JPA/datasource, follow these steps:

1. Add the database driver to `pom.xml` (e.g. PostgreSQL):

```xml
<!-- example -->
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <scope>runtime</scope>
</dependency>
```

2. Configure `application.properties` or environment variables:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/product_catalog
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

3. Remove any auto-configuration exclusion (if applied) and enable the correct profile.

4. Create migrations (Flyway / Liquibase) and/or initial SQL scripts.

### Common error and quick fix

Observed error: "Failed to configure a DataSource: 'url' attribute is not specified and no embedded datasource could be configured." — this means Spring tried to configure a DataSource but did not find a URL or an embedded database (H2/HSQL/Derby). Solutions:

- During development without a DB: disable JPA/DS auto-configuration (via `spring.autoconfigure.exclude` or `excludeName` on `@SpringBootApplication`).
- To enable JPA: add the JDBC driver and configure `spring.datasource.url`.

---

## Running with Docker (optional)

Quick example to package and run with Docker (add a `Dockerfile` when ready):

```bash
# build
./mvnw -DskipTests package
# build image (example)
docker build -t product-catalog:local .
# run
docker run -p 8080:8080 --env-file .env product-catalog:local
```

---

## Project structure

Brief view of the organization (update as needed):

```
src/
  main/
    java/  -> source code
    resources/ -> application.properties, templates, static, sql
  test/ -> unit and integration tests
pom.xml
README.md
```

Include diagrams, UML and database schemas once the domain is defined.

---

## Development and tests

Run the test suite:

```bash
./mvnw test
```

Run a single test with Maven Surefire:

```bash
./mvnw -Dtest=TestName test
```

Best practices:
- Write unit and integration tests.
- Use contract tests or DB mocks; use Testcontainers for real Postgres integration tests when needed.

---

## CI / CD

Add a CI workflow (GitHub Actions / GitLab CI) with minimal steps:
- build
- tests
- static analysis (spotbugs/checkstyle)
- build docker image
- deploy (manual/automatic depending on policy)

---

## Contributing

1. Fork and branch from `main`/`master`.
2. Follow the commit and code-style conventions.
3. Open a PR with a description and checklist.
4. Run tests locally.

Add a `CONTRIBUTING.md` with a detailed process once the project matures.

---

## License

Add the project license (e.g. MIT, Apache-2.0). For now use a placeholder:

```
LICENSE: TBD
```

---

## Maintenance / Contacts

- Maintainers: @tiagoleite (add email or other contact if needed)

---

## Roadmap and next steps

- Integrate JPA with Postgres
- Create REST API for products (CRUD)
- Authentication/Authorization (JWT/OAuth2)
- Integration tests with Testcontainers
- CI/CD pipelines

---

## FAQ / Troubleshooting

Q: Why does the application fail to start with a DataSource error?

A: See the "Database and JPA" section — most likely `spring.datasource.*` is missing or the JDBC driver is not on the classpath. To work without a DB, disable auto-configuration or use a profile that doesn't enable JPA.

Q: How to enable Actuator only for specific profiles?

A: Use `spring.profiles` and define `management.endpoints.web.exposure.include` per profile in `application-<profile>.properties`.

---

If you want, I can:
- add `CONTRIBUTING.md` and `CODE_OF_CONDUCT.md`;
- check `pom.xml` and `application.properties` and add the Actuator dependency / enable minimal exposure;
- create example `Dockerfile` and `docker-compose.yml` with Postgres and the application for development.

Tell me which of these next steps you want me to take now.
