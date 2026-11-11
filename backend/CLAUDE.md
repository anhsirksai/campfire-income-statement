# backend/CLAUDE.md

Spring Boot 3.5, Java 21 (`release`), JDBC via `NamedParameterJdbcTemplate`, H2 in memory. No JPA, no Lombok.
Base package `com.campfire.ledger`. Each package has a `package-info.java` stating its job — keep to it.

- Flow: `controller` → `service` → `repository` → `dao` → `query` (SQL constants). Never skip a layer.
- Records for models and DTOs. Enums map lowercase JSON/DB codes with `fromCode`.
- Money: `BigDecimal`, read with `rs.getBigDecimal`, compare with `compareTo`. DTO money fields use
  `@JsonFormat(shape = STRING)`.
- Working directory for `spring-boot:run` and tests is the **repo root** (see pom), so paths in
  `application.yml` (`data/ledger.json`) are root-relative.
- Constructor injection only. Log with SLF4J; INFO for request params and load counts.
- Tests: `@SpringBootTest` against the real ledger for golden numbers; plain unit tests for strategies.
- Errors: throw `InvalidRequestException` (400) or `LedgerDataException` (startup); `GlobalExceptionHandler` renders ProblemDetail.
