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

See [backend/CLAUDE.md](backend/CLAUDE.md) for detailed backend documentation.

### Frontend (React + Vite)
```bash
cd frontend
npm install               # Install dependencies
npm run dev              # Run dev server (port 5173)
npm run build            # Production build
npm run lint             # Run ESLint
```

## Architecture Overview

### Backend
- **Spring Boot 3.2.0** with Java 17
- **Layered architecture**: Controllers → Services → Repositories → Entities
- **JWT authentication** with Spring Security
- **H2 in-memory database** for development
- **Domain modules**: `user`, `vocabulary`, `review`, `tag`

📖 **For detailed backend documentation, see [backend/CLAUDE.md](backend/CLAUDE.md)**

### Frontend
- **React Router** with protected routes via `ProtectedRoute` component
- **Authentication**: `AuthContext` provides auth state and JWT token management
  - Token stored in localStorage
  - Axios interceptor auto-adds Bearer token to requests
- **API Layer**: `services/api.js` centralizes all API calls
  - Organized into `authAPI`, `vocabularyAPI`, `reviewAPI`, `tagAPI`
  - Base URL: `http://localhost:8080/api`
- **Pages**: `Login`, `Signup`, `Dashboard`, `Review`
- **Components**: Reusable UI components like `VocabularyList`, `VocabularyForm`

## Key Features

### Spaced Repetition (SM-2 Algorithm)
- Quality ratings 0-5 affect ease factor, interval, and repetitions
- Quality < 3 resets card to 1-day interval
- Quality >= 3 advances interval: 1 day → 6 days → interval * ease_factor
- Implemented in `SpacedRepetitionService`

### Authentication Flow
1. User signs up/logs in via `/api/auth/signup` or `/api/auth/login`
2. Backend returns JWT token in `AuthResponse`
3. Frontend stores token in localStorage
4. All subsequent requests include `Authorization: Bearer <token>` header
5. JWT expires after 24 hours

### Review System Flow
1. Dashboard shows cards due count via `/api/vocabulary/due/count`
2. Review page fetches due cards via `/api/vocabulary/due`
3. User reviews card and submits quality rating (0-5) via `/api/review`
4. Backend updates card's SM-2 fields and saves to database
5. `ReviewHistory` record created to track the review

## Quick Reference

### Database Access (Development)
- H2 Console: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:ankidb`
- Username: `sa`, Password: (empty)
- **Note**: In-memory database resets on application restart

### API Endpoints
- **Auth**: `/api/auth/signup`, `/api/auth/login` (public)
- **Vocabulary**: `/api/vocabulary` (CRUD operations)
- **Review**: `/api/review` (submit review)
- **Tags**: `/api/tags` (manage tags)
- All endpoints except auth require JWT token

### Configuration
- Backend port: 8080
- Frontend port: 5173
- CORS configured for `http://localhost:5173`
- JWT secret in `application.properties` (MUST change in production)

## Development Notes
- Backend SQL logging enabled for debugging
- H2 console enabled for database inspection
- Frontend uses Vite for fast development and hot module replacement
