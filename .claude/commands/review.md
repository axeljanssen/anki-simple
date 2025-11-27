---
description: Comprehensive code review of current changes
---

Perform a thorough code review of the current changes. Analyze:

## Code Quality
- Follow project conventions and best practices
- Check naming conventions (camelCase, PascalCase, etc.)
- Identify code duplication and suggest refactoring
- Look for overly complex logic that could be simplified
- Verify proper error handling

## Security
- **Backend**: SQL injection, XSS, CSRF vulnerabilities
- **Authentication**: JWT token handling, password storage
- **Authorization**: Proper permission checks
- **Input validation**: Sanitize user inputs
- **Secrets**: No hardcoded credentials or API keys
- **CORS**: Proper configuration
- **OWASP Top 10**: Check for common vulnerabilities

## Architecture & Design
- Follow Spring Boot layered architecture (Controller → Service → Repository)
- React component design (single responsibility)
- Proper separation of concerns
- RESTful API design principles
- Database schema design

## Testing
- Identify missing test coverage
- Check test quality (unit, integration, edge cases)
- Verify tests follow project patterns (database tests vs mocks)
- Suggest additional test scenarios

## Performance
- Database query optimization (N+1 problems, missing indexes)
- React rendering optimization (unnecessary re-renders)
- Bundle size considerations
- Caching opportunities

## Documentation
- Update CLAUDE.md files if architecture changed
- Add/update code comments where needed
- API endpoint documentation

After analysis, provide:
1. **Summary**: Overall code quality assessment
2. **Critical Issues**: Must-fix items (security, bugs)
3. **Improvements**: Suggested enhancements
4. **Action Items**: Prioritized list of changes

Run tests if applicable and report results.
