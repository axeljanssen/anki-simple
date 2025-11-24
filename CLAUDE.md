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
- **Spring Boot 3.2.0** with Java 17
- **Layered architecture**: Controllers → Services → Repositories → Entities
- **JWT authentication** with Spring Security
- **H2 in-memory database** for development
- **Domain modules**: `user`, `vocabulary`, `review`, `tag`
- **RFC 7807 Problem Details** for error responses
- **Comprehensive test suite** with 40+ tests

### Frontend
- **React 18** with Vite
- **React Router** for client-side routing
- **Context API** for authentication state
- **Axios** for API calls with JWT interceptor
- **Protected routes** requiring authentication
- **Pages**: Login, Signup, Dashboard, Review

## Key Features

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
- **H2 Database Console**: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:ankidb`
  - Username: `sa`
  - Password: (empty)

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
- Database: H2 in-memory
- JWT expiration: `86400000ms` (24 hours)
- CORS allowed origin: `http://localhost:5173`

**Frontend** (`services/api.js`):
- API base URL: `http://localhost:8080/api`

**⚠️ Production Notes**:
- Change JWT secret in `application.properties`
- Update CORS configuration for production domain
- Use persistent database (PostgreSQL, MySQL)
- Update frontend API_BASE_URL for production backend

## Development Workflow

### Starting the Application

1. **Start Backend**:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   Backend runs on `http://localhost:8080`

2. **Start Frontend**:
   ```bash
   cd frontend
   npm run dev
   ```
   Frontend runs on `http://localhost:5173`

3. **Access Application**:
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

## Database

**Development** (H2 in-memory):
- Resets on application restart
- Good for development and testing
- Access via H2 console for debugging

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
- Verify Java 17 is installed: `java -version`
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
- Remember H2 database resets on restart
- Use H2 console to inspect data
- Check entity annotations and relationships

## Additional Resources

- **Backend**: See [backend/CLAUDE.md](backend/CLAUDE.md)
- **Frontend**: See [frontend/CLAUDE.md](frontend/CLAUDE.md)
- **SM-2 Algorithm**: [SuperMemo Documentation](https://www.supermemo.com/en/archives1990-2015/english/ol/sm2)
