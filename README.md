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
- **Code Quality**: Integrated SonarCloud analysis with 81% backend test coverage

## Technology Stack

### Backend
- **Framework**: Spring Boot 3.4.1
- **Language**: Java 21
- **Database**: PostgreSQL 17 (production), H2 (tests)
- **Security**: Spring Security with JWT authentication
- **ORM**: Spring Data JPA with Hibernate
- **Migrations**: Flyway for database versioning
- **Mapping**: MapStruct for DTO/entity conversions
- **Testing**: JUnit 5, Mockito, 81% coverage
- **Build Tool**: Maven 3.9+

### Frontend
- **Language**: TypeScript 5.7+ (strict mode)
- **Framework**: React 19
- **Build Tool**: Vite 7.2.4
- **Routing**: React Router 6
- **HTTP Client**: Axios with TypeScript support
- **Styling**: Tailwind CSS 3.4.0 + Sass 1.94.0
- **State Management**: Context API for authentication
- **Testing**: Vitest 4 with React Testing Library

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
- Username: `postgres`
- Password: `postgres`
- JWT expiration: 24 hours
- CORS allowed origin: `http://localhost:5173`

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

**POST /api/auth/signup**
```json
{
  "username": "user123",
  "email": "user@example.com",
  "password": "password123"
}
```

**POST /api/auth/login**
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

**POST /api/vocabulary**
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

**GET /api/vocabulary** - Get all cards (lean response for performance)
**GET /api/vocabulary/due** - Get cards due for review
**GET /api/vocabulary/due/count** - Get count of due cards
**PUT /api/vocabulary/{id}** - Update card
**DELETE /api/vocabulary/{id}** - Delete card

### Review (Protected)

**POST /api/review**
```json
{
  "cardId": 1,
  "quality": 4
}
```

Quality scale: 0-5 (0=blackout, 5=perfect)

### Tags (Protected)

**GET /api/tags** - Get all user's tags
**POST /api/tags** - Create new tag
**DELETE /api/tags/{id}** - Delete tag

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
- All endpoints except `/api/auth/**` are protected
- Production checklist:
  - Change JWT secret in `application.properties`
  - Update PostgreSQL credentials
  - Update CORS configuration for production domain
  - Enable HTTPS/TLS

## Testing

### Backend Tests
```bash
cd backend
mvn test                    # Run all tests
mvn test jacoco:report      # Generate coverage report
```
- 40+ comprehensive tests
- 81% code coverage
- Integration tests use H2 in-memory database

### Frontend Tests
```bash
cd frontend
npm test                    # Run tests
npm run test:coverage       # Coverage report
```
- Unit tests with Vitest
- Component tests with React Testing Library

## Code Quality

- **SonarCloud**: Continuous code quality analysis
- **Coverage**: Backend 81%, Frontend tracked
- **CI/CD**: GitHub Actions integration
- **Reports**: JaCoCo (backend), LCOV (frontend)

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
