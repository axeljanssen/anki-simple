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

### Frontend (React + Vite)
```bash
cd frontend
npm install               # Install dependencies
npm run dev              # Run dev server (port 5173)
npm run build            # Production build
npm run lint             # Run ESLint
```

## Architecture

### Backend Structure

The backend follows a layered architecture organized by domain features:

- **Domain Modules**: `user`, `vocabulary`, `review`, `tag` - each contains:
  - Entity classes (JPA entities)
  - Repository interfaces (Spring Data JPA)
  - Service classes (business logic)
  - Controller classes (REST endpoints)
  - DTOs in `dto/` subdirectories

- **Security Layer**: `security/` and `config/`
  - JWT-based authentication with `JwtUtil` and `JwtAuthenticationFilter`
  - `CustomUserDetailsService` for user loading
  - `SecurityConfig` configures Spring Security with stateless sessions
  - Public endpoints: `/api/auth/**`, `/h2-console/**`
  - All other endpoints require JWT authentication

- **Spaced Repetition**: `SpacedRepetitionService` (backend/src/main/java/com/anki/simple/review/SpacedRepetitionService.java:1)
  - Implements SM-2 algorithm for card scheduling
  - Quality ratings 0-5 affect ease factor, interval, and repetitions
  - Quality < 3 resets card to 1-day interval
  - Quality >= 3 advances interval: 1 day → 6 days → interval * ease_factor

### Frontend Structure

- **Routing**: React Router with protected routes via `ProtectedRoute` component
- **Authentication**: `AuthContext` provides auth state and JWT token management
  - Token stored in localStorage
  - Axios interceptor auto-adds Bearer token to requests
- **API Layer**: `services/api.js` centralizes all API calls
  - Organized into `authAPI`, `vocabularyAPI`, `reviewAPI`, `tagAPI`
  - Base URL: `http://localhost:8080/api`
- **Pages**: `Login`, `Signup`, `Dashboard`, `Review`
- **Components**: Reusable UI components like `VocabularyList`, `VocabularyForm`

### Key Data Models

**VocabularyCard** (backend/src/main/java/com/anki/simple/vocabulary/VocabularyCard.java:1):
- Core fields: `front`, `back`, `exampleSentence`, `sourceLanguage`, `targetLanguage`, `audioUrl`
- SM-2 fields: `easeFactor` (default 2.5), `intervalDays`, `repetitions`, `lastReviewed`, `nextReview`
- Relationships: belongs to User (ManyToOne), has Tags (ManyToMany), has ReviewHistory (OneToMany)
- Cards become "due" when `nextReview` <= current time

### Database

- H2 in-memory database (resets on restart)
- H2 Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:ankidb`
  - Username: `sa`, Password: (empty)
- JPA with `ddl-auto=update` auto-creates tables from entities
- Main tables: `vocabulary_cards`, `users`, `tags`, `review_history`, `card_tags` (join table)

### Authentication Flow

1. User signs up/logs in via `/api/auth/signup` or `/api/auth/login`
2. Backend returns JWT token in `AuthResponse`
3. Frontend stores token in localStorage
4. All subsequent requests include `Authorization: Bearer <token>` header
5. `JwtAuthenticationFilter` validates token and sets Spring Security context
6. JWT expires after 24 hours (configurable in application.properties)

### Review System Flow

1. Dashboard shows cards due count via `/api/vocabulary/due/count`
2. Review page fetches due cards via `/api/vocabulary/due`
3. User reviews card and submits quality rating (0-5) via `/api/review`
4. `ReviewService` calls `SpacedRepetitionService.updateCardSchedule()`
5. Card's SM-2 fields updated and saved to database
6. `ReviewHistory` record created to track the review

## Important Configuration

### Security & CORS
- CORS configured for `http://localhost:5173` in SecurityConfig.java:56
- JWT secret in application.properties:20 (MUST change in production)
- CSRF disabled for stateless API

### Development Notes
- Backend SQL logging enabled (`spring.jpa.show-sql=true`)
- H2 console enabled for debugging database state
- Frame options disabled for H2 console access
