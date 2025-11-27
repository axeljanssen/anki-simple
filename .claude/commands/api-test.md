---
description: Generate comprehensive tests for API endpoints
---

Generate comprehensive integration tests for the specified API endpoint or the endpoint in the current context.

## Test Generation Guidelines

### Backend Integration Tests (Spring Boot)
Follow the project's testing strategy (prefer database over mocks):

```java
@SpringBootTest
@Transactional
class EndpointIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Repository repository;

    // Test cases here
}
```

### Test Coverage Requirements

**1. Success Cases**
- Valid input returns correct response
- Status code verification (200, 201, etc.)
- Response body structure and content
- Relationships properly loaded

**2. Validation Tests**
- Missing required fields (400 Bad Request)
- Invalid field formats
- Constraint violations
- Boundary value testing

**3. Authentication & Authorization**
- Endpoint requires valid JWT token (401 if missing)
- Token expiration handling
- User can only access their own data (403 if unauthorized)

**4. Error Cases**
- Resource not found (404)
- Duplicate resources (409 if applicable)
- Invalid IDs or references
- Database constraint violations

**5. Edge Cases**
- Empty collections
- Maximum field lengths
- Special characters in inputs
- Null handling

**6. Business Logic**
- Spaced repetition calculations (for review endpoints)
- Card scheduling updates
- Related entity updates (cascades, etc.)

### Frontend API Tests (if applicable)
Generate tests using Vitest and axios mocking:

```javascript
import { describe, it, expect, vi } from 'vitest'
import { apiService } from '../services/api'

describe('API Service', () => {
  it('should handle successful response', async () => {
    // Test implementation
  })
})
```

## Test Structure

For each endpoint, generate tests for:

**GET Endpoints:**
- Retrieve existing resource (200)
- Resource not found (404)
- Unauthorized access (401/403)
- Query parameter handling
- Pagination (if applicable)

**POST Endpoints:**
- Create with valid data (201)
- Missing required fields (400)
- Duplicate resource (409 if applicable)
- Unauthorized (401)
- Verify resource created in database

**PUT Endpoints:**
- Update existing resource (200)
- Resource not found (404)
- Invalid data (400)
- Unauthorized update (401/403)
- Verify changes persisted

**DELETE Endpoints:**
- Delete existing resource (204)
- Resource not found (404)
- Unauthorized delete (401/403)
- Verify resource removed
- Cascade effects tested

## Instructions

If endpoint is specified, generate tests for it.
If no endpoint specified, analyze the current file/context:
- If in a Controller, generate tests for its endpoints
- If in a Service, generate tests for its methods
- If in existing test file, identify gaps and add missing tests

Follow the project's testing patterns:
- Use database integration tests for backend
- Use Vitest + Testing Library for frontend
- Follow existing naming conventions
- Include descriptive test names
- Add helpful comments for complex assertions

After generating tests:
1. Run the tests to verify they pass
2. Report coverage improvements
3. Suggest additional test scenarios if needed
