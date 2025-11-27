---
description: Security-focused audit of code and configuration
---

Perform a comprehensive security audit of the codebase focusing on vulnerabilities and security best practices.

## Authentication & Authorization
- **JWT Implementation**: Token generation, validation, expiration
- **Password Security**: BCrypt hashing, password requirements
- **Session Management**: Token storage, refresh mechanisms
- **Protected Routes**: Verify all sensitive endpoints require auth
- **Authorization Checks**: Ensure users can only access their own data

## OWASP Top 10 Vulnerabilities

### 1. Injection (SQL, NoSQL, Command)
- Check for SQL injection in custom queries
- Verify parameterized queries/JPA usage
- Review any dynamic query construction

### 2. Broken Authentication
- Token expiration settings (not too long)
- Secure token storage
- Password strength requirements
- Account lockout mechanisms

### 3. Sensitive Data Exposure
- No secrets in code or version control
- HTTPS enforcement
- Secure headers (X-Frame-Options, etc.)
- Sensitive data not logged

### 4. XML External Entities (XXE)
- Check XML parsing if applicable

### 5. Broken Access Control
- User can only modify their own data
- No privilege escalation possible
- Admin routes properly protected

### 6. Security Misconfiguration
- CORS configuration review
- Error messages don't leak info
- Default credentials changed
- Unnecessary features disabled

### 7. Cross-Site Scripting (XSS)
- Frontend input sanitization
- Proper HTML escaping
- Content Security Policy headers

### 8. Insecure Deserialization
- Check serialization libraries

### 9. Using Components with Known Vulnerabilities
- Review `package.json` and `pom.xml` dependencies
- Check for outdated libraries
- Suggest `npm audit` / `mvn dependency:check`

### 10. Insufficient Logging & Monitoring
- Security events logged
- Sensitive data not logged
- Log integrity maintained

## Backend Security (Spring Boot)
- `application.properties`: No hardcoded secrets
- JWT secret strength and storage
- CORS origins properly configured
- Security headers configured
- Database credentials secure
- Flyway migrations reviewed

## Frontend Security (React)
- localStorage security considerations
- API token exposure
- XSS prevention in JSX
- Dependency vulnerabilities
- Environment variable handling

## Configuration Files Review
- `.env` files not in git
- `application.properties` secrets
- Database connection strings
- API keys and tokens

Provide:
1. **Critical Vulnerabilities**: Immediate security risks
2. **Security Weaknesses**: Areas needing improvement
3. **Best Practice Violations**: Security hygiene issues
4. **Compliance**: Notes on standards (OWASP, GDPR, etc.)
5. **Recommendations**: Prioritized security improvements

Rate overall security posture (Critical/High/Medium/Low risk).
