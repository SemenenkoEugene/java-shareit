
# Agent Instructions

## Project Exploration

- Before making changes, inspect the existing project structure.
- Determine the actual Java, Spring Boot, and build tool versions from project files.
- Identify existing architectural conventions.
- Do not assume that the project uses the latest framework versions.

## Code Changes

- Do not modify code unless explicitly requested.
- Preserve existing behavior outside the requested task.
- Follow existing naming and architectural conventions.
- Prefer simple and maintainable solutions.
- Do not introduce new dependencies without justification.
- Do not perform unrelated refactoring.

## Safety

- Do not run destructive Git commands.
- Do not modify or delete existing database migrations.
- Do not modify configuration containing secrets.
- Do not commit or push changes unless explicitly requested.

## Testing

- Identify existing testing frameworks before writing tests.
- Add or update tests when behavior changes.
- Run relevant tests when possible.
- Never claim tests passed unless they were actually executed.

## Communication

- Communicate with the user in Russian.
- Explain significant implementation decisions.
- Distinguish confirmed facts from assumptions.
- Report modified files, executed checks, and remaining risks.

## Project-Specific Instructions

### Technology Baseline

- The project targets Java 21 and Spring Boot 3.5.6 and is built with the checked-in Gradle Wrapper (currently Gradle 8.5). Prefer the wrapper (`gradlew` or `gradlew.bat`) over a system Gradle installation.
- The main stack is Spring MVC, Jakarta Validation, Spring Data JPA/Hibernate, PostgreSQL, Lombok, Spring Boot Actuator, and Springdoc OpenAPI. The gateway uses `RestTemplate` with Apache HttpClient 5; tests use H2 where persistence is involved.
- Checkstyle, PMD, and JaCoCo are part of the existing quality toolchain. SpotBugs is commented out and must not be treated as enabled.

### Module Responsibilities

- `gateway` is the external HTTP-facing module. It owns request validation and forwarding through feature-specific clients and `BaseClient`; it does not currently own persistence or core business rules.
- `server` owns business rules, transactional operations, JPA entities and repositories, exception-to-HTTP mapping, and PostgreSQL access.
- The modules communicate over HTTP and have no direct Gradle project dependency. Preserve this boundary unless the task explicitly requires an architectural change.

### Application Structure and Contracts

- Code is organized primarily by feature (`user`, `item`, `booking`, and `request`). Within a server feature, the established flow is Controller -> Service interface -> `ServiceImpl` -> Repository.
- Controllers should remain focused on HTTP concerns and delegation. Service implementations are the usual home for business rules, while repositories extend Spring Data `JpaRepository` and may use derived queries or explicit JPQL.
- DTOs belong in feature-level `dto` packages. Keep persistence entities out of external API contracts and use the existing static Mapper pattern for Entity/DTO conversion where applicable.
- Prefer the established constructor injection style with `private final` dependencies and Lombok `@RequiredArgsConstructor`.
- Gateway and server maintain separate copies of parts of the HTTP contract. When an endpoint, DTO, enum, header, query parameter, status, or serialization shape changes, inspect and update both sides together and cover the contract with relevant tests.
- These conventions describe the current architecture; do not apply them mechanically when they would prevent a correct, scoped solution. Document and justify deliberate deviations.

### Transactions and Persistence

- Define transaction boundaries in the server service layer: use `@Transactional(readOnly = true)` for read operations and `@Transactional` for writes when transaction management is needed.
- Use Spring Data JPA for persistence and keep entity mappings consistent with PostgreSQL column names, relationships, constraints, and types.
- The database schema is currently initialized from `server/src/main/resources/schema.sql`; Liquibase and Flyway are not configured. Keep `schema.sql` and affected JPA entities consistent when a schema change is explicitly requested.
- PostgreSQL is the production/runtime database. H2 is a test substitute, so database-sensitive behavior should be checked for PostgreSQL compatibility rather than inferred solely from H2 tests.

### Testing and Coverage

- Use the existing test style appropriate to the layer: JUnit 5 and Mockito for service tests, `@WebMvcTest` with MockMvc and `@MockitoBean` for controller slices, `@DataJpaTest` with H2/TestEntityManager for repositories, `@JsonTest` with `JacksonTester` for JSON contracts, and `@SpringBootTest` for context-level checks.
- The Gradle `test` task uses JUnit Platform and the `test` Spring profile. It is finalized by JaCoCo report generation and coverage verification, with a minimum line coverage ratio of 0.80.
- Select the narrowest relevant checks first, then include the configured Checkstyle, PMD, and coverage checks when warranted by the scope of the change.

### Docker Compatibility

- Before changing Dockerfiles or Docker Compose, verify compatibility with Java 21, the Gradle artifact location, module ports and service URLs, and PostgreSQL database/user/password settings. Do not assume the current Docker configuration is aligned with the current Gradle build.
