# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A vocabulary learning application using spaced repetition (SM-2 algorithm). Full-stack application with Spring Boot backend and React frontend.

## Development Commands

### Backend (Spring Boot)
```bash
cd backend
mvn clean install          # Build project
mvn spring-boot:run        # Run backend server (port 8080)
mvn test                   # Run tests
```

📖 **For detailed backend documentation, see [backend/CLAUDE.md](backend/CLAUDE.md)**

### Frontend (React + Vite)
```bash
cd frontend
npm install               # Install dependencies
npm run dev              # Run dev server (port 5173)
npm run build            # Production build
npm run lint             # Run ESLint
```

📖 **For detailed frontend documentation, see [frontend/CLAUDE.md](frontend/CLAUDE.md)**

## Architecture Overview

### Backend
- **Spring Boot 3.4.1** with Java 21
- **Layered architecture**: Controllers → Services → Repositories → Entities
- **JWT authentication** with Spring Security
- **PostgreSQL 17** database on port 5431 (credentials via environment variables)
- **H2 in-memory database** for tests only
- **Domain modules**: `user`, `vocabulary`, `review`, `tag`
- **RFC 7807 Problem Details** for error responses
- **Comprehensive test suite** with 119 tests (93% coverage)

### Frontend
- **React 19** with Vite 7
- **TypeScript 5.7+** strict mode with path aliases
- **React Router** for client-side routing
- **Context API** for authentication state
- **Axios** for API calls with JWT interceptor
- **Protected routes** requiring authentication
- **Pages**: Login, Signup, Dashboard, VocabularyTablePage, Review
- **Comprehensive test coverage**: Review (25 tests), Login (5 tests), and more

## Key Features

### Language Support
Vocabulary cards support 10 bidirectional language pairs:
- German ⇄ French, German ⇄ Spanish, German ⇄ Italian
- English ⇄ Spanish, English ⇄ French, English ⇄ German, English ⇄ Italian
- French ⇄ Spanish, French ⇄ Italian
- Spanish ⇄ Italian

Cards use a `languageSelection` enum field (e.g., `EN_ES`, `DE_FR`) that represents both learning directions.

### Spaced Repetition (SM-2 Algorithm)
The application implements the SuperMemo SM-2 algorithm for optimal learning:

- **Quality ratings** (0-5):
  - 0: Complete blackout
  - 1-2: Incorrect recall
  - 3: Correct but difficult
  - 4: Correct with hesitation
  - 5: Perfect recall

- **Scheduling logic**:
  - Quality < 3: Reset to 1-day interval
  - Quality ≥ 3: Advance interval (1 day → 6 days → interval × ease_factor)
  - Ease factor adjusts based on quality (minimum 1.3)

- **Implementation**: `SpacedRepetitionService` in backend

### Authentication Flow
1. User signs up/logs in via `/api/auth/signup` or `/api/auth/login`
2. Backend validates credentials and returns JWT token
3. Frontend stores token in localStorage
4. Axios interceptor adds `Authorization: Bearer <token>` to all requests
5. Backend validates JWT on each request
6. Token expires after 24 hours (configurable)

### Review System Flow
1. **Dashboard**: Shows count of cards due for review
   - Calls `GET /api/vocabulary/due/count`
2. **Review Page**: Fetches due cards
   - Calls `GET /api/vocabulary/due`
3. **User reviews card**: Submits quality rating (0-5)
   - Calls `POST /api/review` with `cardId` and `quality`
4. **Backend processing**:
   - `SpacedRepetitionService` updates card schedule (SM-2 algorithm)
   - Saves updated card to database
   - Creates `ReviewHistory` record
5. **Frontend**: Moves to next card or returns to dashboard

## Quick Reference

### Local Development URLs
- **Backend API**: `http://localhost:8080/api`
- **Frontend Dev Server**: `http://localhost:5173`
- **PostgreSQL Database**: `localhost:5431/ankidb`
  - Username: Set via `${ANKI_DB_USR}` environment variable
  - Password: Set via `${ANKI_DB_PWD}` environment variable

### Environment Variables (Required)
```bash
export ANKI_DB_USR=postgres
export ANKI_DB_PWD=your_password
```

### API Endpoints

**Public** (no authentication required):
- `POST /api/auth/signup` - Register new user
- `POST /api/auth/login` - Authenticate user

**Protected** (JWT token required):
- `GET /api/vocabulary` - Get all user's cards
- `POST /api/vocabulary` - Create new card
- `PUT /api/vocabulary/:id` - Update card
- `DELETE /api/vocabulary/:id` - Delete card
- `GET /api/vocabulary/due` - Get cards due for review
- `GET /api/vocabulary/due/count` - Get count of due cards
- `POST /api/review` - Submit card review with quality rating
- `GET /api/tags` - Get all user's tags
- `POST /api/tags` - Create new tag
- `DELETE /api/tags/:id` - Delete tag

### Configuration

**Backend** (`application.properties`):
- Server port: `8080`
- Database: PostgreSQL on port 5431, database name `ankidb`
- Database credentials: `${ANKI_DB_USR}` and `${ANKI_DB_PWD}` (environment variables)
- JWT expiration: `86400000ms` (24 hours)
- CORS allowed origin: `http://localhost:5173`

**Frontend** (`services/api.js`):
- API base URL: `http://localhost:8080/api`

**⚠️ Production Notes**:
- Set production environment variables (`ANKI_DB_USR`, `ANKI_DB_PWD`)
- Change JWT secret in `application.properties`
- Update CORS configuration for production domain
- Use strong database password for production
- Update frontend API_BASE_URL for production backend
- Enable HTTPS/TLS

## Development Workflow

### Starting the Application

1. **Set Environment Variables**:
   ```bash
   export ANKI_DB_USR=postgres
   export ANKI_DB_PWD=your_password
   ```

2. **Ensure PostgreSQL is running**:
   - PostgreSQL 17 must be running on port 5431
   - Database `ankidb` must exist
   - Create database if needed: `CREATE DATABASE ankidb;`

3. **Start Backend**:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   Backend runs on `http://localhost:8080`
   Flyway will automatically create/migrate the database schema

4. **Start Frontend**:
   ```bash
   cd frontend
   npm run dev
   ```
   Frontend runs on `http://localhost:5173`

5. **Access Application**:
   - Open browser: `http://localhost:5173`
   - Sign up for a new account
   - Start creating vocabulary cards

### Running Tests

**Backend**:
```bash
cd backend
mvn test                              # All tests
mvn test -Dtest=SpacedRepetitionServiceTest  # Specific test
```

**Frontend**:
```bash
cd frontend
npm run lint                          # ESLint
```

### Code Quality & Coverage

The project uses **SonarCloud** for continuous code quality and security analysis:
- Multi-module analysis (backend + frontend)
- **Backend test coverage: 93%** (119 comprehensive tests)
- **Frontend test coverage**: Comprehensive component tests (53+ tests)
- Automated CI/CD integration via GitHub Actions
- Coverage reports: JaCoCo (backend) + LCOV (frontend)

📖 **For detailed SonarCloud configuration, see [backend/CLAUDE.md](backend/CLAUDE.md)**

## Database

**Development** (PostgreSQL 17):
- PostgreSQL runs on port 5431
- Database name: `ankidb`
- Credentials: Set via `${ANKI_DB_USR}` and `${ANKI_DB_PWD}` environment variables
- Data persists in PostgreSQL data directory
- Flyway manages schema migrations automatically
- To reset database: Drop and recreate the `ankidb` database
- Tests use in-memory H2 for speed (see `application-test.properties`)

**Setup PostgreSQL**:
```bash
# Create the database and user (if they don't exist)
psql -U postgres -p 5431 -c "CREATE USER ankidb WITH PASSWORD 'ankidb';"
psql -U postgres -p 5431 -c "CREATE DATABASE ankidb OWNER ankidb;"

# Or connect to PostgreSQL and run:
CREATE USER ankidb WITH PASSWORD 'ankidb';
CREATE DATABASE ankidb OWNER ankidb;
```

**Main Tables**:
- `users` - User accounts
- `vocabulary_cards` - Cards with SM-2 data
- `tags` - Card organization tags
- `review_history` - Historical review data
- `card_tags` - Many-to-many relationship

**Entity Relationships**:
- User → VocabularyCards (one-to-many)
- User → Tags (one-to-many)
- VocabularyCard → Tags (many-to-many)
- VocabularyCard → ReviewHistory (one-to-many)

## Troubleshooting

**Backend won't start**:
- Check port 8080 is available: `lsof -i :8080`
- Verify Java 21 is installed: `java -version`
- Check Maven dependencies: `mvn clean install`

**Frontend won't start**:
- Check port 5173 is available
- Verify Node.js is installed: `node --version`
- Reinstall dependencies: `rm -rf node_modules && npm install`

**CORS errors**:
- Ensure backend is running
- Check CORS configuration in `SecurityConfig.java`
- Verify frontend URL matches allowed origin

**Authentication issues**:
- Check token in localStorage (browser DevTools)
- Verify token hasn't expired (24-hour default)
- Check backend logs for JWT validation errors

**Database issues**:
- Ensure PostgreSQL is running on port 5431
- Verify database `ankidb` exists
- Check database credentials match `application.properties`
- Check entity annotations and relationships
- To reset database: Drop and recreate `ankidb` database
- Tests use in-memory H2 (no PostgreSQL needed for tests)

## Additional Resources

- **Backend**: See [backend/CLAUDE.md](backend/CLAUDE.md)
- **Frontend**: See [frontend/CLAUDE.md](frontend/CLAUDE.md)
- **SM-2 Algorithm**: [SuperMemo Documentation](https://www.supermemo.com/en/archives1990-2015/english/ol/sm2)
- to memorize