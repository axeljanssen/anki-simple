# Frontend - CLAUDE.md

Frontend documentation for the Simple Anki vocabulary learning application.

## Quick Start

```bash
# Install dependencies
npm install

# Run development server (port 5173)
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Run ESLint
npm run lint
```

## Technology Stack

- **React 18** - UI library
- **Vite** - Build tool and dev server
- **React Router** - Client-side routing
- **Axios** - HTTP client for API calls
- **Context API** - State management for authentication
- **CSS** - Styling (no framework currently)

## Project Structure

```
src/
├── assets/           # Static assets (images, etc.)
├── components/       # Reusable UI components
├── context/          # React Context providers
│   └── AuthContext.jsx    # Authentication state management
├── pages/            # Page components
│   ├── Login.jsx
│   ├── Signup.jsx
│   ├── Dashboard.jsx
│   └── Review.jsx
├── services/         # API service layer
│   └── api.js       # Centralized API calls
├── utils/            # Utility functions
├── App.jsx           # Main application component
├── main.jsx          # Application entry point
└── index.css         # Global styles
```

## Architecture

### Routing Structure

The application uses React Router with protected routes:

```javascript
<Routes>
  <Route path="/login" element={<Login />} />
  <Route path="/signup" element={<Signup />} />
  <Route path="/" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
  <Route path="/review" element={<ProtectedRoute><Review /></ProtectedRoute>} />
</Routes>
```

- **Public routes**: `/login`, `/signup`
- **Protected routes**: `/` (Dashboard), `/review`
- `ProtectedRoute` component checks authentication and redirects to login if needed

### Authentication Flow

**AuthContext** (`context/AuthContext.jsx`):
- Provides authentication state and methods throughout the app
- Stores JWT token in localStorage
- Manages user login/logout state
- Provides `authAPI` methods: `login()`, `signup()`, `logout()`

**Axios Interceptor**:
- Automatically adds `Authorization: Bearer <token>` header to all API requests
- Configured in `services/api.js`

**Authentication Process**:
1. User submits login/signup form
2. Frontend calls `/api/auth/login` or `/api/auth/signup`
3. Backend returns JWT token in response
4. Token stored in localStorage
5. AuthContext updates with user data
6. All subsequent API calls include token in header
7. Protected routes become accessible

### API Layer

**Services** (`services/api.js`):
Centralizes all API calls organized by domain:

```javascript
// Base configuration
const API_BASE_URL = 'http://localhost:8080/api'

// API modules
export const authAPI = {
  login: (credentials) => POST /api/auth/login
  signup: (userData) => POST /api/auth/signup
}

export const vocabularyAPI = {
  getAll: () => GET /api/vocabulary
  create: (card) => POST /api/vocabulary
  update: (id, card) => PUT /api/vocabulary/:id
  delete: (id) => DELETE /api/vocabulary/:id
  getDue: () => GET /api/vocabulary/due
  getDueCount: () => GET /api/vocabulary/due/count
}

export const reviewAPI = {
  submitReview: (cardId, quality) => POST /api/review
}

export const tagAPI = {
  getAll: () => GET /api/tags
  create: (tag) => POST /api/tags
  delete: (id) => DELETE /api/tags/:id
}
```

**Axios Configuration**:
- Base URL: `http://localhost:8080/api`
- Request interceptor: Adds JWT token to Authorization header
- Response interceptor: Handles 401 errors (token expiration)

### State Management

**Context API**:
- **AuthContext**: Global authentication state
  - `user`: Current user data
  - `token`: JWT token
  - `login()`: Authenticate user
  - `signup()`: Register new user
  - `logout()`: Clear authentication

**Component State**:
- Local state with `useState` for component-specific data
- Form handling with controlled components

### Component Organization

**Pages** (`pages/`):
- **Login**: User login form
- **Signup**: User registration form
- **Dashboard**: Main view showing vocabulary cards and due count
- **Review**: Review interface for due cards with quality rating

**Components** (`components/`):
- **ProtectedRoute**: Route wrapper that requires authentication
- **VocabularyList**: Display list of vocabulary cards
- **VocabularyForm**: Form for creating/editing cards
- Other reusable UI components

## Key Features

### Authentication
- JWT-based authentication with token stored in localStorage
- Automatic token inclusion in API requests via Axios interceptor
- Protected routes redirect to login when unauthenticated
- Logout clears token and redirects to login

### Vocabulary Management
- Create, read, update, delete vocabulary cards
- Cards include: front, back, example sentence, languages, audio URL
- Tag organization for cards
- Display all cards or filter by tags

### Spaced Repetition Review
- Dashboard shows count of cards due for review
- Review page fetches due cards from backend
- User rates recall quality (0-5 scale):
  - 0: Complete blackout
  - 1: Incorrect, but recognized
  - 2: Incorrect, seemed familiar
  - 3: Correct, but difficult
  - 4: Correct, with hesitation
  - 5: Perfect recall
- Backend updates card scheduling based on SM-2 algorithm
- Review history tracked for analytics

## Development

### Running Locally

1. Ensure backend is running on `http://localhost:8080`
2. Install dependencies: `npm install`
3. Start dev server: `npm run dev`
4. Open browser: `http://localhost:5173`

### Environment Configuration

Update API base URL in `services/api.js` if backend runs on different port:
```javascript
const API_BASE_URL = 'http://localhost:8080/api'
```

### Code Style

- Use functional components with hooks
- Prefer arrow functions for components
- Use destructuring for props
- Keep components focused and small
- Extract reusable logic into custom hooks

### Common Development Tasks

**Add new API endpoint**:
1. Add method to appropriate API module in `services/api.js`
2. Use axios with appropriate HTTP method
3. Return the promise for error handling

**Create new page**:
1. Create component in `pages/` directory
2. Add route in `App.jsx`
3. Wrap with `ProtectedRoute` if authentication required

**Add new protected route**:
```javascript
<Route
  path="/new-page"
  element={<ProtectedRoute><NewPage /></ProtectedRoute>}
/>
```

**Access authentication state**:
```javascript
import { useAuth } from '../context/AuthContext'

function MyComponent() {
  const { user, logout } = useAuth()
  // Use user data or logout function
}
```

## API Integration

### Request Format

All API requests (except auth) require JWT token:
```javascript
Authorization: Bearer <jwt-token>
```

### Response Format

**Success** (2xx):
```json
{
  "id": 1,
  "front": "Hello",
  "back": "Hola",
  ...
}
```

**Error** (4xx/5xx) - RFC 7807 Problem Details:
```json
{
  "type": "about:blank",
  "title": "Card Not Found",
  "status": 404,
  "detail": "Vocabulary card not found with id: 999",
  "instance": "/api/vocabulary/999",
  "timestamp": "2024-11-24T18:00:00"
}
```

### Error Handling

```javascript
try {
  const response = await vocabularyAPI.getAll()
  // Handle success
} catch (error) {
  if (error.response) {
    // Server responded with error status
    console.error(error.response.data.detail)
  } else if (error.request) {
    // Request made but no response
    console.error('No response from server')
  } else {
    // Request setup error
    console.error('Request error:', error.message)
  }
}
```

## Building for Production

```bash
# Build production bundle
npm run build

# Preview production build locally
npm run preview
```

**Build output**: `dist/` directory
- Optimized and minified JavaScript
- CSS extracted and minified
- Assets copied and hashed for cache busting

**Deployment**:
- Serve `dist/` directory as static files
- Configure web server to redirect all routes to `index.html` (for client-side routing)
- Update `API_BASE_URL` in `api.js` to production backend URL

## Troubleshooting

**CORS errors**:
- Ensure backend CORS is configured for frontend URL
- Check `SecurityConfig.java` in backend for allowed origins
- Default: `http://localhost:5173`

**Authentication issues**:
- Check token exists in localStorage: `localStorage.getItem('token')`
- Verify token format: Should be `Bearer <jwt-token>`
- Check token expiration (default 24 hours)
- Backend logs will show JWT validation errors

**API connection errors**:
- Verify backend is running: `http://localhost:8080`
- Check network tab in browser DevTools
- Verify API_BASE_URL in `services/api.js`

**Build errors**:
- Clear node_modules: `rm -rf node_modules && npm install`
- Check Node.js version (requires Node 14+)
- Review console for specific error messages

## Additional Resources

- [React Documentation](https://react.dev/)
- [Vite Documentation](https://vitejs.dev/)
- [React Router Documentation](https://reactrouter.com/)
- [Axios Documentation](https://axios-http.com/)
