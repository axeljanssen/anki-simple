# Simple Anki - Vocabulary Learning Application

A modern vocabulary learning application built with React and Spring Boot, featuring spaced repetition learning using the SM-2 algorithm.

## Features

- **User Authentication**: JWT-based authentication with signup and login
- **Vocabulary Management**: Create, edit, and delete vocabulary cards
- **Multi-language Support**: Track vocabulary in different languages
- **Example Sentences**: Add contextual examples to vocabulary cards
- **Tags/Categories**: Organize vocabulary by topics or categories
- **Audio Support**: Add audio pronunciation URLs to cards
- **Spaced Repetition**: Smart review scheduling using the SM-2 algorithm
- **Review System**: Interactive flashcard-style review interface
- **Progress Tracking**: View total cards and cards due for review

## Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: H2 (in-memory)
- **Security**: Spring Security with JWT
- **Build Tool**: Maven

### Frontend
- **Framework**: React 18
- **Build Tool**: Vite
- **Routing**: React Router v6
- **HTTP Client**: Axios
- **Styling**: CSS3

## Project Structure

```
anki-simple/
├── backend/                      # Spring Boot backend
│   ├── src/
│   │   └── main/
│   │       ├── java/com/anki/simple/
│   │       │   ├── user/         # User management
│   │       │   ├── vocabulary/   # Vocabulary cards
│   │       │   ├── review/       # Review and SM-2 algorithm
│   │       │   ├── tag/          # Tag management
│   │       │   ├── security/     # JWT authentication
│   │       │   └── config/       # Security configuration
│   │       └── resources/
│   │           └── application.properties
│   └── pom.xml
│
└── frontend/                     # React frontend
    ├── src/
    │   ├── components/           # Reusable components
    │   ├── pages/                # Page components
    │   ├── services/             # API services
    │   ├── context/              # React context (Auth)
    │   └── App.jsx
    └── package.json
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- Maven 3.6 or higher

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

**Important Endpoints:**
- `POST /api/auth/signup` - User registration
- `POST /api/auth/login` - User login
- `GET /api/vocabulary` - Get all vocabulary cards
- `GET /api/vocabulary/due` - Get cards due for review
- `POST /api/review` - Submit a review

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

### 2. Add Vocabulary Cards
- Click "Add New Card" on the dashboard
- Fill in:
  - **Front**: The word or question
  - **Back**: The translation or answer
  - **Example Sentence**: (Optional) Context for the word
  - **Languages**: (Optional) Source and target languages
  - **Audio URL**: (Optional) Link to pronunciation audio
  - **Tags**: (Optional) Categorize the card

### 3. Review System
- Cards are scheduled using the SM-2 algorithm
- Click "Start Review" when cards are due
- For each card:
  1. Try to recall the answer
  2. Click "Show Answer"
  3. Rate your recall:
     - **Again**: Complete blackout (0)
     - **Hard**: Difficult recall (3)
     - **Good**: Some hesitation (4)
     - **Easy**: Perfect recall (5)

### 4. Spaced Repetition Algorithm
The app uses the SM-2 algorithm:
- Initial interval: 1 day
- Second interval: 6 days
- Subsequent intervals: previous interval × ease factor
- Ease factor adjusts based on your performance
- Failed cards (Again/Hard) reset to 1-day interval

## API Documentation

### Authentication

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

### Vocabulary

**POST /api/vocabulary**
```json
{
  "front": "Hello",
  "back": "Hola",
  "exampleSentence": "Hello, how are you?",
  "sourceLanguage": "EN",
  "targetLanguage": "ES",
  "tagIds": [1, 2]
}
```

**GET /api/vocabulary** - Get all cards
**GET /api/vocabulary/due** - Get due cards
**PUT /api/vocabulary/{id}** - Update card
**DELETE /api/vocabulary/{id}** - Delete card

### Review

**POST /api/review**
```json
{
  "cardId": 1,
  "quality": 4
}
```

Quality scale: 0-5 (0=blackout, 5=perfect)

## Database

The application uses H2 in-memory database. You can access the H2 console at:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:ankidb`
- Username: `sa`
- Password: (empty)

## Security

- Passwords are encrypted using BCrypt
- JWT tokens expire after 24 hours
- CORS is configured for localhost:5173
- All endpoints except auth are protected

## Future Enhancements

- File upload for audio recordings
- Statistics and progress charts
- Card import/export
- Mobile app
- Shared decks
- Multiple deck support
- Dark mode

## License

MIT License
