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

- **TypeScript 5.7+** - Statically typed JavaScript with strict mode
- **React 19** - UI library
- **Vite 7** - Build tool and dev server
- **React Router 6** - Client-side routing
- **Axios** - HTTP client for API calls with full TypeScript support
- **Context API** - State management for authentication
- **Tailwind CSS 3.4.0** - Utility-first CSS framework
- **Sass 1.94.0** - CSS preprocessor for custom styles
- **PostCSS & Autoprefixer** - CSS transformation tools
- **Vitest 4** - Unit testing framework
- **React Testing Library** - Component testing utilities

## Project Structure

```
src/
├── assets/           # Static assets (images, etc.)
├── components/       # Reusable UI components
│   ├── ProtectedRoute.tsx  # Route authentication wrapper
│   ├── VocabularyList.tsx  # Card grid display
│   ├── VocabularyForm.tsx  # Card create/edit form
│   └── VocabularyTable.tsx # Card table display
├── context/          # React Context providers
│   └── AuthContext.tsx    # Authentication state management
├── pages/            # Page components
│   ├── Login.tsx           # Login page
│   ├── Signup.tsx          # Registration page
│   ├── Dashboard.tsx       # Main dashboard (grid view)
│   ├── VocabularyTablePage.tsx  # Table view page
│   └── Review.tsx          # Spaced repetition review
├── services/         # API service layer
│   └── api.ts       # Centralized API calls with TypeScript types
├── styles/           # Sass stylesheets
│   ├── main.scss         # Main entry (Tailwind directives)
│   ├── _base.scss        # Global base styles
│   ├── _components.scss  # Custom component utilities
│   └── _animations.scss  # Keyframe animations
├── test/             # Test utilities
│   └── setup.ts     # Vitest configuration
├── types/            # TypeScript type definitions
│   ├── index.ts          # Re-export all types
│   ├── models.ts         # Data models (User, Card, Tag)
│   ├── api.ts            # API request/response types
│   ├── context.ts        # Context types
│   └── components.ts     # Component prop types
├── utils/            # Utility functions
├── App.tsx           # Main application component
└── main.tsx          # Application entry point
```

## Architecture

### TypeScript Type System

The application uses TypeScript with **strict mode** enabled for maximum type safety. All types are centralized in the `src/types/` directory.

**Type Configuration** (`tsconfig.json`):
- Strict mode: All strict type-checking options enabled
- Path aliases: `@/*` resolves to `./src/*` for cleaner imports
- No unchecked indexed access: Array access returns `T | undefined`
- Modern ES2022 target with DOM libraries

**Core Type Definitions**:

```typescript
// src/types/models.ts
export interface User {
  username: string
  email: string
  token: string
}

export interface VocabularyCard {
  id: number
  front: string
  back: string
  exampleSentence: string | null
  languageSelection: string  // LanguageSelection enum from backend (e.g., 'EN_ES', 'DE_FR')
  tags: Tag[]
  easeFactor: number
  intervalDays: number
  nextReview: string | null
  // ... other fields
}

export interface Tag {
  id: number
  name: string
  color: string
}
```

**Language Selection**:
- The `languageSelection` field corresponds to backend's `LanguageSelection` enum
- Supports 10 bidirectional language pairs: DE_FR, DE_ES, EN_ES, EN_FR, EN_DE, FR_ES, EN_IT, DE_IT, FR_IT, ES_IT
- Each value has a display name (e.g., "English ⇄ Spanish" for EN_ES)
- Used in vocabulary forms as dropdown selection

**API Types** (`src/types/api.ts`):
```typescript
export interface AuthResponse {
  token: string
  username: string
  email: string
}
```

**Context Types** (`src/types/context.ts`):
```typescript
export interface AuthContextValue {
  user: User | null
  loading: boolean
  login: (credentials: LoginCredentials) => Promise<AuthResult>
  signup: (userData: SignupData) => Promise<AuthResult>
  logout: () => void
}
```

**Component Props** (`src/types/components.ts`):
```typescript
export interface VocabularyListProps {
  cards: VocabularyCard[]
  onEdit: (card: VocabularyCard) => void
  onDelete: (id: number) => void
}
```

**Path Aliases**:
All imports use the `@/` prefix for cleaner code:
```typescript
import { User, VocabularyCard } from '@/types'
import { authAPI } from '@/services/api'
import { useAuth } from '@/context/AuthContext'
```

### Routing Structure

The application uses React Router with protected routes:

```tsx
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

**AuthContext** (`context/AuthContext.tsx`):
- Provides authentication state and methods throughout the app
- Stores JWT token in localStorage
- Manages user login/logout state
- Provides `authAPI` methods: `login()`, `signup()`, `logout()`

**Axios Interceptor**:
- Automatically adds `Authorization: Bearer <token>` header to all API requests
- Configured in `services/api.ts` with full TypeScript types

**Authentication Process**:
1. User submits login/signup form
2. Frontend calls `/api/auth/login` or `/api/auth/signup`
3. Backend returns JWT token in response
4. Token stored in localStorage
5. AuthContext updates with user data
6. All subsequent API calls include token in header
7. Protected routes become accessible

### API Layer

**Services** (`services/api.ts`):
Centralizes all API calls organized by domain with full TypeScript typing:

```typescript
import axios, { AxiosInstance, InternalAxiosRequestConfig } from 'axios'
import { AuthResponse, VocabularyCard, Tag, LoginCredentials, SignupData, VocabularyFormData, ReviewSubmission } from '@/types'

// Base configuration
const API_BASE_URL = 'http://localhost:8080/api'

const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

// Typed request interceptor
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  }
)

// API modules with full type safety
export const authAPI = {
  login: (data: LoginCredentials) => api.post<AuthResponse>('/auth/login', data),
  signup: (data: SignupData) => api.post<AuthResponse>('/auth/signup', data),
}

export const vocabularyAPI = {
  getAll: () => api.get<VocabularyCard[]>('/vocabulary'),
  create: (data: VocabularyFormData) => api.post<VocabularyCard>('/vocabulary', data),
  update: (id: number, data: VocabularyFormData) => api.put<VocabularyCard>(`/vocabulary/${id}`, data),
  delete: (id: number) => api.delete<void>(`/vocabulary/${id}`),
  getDue: () => api.get<VocabularyCard[]>('/vocabulary/due'),
  getDueCount: () => api.get<number>('/vocabulary/due/count'),
}

export const reviewAPI = {
  review: (data: ReviewSubmission) => api.post<void>('/review', data),
}

export const tagAPI = {
  getAll: () => api.get<Tag[]>('/tags'),
  create: (data: { name: string; color: string }) => api.post<Tag>('/tags', data),
  delete: (id: number) => api.delete<void>(`/tags/${id}`),
}
```

**TypeScript Benefits**:
- Full type safety for all API requests and responses
- IntelliSense autocomplete for API methods and data structures
- Compile-time validation of request/response shapes
- Generic types ensure response data matches expected types

**Performance Optimization**:
- `vocabularyAPI.getAll()` returns lean card data (id, front, back, languageSelection only)
- Backend uses `VocabularyCardLeanResponse` DTO to reduce payload size
- Full card details (tags, SM-2 data, timestamps) fetched individually when editing
- Significantly improves list loading performance with large card collections

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
  - Language selection dropdown with 10 bidirectional language pairs
  - Display names shown (e.g., "English ⇄ Spanish", "German ⇄ French")
  - Enum values (EN_ES, DE_FR, etc.) sent to backend

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
- `api.test.ts` - 12 tests for API service layer with TypeScript types
- `VocabularyList.test.tsx` - 10 tests for card list component
- `AuthContext.test.tsx` - 10 tests for authentication context
- `Login.test.tsx` - 5 tests for login page
- `ProtectedRoute.test.tsx` - 3 tests for route protection

**Running Tests**:
```bash
npm test              # Run all tests
npm run test:ui       # Run tests with UI
npm run test:coverage # Generate coverage report
```

**Test Utilities**:
- `src/test/setup.ts` - Vitest configuration with TypeScript
- Mock implementations for React Router and AuthContext with proper typing
- `@testing-library/user-event` for simulating user interactions
- Type-safe mock data using domain types from `@/types`

**Coverage Reports**:
- Generated in `coverage/` directory (excluded from git)
- HTML reports available at `coverage/index.html`

### SonarCloud Integration

Frontend test coverage is automatically uploaded to SonarCloud via GitHub Actions:

- LCOV reports generated by `npm run test:coverage`
- Coverage data: `frontend/coverage/lcov.info`
- Integrated with backend coverage in unified SonarCloud dashboard
- Test files excluded from coverage metrics (see `sonar-project.properties`)

**Exclusions**:
- `**/*.test.ts`, `**/*.test.tsx` - Test files
- `**/*.config.js`, `**/*.config.ts` - Configuration files
- `**/main.tsx` - Entry point
- `**/vite-env.d.ts` - Type definitions

## Development

### Running Locally

1. Ensure backend is running on `http://localhost:8080`
2. Install dependencies: `npm install`
3. Start dev server: `npm run dev`
4. Open browser: `http://localhost:5173`

### Environment Configuration

Update API base URL in `services/api.ts` if backend runs on different port:
```typescript
const API_BASE_URL = 'http://localhost:8080/api'
```

### Code Style

**TypeScript Patterns**:
- Use functional components with TypeScript and hooks
- Define interfaces for all component props
- Type all useState hooks with explicit types
- Use path aliases (`@/`) for imports
- Leverage type inference where appropriate

**Component Structure**:
```typescript
import React, { useState, FormEvent } from 'react'
import { ComponentProps } from '@/types'

const Component = ({ prop1, prop2 }: ComponentProps): React.JSX.Element => {
  const [state, setState] = useState<Type>(initialValue)

  const handleSubmit = (e: FormEvent<HTMLFormElement>): void => {
    e.preventDefault()
    // handler logic
  }

  return <div>...</div>
}

export default Component
```

**General Principles**:
- Keep components focused and small
- Extract reusable logic into custom hooks
- Type all function parameters and return values
- Use destructuring for props with type annotations
- Handle null/undefined cases explicitly (strict mode)

### Common Development Tasks

**Add new API endpoint**:
1. Define types in `src/types/api.ts` or `src/types/models.ts`
2. Add typed method to appropriate API module in `services/api.ts`
3. Use axios with generic type parameter for response

```typescript
// In src/types/models.ts
export interface NewEntity {
  id: number
  name: string
}

// In services/api.ts
export const newAPI = {
  getAll: () => api.get<NewEntity[]>('/new-entities'),
  create: (data: Omit<NewEntity, 'id'>) => api.post<NewEntity>('/new-entities', data),
}
```

**Create new page**:
1. Create typed component in `pages/` directory (`.tsx` extension)
2. Define prop types if needed in `src/types/components.ts`
3. Add route in `App.tsx`
4. Wrap with `ProtectedRoute` if authentication required

```typescript
// pages/NewPage.tsx
import React from 'react'
import { useAuth } from '@/context/AuthContext'

const NewPage = (): React.JSX.Element => {
  const { user } = useAuth()
  return <div>New Page for {user?.username}</div>
}

export default NewPage
```

**Add new protected route**:
```tsx
<Route
  path="/new-page"
  element={<ProtectedRoute><NewPage /></ProtectedRoute>}
/>
```

**Access authentication state**:
```typescript
import { useAuth } from '@/context/AuthContext'

const MyComponent = (): React.JSX.Element => {
  const { user, logout } = useAuth() // Fully typed via AuthContextValue
  // user is typed as User | null

  return (
    <div>
      {user && <p>Hello, {user.username}</p>}
      <button onClick={logout}>Logout</button>
    </div>
  )
}
```

**Create typed component with props**:
```typescript
// Define props interface in src/types/components.ts
export interface MyComponentProps {
  title: string
  count: number
  onUpdate: (newCount: number) => void
}

// Component file
import React, { useState } from 'react'
import { MyComponentProps } from '@/types'

const MyComponent = ({ title, count, onUpdate }: MyComponentProps): React.JSX.Element => {
  const [localCount, setLocalCount] = useState<number>(count)

  const handleClick = (): void => {
    const newCount = localCount + 1
    setLocalCount(newCount)
    onUpdate(newCount)
  }

  return (
    <div>
      <h2>{title}</h2>
      <button onClick={handleClick}>Count: {localCount}</button>
    </div>
  )
}

export default MyComponent
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

**TypeScript Error Handling**:
```typescript
import { AxiosError } from 'axios'
import { vocabularyAPI } from '@/services/api'
import { ProblemDetails } from '@/types'

try {
  const response = await vocabularyAPI.getAll()
  const cards = response.data // Typed as VocabularyCard[]
  // Handle success
} catch (error) {
  const axiosError = error as AxiosError<ProblemDetails>

  if (axiosError.response) {
    // Server responded with error status
    console.error(axiosError.response.data.detail)
    console.error(`Status: ${axiosError.response.data.status}`)
  } else if (axiosError.request) {
    // Request made but no response
    console.error('No response from server')
  } else {
    // Request setup error
    console.error('Request error:', axiosError.message)
  }
}
```

**Type-Safe Async State**:
```typescript
const [cards, setCards] = useState<VocabularyCard[]>([])
const [loading, setLoading] = useState<boolean>(false)
const [error, setError] = useState<string | null>(null)

const loadCards = async (): Promise<void> => {
  setLoading(true)
  setError(null)

  try {
    const response = await vocabularyAPI.getAll()
    setCards(response.data)
  } catch (error) {
    const axiosError = error as AxiosError<{ message?: string }>
    setError(axiosError.response?.data?.message || 'Failed to load cards')
  } finally {
    setLoading(false)
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
- TypeScript compiled to optimized JavaScript (ES2022)
- CSS extracted and minified (18.20 kB → 4.18 kB gzipped)
- JavaScript bundled and minified (277.50 kB → 88.07 kB gzipped)
- Assets copied and hashed for cache busting
- Full type checking performed during build

**Deployment**:
- Serve `dist/` directory as static files
- Configure web server to redirect all routes to `index.html` (for client-side routing)
- Update `API_BASE_URL` in `api.ts` to production backend URL
- TypeScript types are removed during compilation (zero runtime overhead)

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
- Verify API_BASE_URL in `services/api.ts`
- Check TypeScript types match backend API responses

**TypeScript errors**:
- Run type check: `npx tsc --noEmit`
- Check `tsconfig.json` for proper configuration
- Ensure all imports use correct paths (use `@/` aliases)
- Verify type definitions in `src/types/` match actual data

**Build errors**:
- Clear node_modules and reinstall: `rm -rf node_modules && npm install`
- Check Node.js version (requires Node 18+)
- Run TypeScript compiler directly: `npx tsc --noEmit`
- Review console for specific error messages
- Ensure all `.tsx` files have proper JSX syntax

## Additional Resources

- [TypeScript Documentation](https://www.typescriptlang.org/docs/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/handbook/intro.html)
- [React with TypeScript](https://react.dev/learn/typescript)
- [React Documentation](https://react.dev/)
- [Vite Documentation](https://vitejs.dev/)
- [React Router Documentation](https://reactrouter.com/)
- [Axios Documentation](https://axios-http.com/)
- [Tailwind CSS Documentation](https://tailwindcss.com/docs)
- [Sass Documentation](https://sass-lang.com/documentation/)
- [Vitest Documentation](https://vitest.dev/)
- [React Testing Library](https://testing-library.com/react)
