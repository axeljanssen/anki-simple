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
- **Tailwind CSS 3.4.0** - Utility-first CSS framework
- **Sass 1.70.0** - CSS preprocessor for custom styles
- **PostCSS & Autoprefixer** - CSS transformation tools
- **Vitest** - Unit testing framework
- **React Testing Library** - Component testing utilities

## Project Structure

```
src/
├── assets/           # Static assets (images, etc.)
├── components/       # Reusable UI components
│   ├── ProtectedRoute.jsx  # Route authentication wrapper
│   ├── VocabularyList.jsx  # Card grid display
│   ├── VocabularyForm.jsx  # Card create/edit form
│   └── VocabularyTable.jsx # Card table display
├── context/          # React Context providers
│   └── AuthContext.jsx    # Authentication state management
├── pages/            # Page components
│   ├── Login.jsx           # Login page
│   ├── Signup.jsx          # Registration page
│   ├── Dashboard.jsx       # Main dashboard (grid view)
│   ├── VocabularyTablePage.jsx  # Table view page
│   └── Review.jsx          # Spaced repetition review
├── services/         # API service layer
│   └── api.js       # Centralized API calls
├── styles/           # Sass stylesheets
│   ├── main.scss         # Main entry (Tailwind directives)
│   ├── _base.scss        # Global base styles
│   ├── _components.scss  # Custom component utilities
│   └── _animations.scss  # Keyframe animations
├── test/             # Test utilities
│   └── setup.js     # Vitest configuration
├── utils/            # Utility functions
├── App.jsx           # Main application component
└── main.jsx          # Application entry point
```

## Architecture

### Routing Structure

The application uses React Router with protected routes:

```javascript
<Routes>
  <Route path="/login" element={<Login />} />
  <Route path="/signup" element={<Signup />} />
  <Route path="/" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
  <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
  <Route path="/table" element={<ProtectedRoute><VocabularyTablePage /></ProtectedRoute>} />
  <Route path="/review" element={<ProtectedRoute><Review /></ProtectedRoute>} />
</Routes>
```

- **Public routes**: `/login`, `/signup`
- **Protected routes**: `/` (Dashboard - grid view), `/table` (Table view), `/review` (Spaced repetition)
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

### Styling with Tailwind CSS

**Approach**:
- Utility-first CSS using Tailwind classes directly in JSX
- Custom Sass modules for reusable components and animations
- Blue color theme (#2563eb) throughout the application
- Responsive design with Tailwind breakpoints (md:, max-md:)

**Configuration Files**:
- `tailwind.config.js` - Tailwind configuration with custom blue theme
- `postcss.config.js` - PostCSS configuration for Tailwind processing
- `src/styles/main.scss` - Main entry point with @tailwind directives

**Custom Sass Utilities**:
- `.tag-pill` - Reusable tag component with dynamic background colors
- `animate-modal-slide` - Modal slide-in animation (modalSlideIn keyframes)
- Global base styles (body background, code font)

**Dynamic Inline Styles**:
- Tag colors: `style={{ backgroundColor: tag.color }}` (database-driven)
- Progress bar: `style={{ width: `${progress}%` }}` (calculated value)

**Example Tailwind Usage**:
```jsx
// Button with gradient and hover effect
<button className="px-6 py-3 bg-gradient-to-br from-blue-600 to-blue-700
                   text-white rounded-lg font-semibold transition-all
                   hover:-translate-y-0.5 hover:shadow-xl">
  Add Card
</button>

// Responsive grid (1 column mobile → 2 tablet → 3 desktop)
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
  {cards.map(card => <CardComponent key={card.id} {...card} />)}
</div>
```

**Build Output**:
- CSS bundle: 18.20 kB uncompressed, 4.18 kB gzipped
- Automatically purges unused Tailwind classes in production

### Component Organization

**Pages** (`pages/`):
- **Login**: User login form with blue gradient background
- **Signup**: User registration form
- **Dashboard**: Main dashboard with card grid view and stats
- **VocabularyTablePage**: Table view with search and CRUD operations
- **Review**: Spaced repetition review interface with quality rating (0-5)

**Components** (`components/`):
- **ProtectedRoute**: Route wrapper that requires authentication
- **VocabularyList**: Card grid display with hover effects and tag colors
- **VocabularyTable**: Tabular card display with sorting and filtering
- **VocabularyForm**: Modal form for creating/editing cards with responsive grid

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

### Testing

**Test Framework**: Vitest with React Testing Library

**Test Suite** (40 tests total):
- `api.test.js` - 12 tests for API service layer
- `VocabularyList.test.jsx` - 10 tests for card list component
- `AuthContext.test.jsx` - 10 tests for authentication context
- `Login.test.jsx` - 5 tests for login page
- `ProtectedRoute.test.jsx` - 3 tests for route protection

**Running Tests**:
```bash
npm test              # Run all tests
npm run test:ui       # Run tests with UI
npm run test:coverage # Generate coverage report
```

**Test Utilities**:
- `src/test/setup.js` - Vitest configuration and global test setup
- Mock implementations for React Router and AuthContext
- `@testing-library/user-event` for simulating user interactions

**Coverage Reports**:
- Generated in `coverage/` directory (excluded from git)
- HTML reports available at `coverage/index.html`

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

**Style a component with Tailwind**:
```jsx
// Use utility classes directly in JSX
<div className="bg-white p-8 rounded-xl shadow-md">
  <h2 className="text-2xl font-bold text-gray-800 mb-4">Title</h2>
  <button className="px-6 py-3 bg-gradient-to-br from-blue-600 to-blue-700
                     text-white rounded-lg hover:-translate-y-0.5 transition-all">
    Click Me
  </button>
</div>
```

**Add custom Sass utility**:
1. Add styles to appropriate partial in `src/styles/`
2. Use `@layer components` for component classes
3. Use `@layer utilities` for utility classes
4. Import automatically compiled via `main.scss`

**Preserve dynamic inline styles**:
```jsx
// For database-driven colors or calculated values
<span
  className="tag-pill"
  style={{ backgroundColor: tag.color }}
>
  {tag.name}
</span>
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
- [Tailwind CSS Documentation](https://tailwindcss.com/docs)
- [Sass Documentation](https://sass-lang.com/documentation/)
- [Vitest Documentation](https://vitest.dev/)
- [React Testing Library](https://testing-library.com/react)
