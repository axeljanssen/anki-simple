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

- **Spring Boot 3.2.0** - Application framework
- **Java 17** - Programming language
- **Spring Data JPA** - Database access
- **Spring Security** - Authentication & authorization
- **JWT (jjwt 0.11.5)** - Token-based authentication
- **H2 Database** - In-memory database (development)
- **Lombok** - Reduce boilerplate code
- **Maven** - Build tool

## Project Structure

```
src/main/java/com/anki/simple/
├── config/              # Configuration classes (SecurityConfig, etc.)
├── security/            # Security components (JWT, filters, user details)
├── user/                # User domain (entity, repository, service, controller)
│   └── dto/            # User-related DTOs (AuthResponse, LoginRequest, SignupRequest)
├── vocabulary/          # Vocabulary card domain
│   └── dto/            # Vocabulary DTOs (VocabularyCardRequest, VocabularyCardResponse)
├── review/              # Review system and spaced repetition
│   └── dto/            # Review DTOs (ReviewRequest, ReviewResponse)
└── tag/                 # Tag system for organizing cards
    └── dto/            # Tag DTOs

src/main/resources/
├── application.properties  # Application configuration
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
- **UserService** - User registration and management
- **AuthController** - Endpoints: `/api/auth/signup`, `/api/auth/login`

#### Vocabulary Domain (vocabulary/)

- **VocabularyCard** - Core entity with:
  - Card content: front, back, exampleSentence, audioUrl
  - Languages: sourceLanguage, targetLanguage
  - SM-2 fields: easeFactor, intervalDays, repetitions, lastReviewed, nextReview
  - Relationships: ManyToOne with User, ManyToMany with Tag, OneToMany with ReviewHistory

- **VocabularyService** - CRUD operations for cards
- **VocabularyController** - Endpoints:
  - `GET /api/vocabulary` - Get all user's cards
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
- Mock externals with WireMock
- Minimum coverage: 80% for business logic


## Database Configuration

### H2 In-Memory Database

Configuration in `application.properties`:
```properties
spring.datasource.url=jdbc:h2:mem:ankidb
spring.datasource.username=sa
spring.datasource.password=
```

**Important**: Database resets on application restart. Data is NOT persisted.

### H2 Console

Access at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:ankidb`
- Username: `sa`
- Password: (empty)

### JPA Configuration

- `spring.jpa.hibernate.ddl-auto=update` - Auto-creates tables from entities
- `spring.jpa.show-sql=true` - Logs SQL statements to console

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
4. JPA will auto-create table on next startup

**Add new API endpoint:**
1. Create/update DTO classes in dto/ subdirectory
2. Add method to service class with business logic
3. Add endpoint to controller with proper mapping
4. Add security configuration if endpoint should be public

**Debug database issues:**
1. Check SQL logs in console (spring.jpa.show-sql=true)
2. Use H2 console to inspect data: http://localhost:8080/h2-console
3. Check entity relationships and cascade settings

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
- Remember H2 is in-memory and resets on restart
- Check entity annotations and relationships
- Verify application.properties database settings
- Use H2 console to inspect actual data

## IDE Setup

### IntelliJ IDEA

1. Import as Maven project
2. Enable annotation processing for Lombok:
   - Settings > Build > Compiler > Annotation Processors
   - Check "Enable annotation processing"
3. Install Lombok plugin if not already installed
4. Set Java SDK to 17

### VS Code

1. Install extensions:
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Lombok Annotations Support
2. Java SDK 17 should be configured in settings

## Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/3.2.0/reference/html/)
- [Spring Security](https://docs.spring.io/spring-security/reference/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [JWT.io](https://jwt.io/) - JWT debugger
- [SM-2 Algorithm](https://www.supermemo.com/en/archives1990-2015/english/ol/sm2) - Original SuperMemo algorithm
