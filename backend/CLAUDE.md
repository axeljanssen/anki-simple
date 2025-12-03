# Backend - CLAUDE.md

Backend documentation for the Simple Anki vocabulary learning application.

## Quick Start

```bash
# Build the project
mvn clean install

# Run the application (port 8080)
mvn spring-boot:run

# Run tests
mvn test

# Package as JAR
mvn package
```

## Technology Stack

- **Spring Boot 3.4.1** - Application framework
- **Java 21** - Programming language
- **Spring Data JPA** - Database access
- **Spring Security** - Authentication & authorization
- **JWT (jjwt 0.11.5)** - Token-based authentication
- **H2 Database** - File-based database (development, persists in `~/.anki-simple/`)
- **Flyway** - Database migration and versioning
- **Lombok** - Reduce boilerplate code
- **Maven** - Build tool

## Project Structure

```
src/main/java/com/anki/simple/
├── config/              # Configuration classes (SecurityConfig, etc.)
├── security/            # Security components (JWT, filters, user details)
├── user/                # User domain (entity, repository, service, controller)
│   └── dto/            # User-related DTOs (AuthResponse, LoginRequest, SignupRequest, LeanUserInternal)
├── vocabulary/          # Vocabulary card domain
│   └── dto/            # Vocabulary DTOs (VocabularyCardRequest, VocabularyCardResponse, VocabularyCardLeanResponse)
├── review/              # Review system and spaced repetition
│   └── dto/            # Review DTOs (ReviewRequest, ReviewResponse)
└── tag/                 # Tag system for organizing cards
    └── dto/            # Tag DTOs

src/main/resources/
├── application.properties  # Application configuration
├── db/
│   └── migration/         # Flyway database migration scripts
│       └── V1__initial_schema.sql  # Initial database schema
└── data.sql               # Initial data (if needed)
```

## Architecture

### Layered Architecture

Each domain module follows a consistent pattern:

1. **Entity Layer** - JPA entities with database mappings
2. **Repository Layer** - Spring Data JPA repositories
3. **Service Layer** - Business logic and transaction management
4. **Controller Layer** - REST API endpoints
5. **DTO Layer** - Data transfer objects for API requests/responses
6. **Mapper Layer** - MapStruct mappers for DTO/entity conversion

### MapStruct Integration

MapStruct is used throughout the application for type-safe and efficient DTO-to-entity conversions, eliminating boilerplate mapping code.

#### Configuration

**Maven Dependencies** (pom.xml):
```xml
<properties>
    <mapstruct.version>1.5.5.Final</mapstruct.version>
    <lombok-mapstruct-binding.version>0.2.0</lombok-mapstruct-binding.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>${mapstruct.version}</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>${mapstruct.version}</version>
                    </path>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>${lombok.version}</version>
                    </path>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok-mapstruct-binding</artifactId>
                        <version>${lombok-mapstruct-binding.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**Important**: The `lombok-mapstruct-binding` dependency ensures MapStruct and Lombok work together correctly. Always list MapStruct processor before Lombok in annotation processor paths.

#### Mapper Creation Pattern

All mappers follow this pattern:

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VocabularyCardMapper {

  // Entity to DTO
  VocabularyCardResponse toResponse(VocabularyCard card);

  // DTO to Entity with ignored fields
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  VocabularyCard toEntity(VocabularyCardRequest request);
}
```

**Key Annotations**:
- `@Mapper(componentModel = "SPRING")` - Registers mapper as Spring bean for dependency injection
- `@Mapping(target = "...", ignore = true)` - Excludes fields from mapping (e.g., auto-generated IDs, relationships)
- Use `default` methods for custom mapping logic

#### Mapper Locations

Mappers are organized by domain in `mapper` packages:

- `user/mapper/UserMapper.java` - User and authentication DTOs
- `vocabulary/mapper/VocabularyCardMapper.java` - Vocabulary card DTOs
- `tag/mapper/TagMapper.java` - Tag DTOs
- `review/mapper/ReviewHistoryMapper.java` - Review history creation

#### Service Integration

Mappers are injected into services via constructor injection (using Lombok `@RequiredArgsConstructor`):

```java
@Service
@RequiredArgsConstructor
public class VocabularyService {
    private final VocabularyRepository vocabularyRepository;
    private final VocabularyCardMapper vocabularyCardMapper;  // MapStruct mapper

    public VocabularyCardResponse createCard(VocabularyCardRequest request, String username) {
        VocabularyCard card = vocabularyCardMapper.toEntity(request);
        // Set unmapped fields
        card.setUser(user);

        VocabularyCard savedCard = vocabularyRepository.save(card);
        return vocabularyCardMapper.toResponse(savedCard);
    }
}
```

#### Benefits

1. **Type Safety** - Compile-time checks prevent mapping errors
2. **Performance** - Generates plain Java code, no reflection
3. **Maintainability** - Centralized mapping logic, easy to update
4. **Consistency** - Ensures uniform mapping patterns across the application
5. **Less Boilerplate** - No manual field-by-field copying

#### Best Practices

- Always use `@Mapping(target = "id", ignore = true)` for entity creation to avoid ID conflicts
- Ignore collection fields (e.g., `vocabularyCards`, `tags`) when mapping from DTOs
- Use `default` methods for complex or custom mapping logic
- Let services set relationship fields (e.g., `setUser()`) after mapping
- Run `mvn clean compile` after mapper changes to regenerate implementation classes



### Key Components

#### Security (security/ and config/)

- **JwtUtil** (security/JwtUtil.java) - JWT token generation and validation
  - Generates tokens with 24-hour expiration
  - Extracts username and validates token signature

- **JwtAuthenticationFilter** (security/JwtAuthenticationFilter.java) - Request filter
  - Intercepts requests and validates JWT tokens
  - Sets Spring Security authentication context

- **CustomUserDetailsService** (security/CustomUserDetailsService.java)
  - Loads user details for Spring Security

- **SecurityConfig** (config/SecurityConfig.java) - Security configuration
  - Configures JWT authentication
  - Sets up CORS for frontend (http://localhost:5173)
  - Public endpoints: `/api/auth/**`, `/h2-console/**`
  - All other endpoints require authentication

#### User Domain (user/)

- **User** - Entity with username, email, password (BCrypt hashed)
- **LeanUserInternal** - Internal lightweight user DTO
  - Contains only: `id`, `username`
  - Used for internal service-to-service communication
  - Avoids exposing sensitive user data (email, password hash)
  - Reduces memory footprint when user details needed for associations
- **UserService** - User registration and management
- **AuthController** - Endpoints: `/api/auth/signup`, `/api/auth/login`

#### Vocabulary Domain (vocabulary/)

- **VocabularyCard** - Core entity with:
  - Card content: front, back, exampleSentence, audioUrl
  - Language: languageSelection (LanguageSelection enum for bidirectional pairs)
  - SM-2 fields: easeFactor, intervalDays, repetitions, lastReviewed, nextReview
  - Relationships: ManyToOne with User, ManyToMany with Tag, OneToMany with ReviewHistory

- **LanguageSelection** - Enum for bidirectional language pairs
  - Replaces separate sourceLanguage/targetLanguage fields
  - Supports 10 language pairs: DE_FR, DE_ES, EN_ES, EN_FR, EN_DE, FR_ES, EN_IT, DE_IT, FR_IT, ES_IT
  - Each enum has displayName (e.g., "German ⇄ French")
  - Stored as STRING in database (`@Enumerated(EnumType.STRING)` in `language_selection` column)
  - Simplifies UI and ensures valid language combinations
  - Example usage:
    ```java
    @Enumerated(EnumType.STRING)
    @Column(name = "language_selection", length = 20)
    private LanguageSelection languageSelection;
    ```

- **VocabularyCardLeanResponse** - Lightweight DTO for list views
  - Contains only: `id`, `front`, `back`, `languageSelection`
  - Used by `GET /api/vocabulary` endpoint for performance optimization
  - Reduces payload size by excluding: tags, SM-2 data (easeFactor, intervalDays, etc.), timestamps, exampleSentence
  - Significantly improves list loading performance, especially with large card collections
  - Full card details fetched individually when editing

- **VocabularyService** - CRUD operations for cards
- **VocabularyController** - Endpoints:
  - `GET /api/vocabulary` - Get all user's cards (returns `VocabularyCardLeanResponse[]` for performance)
  - `POST /api/vocabulary` - Create new card
  - `PUT /api/vocabulary/{id}` - Update card
  - `DELETE /api/vocabulary/{id}` - Delete card
  - `GET /api/vocabulary/due` - Get cards due for review
  - `GET /api/vocabulary/due/count` - Count of due cards

#### Review Domain (review/)

- **SpacedRepetitionService** (review/SpacedRepetitionService.java) - SM-2 algorithm implementation
  - Quality ratings: 0-5 (0=complete blackout, 5=perfect recall)
  - Quality < 3: Resets card to 1-day interval, resets repetitions to 0
  - Quality >= 3: Advances interval based on ease factor
    - First review: 1 day
    - Second review: 6 days
    - Subsequent: interval * ease_factor
  - Ease factor adjusts based on quality: `easeFactor = easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))`
  - Minimum ease factor: 1.3

- **ReviewHistory** - Tracks each review session
- **ReviewService** - Processes reviews and updates card schedules
- **ReviewController** - Endpoint: `POST /api/review` (submits review with quality rating)

#### Tag Domain (tag/)

- **Tag** - Entity for organizing cards with categories
- **TagService** - Tag management
- **TagController** - Tag CRUD endpoints

## Code Conventions
### Java Style
- Google Java Style Guide for indentation (2 spaces)
- Lombok to reduce boilerplate (@Data, @Builder, @RequiredArgsConstructor)
- Avoid @AllArgsConstructor unless strictly necessary
- Prefer Optional<T> to null returns
- Stream API for collections when it improves readability
### Naming Conventions
- Package: com.company.domain.subdomain
- Classes: PascalCase (UserProfile, OrderService)
- Methods/Variables: camelCase
- Constants: UPPER_SNAKE_CASE
- Database fields: snake_case but mapped to camelCase in entities
### Error Handling
- Custom exceptions for domain logic (e.g.: InsufficientStockException)
- GlobalExceptionHandler for REST endpoints
- Problem Details RFC 7807 for error responses
## Testing Strategy
- Unit tests: Given-When-Then pattern with AssertJ
- Integration tests: @SpringBootTest with Testcontainers
- Integration tests: avoid mocks and use m2 in-memory database for testing whenever possible.
- Mock externals with WireMock
- Minimum coverage: 80% for business logic

## Code Quality with SonarCloud

The project uses SonarCloud for continuous code quality and security analysis.

### Configuration

**sonar-project.properties** (root):
- Multi-module project setup (backend + frontend)
- Java sources: `backend/src/main/java`
- Test sources: `backend/src/test/java`
- JaCoCo coverage reports: `backend/target/site/jacoco/jacoco.xml`
- Project key: `axeljanssen_anki-simple`
- Organization: `axeljanssen`

### CI/CD Integration

**GitHub Workflow** (`.github/workflows/sonarqube.yml`):
- Triggers on: push to main/develop, pull requests
- Steps:
  1. Build backend with `mvn clean verify` (includes JaCoCo coverage)
  2. Run backend tests with coverage
  3. Install frontend dependencies
  4. Run frontend tests with coverage
  5. Upload results to SonarCloud

### Coverage Reports

- **Backend**: JaCoCo XML reports generated during `mvn verify`
- **Frontend**: LCOV reports from Vitest coverage
- **Current Coverage**: 81% backend test coverage achieved

### Running SonarQube Locally

```bash
# Requires SONAR_TOKEN environment variable
cd backend
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=axeljanssen_anki-simple \
  -Dsonar.organization=axeljanssen \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.token=$SONAR_TOKEN
```

### Best Practices

- Check SonarCloud dashboard before merging PRs
- Address critical and high-severity issues
- Maintain minimum 80% test coverage for business logic
- Review security hotspots and vulnerabilities

## Database Configuration

### H2 File-Based Database

Configuration in `application.properties`:
```properties
spring.datasource.url=jdbc:h2:file:~/.anki-simple/ankidb;AUTO_SERVER=TRUE
spring.datasource.username=sa
spring.datasource.password=
```

**Features**:
- Data persists between application restarts in `~/.anki-simple/` directory
- AUTO_SERVER mode allows H2 console access while app is running
- To reset database: Stop app, delete `~/.anki-simple/ankidb.mv.db`, restart app

**Test Configuration**:
Tests use in-memory H2 for speed via `application-test.properties`:
```properties
spring.datasource.url=jdbc:h2:mem:testdb
```
Integration tests use `@ActiveProfiles("test")` annotation to load test config.

### H2 Console

Access at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:~/.anki-simple/ankidb;AUTO_SERVER=TRUE`
- Username: `sa`
- Password: (empty)

### JPA Configuration

- `spring.jpa.hibernate.ddl-auto=validate` - Validates schema against entities (Flyway manages schema)
- `spring.jpa.show-sql=true` - Logs SQL statements to console

### Flyway Database Migrations

Flyway manages database schema versioning and migrations automatically.

**Configuration in `application.properties`:**
```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

**Migration Files:**
- Located in: `src/main/resources/db/migration/`
- Naming convention: `V{version}__{description}.sql`
- Example: `V1__initial_schema.sql`, `V2__add_user_preferences.sql`

**How it works:**
1. Flyway tracks applied migrations in `flyway_schema_history` table
2. On startup, Flyway checks for new migration files
3. New migrations are applied in order automatically
4. Schema changes are version-controlled with your code

**Creating a new migration:**
```bash
# Create new migration file
touch src/main/resources/db/migration/V2__add_new_feature.sql

# Add your SQL DDL statements
# ALTER TABLE users ADD COLUMN last_login TIMESTAMP;
```

**Important:**
- Never modify existing migration files after they've been applied
- Always create new migrations for schema changes
- Use descriptive names for migrations
- Test migrations on development database first

### Main Tables

- `users` - User accounts
- `vocabulary_cards` - Vocabulary cards with SM-2 data
- `tags` - Tags for organizing cards
- `review_history` - Historical review data
- `card_tags` - Join table for card-tag relationship

## API Authentication

### Public Endpoints

- `POST /api/auth/signup` - Register new user
- `POST /api/auth/login` - Login and receive JWT token
- `/h2-console/**` - H2 database console (development only)

### Protected Endpoints

All other endpoints require JWT authentication:

```
Authorization: Bearer <jwt-token>
```

### Authentication Flow

1. User registers via `/api/auth/signup` or logs in via `/api/auth/login`
2. Backend validates credentials and returns JWT token in response
3. Client includes token in `Authorization` header for subsequent requests
4. `JwtAuthenticationFilter` validates token on each request
5. If valid, user identity is set in Spring Security context
6. Token expires after 24 hours (86400000ms)

## Development Tips

### Running the Application

```bash
# Run with Maven
mvn spring-boot:run

# Run packaged JAR
java -jar target/simple-anki-0.0.1-SNAPSHOT.jar
```

### Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=VocabularyServiceTest

# Run with coverage
mvn test jacoco:report
```

### Common Development Tasks

**Add a new entity:**
1. Create entity class in appropriate domain package
2. Add JPA annotations (@Entity, @Table, @Id, etc.)
3. Create repository interface extending JpaRepository
4. Create Flyway migration script for the new table:
   ```bash
   # Create migration file (increment version number)
   touch src/main/resources/db/migration/V2__add_new_entity.sql
   ```
5. Add SQL DDL in migration file:
   ```sql
   CREATE TABLE new_entity (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       name VARCHAR(255) NOT NULL
   );
   ```
6. Restart application - Flyway will apply the migration

**Add new API endpoint:**
1. Create/update DTO classes in dto/ subdirectory
2. Add method to service class with business logic
3. Add endpoint to controller with proper mapping
4. Add security configuration if endpoint should be public

**Modify database schema:**
1. Never modify existing migration files
2. Create new migration with incremented version:
   ```bash
   touch src/main/resources/db/migration/V3__modify_table.sql
   ```
3. Add ALTER TABLE statements in the migration file
4. Update entity class to match new schema
5. Restart application to apply migration

**Debug database issues:**
1. Check SQL logs in console (spring.jpa.show-sql=true)
2. Use H2 console to inspect data: http://localhost:8080/h2-console
3. Check Flyway migration history: `SELECT * FROM flyway_schema_history`
4. Check entity relationships and cascade settings

**Flyway troubleshooting:**
1. View migration status: Check `flyway_schema_history` table in H2 console
2. Failed migration: Fix the SQL, delete failed entry from history table, restart
3. Schema mismatch: Ensure entity annotations match migration scripts
4. Database persists in `~/.anki-simple/ankidb.mv.db` - delete file to reset

### Security Considerations

**Development:**
- H2 console is enabled for debugging
- CORS allows localhost:5173
- JWT secret is a placeholder

**Production:**
- Change JWT secret in application.properties:20
- Disable H2 console
- Use persistent database (PostgreSQL, MySQL)
- Update CORS configuration for production domain
- Enable HTTPS/TLS
- Consider token refresh mechanism
- Implement rate limiting

## Troubleshooting

**Port 8080 already in use:**
```bash
# Find process using port 8080
lsof -i :8080
# Kill the process
kill -9 <PID>
```

**Maven build fails:**
```bash
# Clean and rebuild
mvn clean install -U
```

**Tests fail:**
```bash
# Run with full stack traces
mvn test -X
```

**Database issues:**
- Database persists in `~/.anki-simple/ankidb.mv.db`
- To reset database: Stop backend, delete database file, restart
- Use H2 console to inspect data while app is running (AUTO_SERVER mode)
- Check entity annotations and relationships
- Verify application.properties database settings
- Tests use in-memory H2 (see `application-test.properties`)

## IDE Setup

### IntelliJ IDEA

1. Import as Maven project
2. Enable annotation processing for Lombok:
   - Settings > Build > Compiler > Annotation Processors
   - Check "Enable annotation processing"
3. Install Lombok plugin if not already installed
4. Set Java SDK to 21

### VS Code

1. Install extensions:
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Lombok Annotations Support
2. Java SDK 21 should be configured in settings

## Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/3.4.1/reference/html/)
- [Spring Security](https://docs.spring.io/spring-security/reference/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Flyway Documentation](https://documentation.red-gate.com/flyway) - Database migration tool
- [JWT.io](https://jwt.io/) - JWT debugger
- [SM-2 Algorithm](https://www.supermemo.com/en/archives1990-2015/english/ol/sm2) - Original SuperMemo algorithm
