# Simple Anki - Vocabulary Learning Application

A modern vocabulary learning application built with React 19 and Spring Boot 3, featuring spaced repetition learning using the SM-2 algorithm.

## Features

- **User Authentication**: JWT-based authentication with signup and login
- **Vocabulary Management**: Create, edit, and delete vocabulary cards with full CRUD operations
- **Multi-language Support**: 10 bidirectional language pairs (German ⇄ French, English ⇄ Spanish, etc.)
- **Example Sentences**: Add contextual examples to vocabulary cards
- **Tags/Categories**: Organize vocabulary by topics with custom colors
- **Audio Support**: Add audio pronunciation URLs to cards
- **Spaced Repetition**: Smart review scheduling using the SM-2 algorithm
- **Review System**: Interactive flashcard-style review interface with keyboard shortcuts
- **Progress Tracking**: Real-time count of total cards and cards due for review
- **Accessibility**: WCAG 2.1 compliant with full keyboard navigation support
- **Code Quality**: Integrated SonarCloud analysis with 93% backend test coverage

## Technology Stack

### Backend
- **Framework**: Spring Boot 3.4.1
- **Language**: Java 21
- **Database**: PostgreSQL 17 (production), H2 (tests)
- **Security**: Spring Security with JWT authentication
- **ORM**: Spring Data JPA with Hibernate
- **Migrations**: Flyway for database versioning
- **Mapping**: MapStruct for DTO/entity conversions
- **Testing**: JUnit 5, Mockito, AssertJ, 93% coverage (119 tests)
- **Build Tool**: Maven 3.9+

### Frontend
- **Language**: TypeScript 5.7+ (strict mode)
- **Framework**: React 19
- **Build Tool**: Vite 7.2.4
- **Routing**: React Router 6
- **HTTP Client**: Axios with TypeScript support
- **Styling**: Tailwind CSS 3.4.0 + Sass 1.94.0
- **State Management**: Context API for authentication
- **Testing**: Vitest 4 with React Testing Library (comprehensive component tests)

## Project Structure

```
anki-simple/
├── backend/                      # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/anki/simple/
│   │   │   │   ├── user/         # User domain (entity, service, controller, DTOs)
│   │   │   │   ├── vocabulary/   # Vocabulary cards domain
│   │   │   │   ├── review/       # Review system and SM-2 algorithm
│   │   │   │   ├── tag/          # Tag management
│   │   │   │   ├── security/     # JWT authentication components
│   │   │   │   └── config/       # Security and application configuration
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── db/migration/ # Flyway migration scripts
│   │   └── test/                 # Comprehensive test suite (40+ tests)
│   └── pom.xml
│
└── frontend/                     # React TypeScript frontend
    ├── src/
    │   ├── components/           # Reusable UI components
    │   ├── pages/                # Page components (Dashboard, Review, etc.)
    │   ├── services/             # API service layer with TypeScript types
    │   ├── context/              # React Context (AuthContext)
    │   ├── types/                # TypeScript type definitions
    │   ├── styles/               # Sass stylesheets
    │   └── App.tsx
    └── package.json
```

## Getting Started

### Prerequisites

- **Java 21** or higher
- **Node.js 18** or higher
- **Maven 3.9** or higher
- **PostgreSQL 17** running on port 5431

### Database Setup

1. Start PostgreSQL 17 on port 5431

2. Create the database:
   ```bash
   psql -U postgres -p 5431 -c "CREATE DATABASE ankidb;"
   ```

3. The application will automatically run Flyway migrations on startup

### Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

The backend will start on `http://localhost:8080`

**Configuration** (`application.properties`):
- Database: `jdbc:postgresql://localhost:5431/ankidb`
- Username: `${ANKI_DB_USR}` (environment variable)
- Password: `${ANKI_DB_PWD}` (environment variable)
- JWT expiration: 24 hours
- CORS allowed origin: `http://localhost:5173`

**Environment Variables** (required):
```bash
export ANKI_DB_USR=postgres
export ANKI_DB_PWD=your_password
```

### Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm run dev
   ```

The frontend will start on `http://localhost:5173`

## Usage Guide

### 1. Sign Up / Login
- Create a new account or login with existing credentials
- JWT token is stored in localStorage for subsequent requests
- Token expires after 24 hours

### 2. Add Vocabulary Cards
- Click "Add New Card" on the dashboard
- Fill in:
  - **Front**: The word or question (required)
  - **Back**: The translation or answer (required)
  - **Example Sentence**: (Optional) Context for the word
  - **Language Pair**: Select from 10 bidirectional options (e.g., "English ⇄ Spanish")
  - **Audio URL**: (Optional) Link to pronunciation audio
  - **Tags**: (Optional) Categorize with colored tags

### 3. Review System
- Cards are scheduled using the SM-2 algorithm
- Click "Start Review" when cards are due
- Keyboard shortcuts available (press '?' to view)
- For each card:
  1. Try to recall the answer
  2. Press Space or click "Show Answer"
  3. Rate your recall (0-5):
     - **0**: Complete blackout
     - **1**: Incorrect, but recognized
     - **2**: Incorrect, seemed familiar
     - **3**: Correct, but difficult
     - **4**: Correct, with hesitation
     - **5**: Perfect recall

### 4. Spaced Repetition Algorithm (SM-2)
- **Quality < 3**: Resets card to 1-day interval
- **Quality ≥ 3**: Advances interval
  - First review: 1 day
  - Second review: 6 days
  - Subsequent: interval × ease_factor
- Ease factor adjusts based on performance (minimum 1.3)

## Language Pairs

The application supports 10 bidirectional language pairs:
- German ⇄ French (DE_FR)
- German ⇄ Spanish (DE_ES)
- German ⇄ Italian (DE_IT)
- English ⇄ Spanish (EN_ES)
- English ⇄ French (EN_FR)
- English ⇄ German (EN_DE)
- English ⇄ Italian (EN_IT)
- French ⇄ Spanish (FR_ES)
- French ⇄ Italian (FR_IT)
- Spanish ⇄ Italian (ES_IT)

## API Documentation

### Authentication (Public)

**POST /api/v1/auth/signup**
```json
{
  "username": "user123",
  "email": "user@example.com",
  "password": "password123"
}
```

**POST /api/v1/auth/login**
```json
{
  "username": "user123",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "user123",
  "email": "user@example.com"
}
```

### Vocabulary (Protected - requires JWT)

**POST /api/v1/vocabulary**
```json
{
  "front": "Hello",
  "back": "Hola",
  "exampleSentence": "Hello, how are you?",
  "languageSelection": "EN_ES",
  "audioUrl": "https://example.com/audio.mp3",
  "tagIds": [1, 2]
}
```

**GET /api/v1/vocabulary** - Get all cards (lean response for performance)
**GET /api/v1/vocabulary/due** - Get cards due for review
**GET /api/v1/vocabulary/due/count** - Get count of due cards
**PUT /api/v1/vocabulary/{id}** - Update card
**DELETE /api/v1/vocabulary/{id}** - Delete card

### Review (Protected)

**POST /api/v1/review**
```json
{
  "cardId": 1,
  "quality": 4
}
```

Quality scale: 0-5 (0=blackout, 5=perfect)

### Tags (Protected)

**GET /api/v1/tags** - Get all user's tags
**POST /api/v1/tags** - Create new tag
**DELETE /api/v1/tags/{id}** - Delete tag

## Database

### Production
- **Database**: PostgreSQL 17
- **Port**: 5431
- **Database name**: ankidb
- **Migrations**: Flyway automatic versioning

### Development
- **Tests**: H2 in-memory database (fast, isolated)
- **Schema**: Managed by Flyway migrations in `src/main/resources/db/migration/`

### Main Tables
- `users` - User accounts
- `vocabulary_cards` - Cards with SM-2 scheduling data
- `tags` - Organization tags with colors
- `review_history` - Historical review records
- `card_tags` - Many-to-many relationship
- `flyway_schema_history` - Migration tracking

## Security

- Passwords encrypted using BCrypt
- JWT tokens with 24-hour expiration
- CORS configured for `http://localhost:5173`
- All endpoints except `/api/v1/auth/**` are protected
- **Environment Variables**: Database credentials use `${ANKI_DB_USR}` and `${ANKI_DB_PWD}`
- **Production checklist**:
  - Set environment variables for database credentials
  - Change JWT secret in `application.properties`
  - Update CORS configuration for production domain
  - Enable HTTPS/TLS
  - Use strong passwords for production database

## Testing

### Backend Tests
```bash
cd backend
mvn test                    # Run all tests
mvn test jacoco:report      # Generate coverage report
```
- **119 comprehensive tests** covering all domains
- **93% code coverage** (exceeds 80% target)
- **Test coverage by domain**:
  - GlobalExceptionHandler: 97% (11 tests)
  - JwtAuthenticationFilter: 100% (7 tests)
  - VocabularyService: 95% (24 tests including search/sort)
  - ReviewService, TagService, UserService: 90%+ coverage
- Integration tests use H2 in-memory database

### Frontend Tests
```bash
cd frontend
npm test                    # Run tests
npm run test:coverage       # Coverage report
```
- **Comprehensive component tests** with Vitest and React Testing Library
- **Test suites**:
  - Review.test.tsx: 25 tests (loading, cards, keyboard shortcuts, quality ratings)
  - Login.test.tsx: 5 tests (authentication flow)
  - VocabularyList.test.tsx: 10 tests (card display, editing)
  - AuthContext.test.tsx: 10 tests (state management)
  - ProtectedRoute.test.tsx: 3 tests (route guards)
- Full coverage of user interactions and edge cases

## Code Quality

- **SonarCloud**: Continuous code quality analysis
- **Coverage**: Backend 93% (119 tests), Frontend comprehensive
- **CI/CD**: GitHub Actions integration
- **Reports**: JaCoCo (backend), LCOV (frontend)
- **Code Standards**:
  - Strict TypeScript mode
  - MapStruct for type-safe mappings
  - Environment variables for sensitive data
  - WCAG 2.1 accessibility compliance

## Development Resources

📖 **Detailed Documentation:**
- Root: [CLAUDE.md](CLAUDE.md) - Project overview and quick start
- Backend: [backend/CLAUDE.md](backend/CLAUDE.md) - Architecture, patterns, testing
- Frontend: [frontend/CLAUDE.md](frontend/CLAUDE.md) - TypeScript, React patterns, styling

## Troubleshooting

**Backend won't start:**
- Verify PostgreSQL is running on port 5431
- Check database `ankidb` exists
- Ensure Java 21 is installed: `java -version`
- Check credentials in `application.properties`

**Frontend won't start:**
- Verify Node.js 18+: `node --version`
- Reinstall dependencies: `rm -rf node_modules && npm install`
- Check port 5173 is available

**CORS errors:**
- Ensure backend is running on port 8080
- Check CORS configuration in `SecurityConfig.java`

**Database issues:**
- Verify PostgreSQL connection: `psql -U postgres -p 5431 -d ankidb`
- Check Flyway migration status: `SELECT * FROM flyway_schema_history;`
- To reset: Drop and recreate database

## Architecture Highlights

- **Layered Architecture**: Controllers → Services → Repositories → Entities
- **MapStruct**: Type-safe DTO/entity mapping
- **Flyway**: Version-controlled database migrations
- **TypeScript Strict Mode**: Full type safety with path aliases (`@/`)
- **Context API**: Centralized authentication state
- **Protected Routes**: Client-side route guards
- **RFC 7807**: Problem Details for error responses

## License

MIT License
