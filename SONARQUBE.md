# SonarQube Integration

This project is configured to use [SonarCloud](https://sonarcloud.io) for continuous code quality and security analysis.

## Setup

### Prerequisites
- SonarCloud account (free for open source projects)
- SONAR_TOKEN configured in your environment or CI/CD

### Local Analysis

#### Backend Analysis (Maven)

1. Set your SonarQube token as an environment variable:
```bash
export SONAR_TOKEN=your_sonarcloud_token_here
```

2. Run tests with coverage:
```bash
cd backend
mvn clean verify
```

3. Run SonarQube analysis:
```bash
mvn sonar:sonar
```

The analysis will be uploaded to: https://sonarcloud.io/dashboard?id=axeljanssen_anki-simple

#### Frontend Analysis

Frontend analysis is included in the backend Maven scan. The frontend tests generate coverage reports that are picked up by SonarQube.

To generate frontend coverage locally:
```bash
cd frontend
npm run test:coverage
```

## GitHub Actions Integration

The project includes a GitHub Actions workflow (`.github/workflows/sonarqube.yml`) that automatically runs SonarQube analysis on:
- Push to `main` or `develop` branches
- Pull requests

### Setup GitHub Actions

1. Go to [SonarCloud](https://sonarcloud.io)
2. Sign in with your GitHub account
3. Import your repository
4. Generate a token
5. Add the token to your GitHub repository secrets:
   - Go to Settings → Secrets and variables → Actions
   - Create a new secret named `SONAR_TOKEN`
   - Paste your SonarCloud token

The workflow will automatically run on every push and PR.

## Configuration Files

### Backend (pom.xml)
The Maven POM includes:
- **sonar-maven-plugin**: SonarQube scanner for Maven
- **jacoco-maven-plugin**: Java code coverage generation

### Root (sonar-project.properties)
Project-level configuration including:
- Project identification
- Source and test directories
- Coverage report paths
- Exclusions

### GitHub Actions (.github/workflows/sonarqube.yml)
CI/CD pipeline that:
1. Checks out code
2. Sets up Java 21 and Node.js 20
3. Builds and tests backend with coverage
4. Runs frontend tests with coverage
5. Executes SonarQube analysis

## Metrics Tracked

- **Code Coverage**: Backend (JaCoCo) and Frontend (Vitest)
- **Code Smells**: Maintainability issues
- **Bugs**: Potential runtime errors
- **Vulnerabilities**: Security issues
- **Security Hotspots**: Security-sensitive code
- **Duplications**: Code duplication percentage
- **Technical Debt**: Time to fix all issues

## Quality Gates

SonarCloud automatically enforces quality gates. By default:
- Coverage > 80%
- Duplicated Lines < 3%
- Maintainability Rating ≥ A
- Reliability Rating ≥ A
- Security Rating ≥ A

## Viewing Results

### SonarCloud Dashboard
Visit: https://sonarcloud.io/dashboard?id=axeljanssen_anki-simple

### GitHub PR Checks
SonarQube analysis results appear as checks on pull requests, showing:
- Quality Gate status
- New issues introduced
- Coverage changes

## Badges

Add these badges to your README.md:

```markdown
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=axeljanssen_anki-simple&metric=alert_status)](https://sonarcloud.io/dashboard?id=axeljanssen_anki-simple)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=axeljanssen_anki-simple&metric=coverage)](https://sonarcloud.io/dashboard?id=axeljanssen_anki-simple)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=axeljanssen_anki-simple&metric=bugs)](https://sonarcloud.io/dashboard?id=axeljanssen_anki-simple)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=axeljanssen_anki-simple&metric=security_rating)](https://sonarcloud.io/dashboard?id=axeljanssen_anki-simple)
```

## Troubleshooting

### "Not authorized" error
- Verify your SONAR_TOKEN is correct
- Check that the token has appropriate permissions

### Coverage not showing
- Ensure tests are running successfully
- Verify coverage report paths in sonar-project.properties
- Check that coverage files exist after running tests

### Frontend not analyzed
- Ensure frontend tests generate coverage reports
- Verify `frontend/coverage/lcov.info` exists after running `npm run test:coverage`

## Resources

- [SonarCloud Documentation](https://docs.sonarcloud.io/)
- [SonarQube Maven Plugin](https://docs.sonarqube.org/latest/analysis/scan/sonarscanner-for-maven/)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
