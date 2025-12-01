# SonarQube Integration

This project is configured to use [SonarCloud](https://sonarcloud.io) for continuous code quality and security analysis.

## Setup

### Prerequisites
- SonarCloud account (free for open source projects)
- SONAR_TOKEN configured in your environment or CI/CD
- Node.js 20+ (for frontend)
- Java 21+ (for backend)

### Local Analysis

This is a multi-module project with both backend (Java) and frontend (TypeScript/React) code. Both are analyzed together in a single SonarQube scan.

#### Full Project Analysis

1. Set your SonarQube token as an environment variable:
```bash
export SONAR_TOKEN=your_sonarcloud_token_here
```

2. Run backend tests with coverage:
```bash
cd backend
mvn clean verify
cd ..
```

3. Run frontend tests with coverage:
```bash
cd frontend
npm run test:coverage
cd ..
```

4. Run SonarQube analysis from project root:
```bash
# Install SonarScanner CLI if not already installed
# On macOS: brew install sonar-scanner
# On Linux: Download from https://docs.sonarqube.org/latest/analysis/scan/sonarscanner/

sonar-scanner \
  -Dsonar.projectKey=axeljanssen_anki-simple \
  -Dsonar.organization=axeljanssen \
  -Dsonar.sources=backend/src/main/java,frontend/src \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.token=$SONAR_TOKEN
```

The analysis will be uploaded to: https://sonarcloud.io/dashboard?id=axeljanssen_anki-simple

#### Backend-Only Analysis (Maven)

If you only want to analyze the backend:
```bash
cd backend
mvn clean verify sonar:sonar
```

#### Frontend-Only Coverage

To generate and view frontend coverage locally:
```bash
cd frontend
npm run test:coverage
# Open coverage/index.html in browser
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

### Backend (Java/Spring Boot)
- **Code Coverage**: JaCoCo reports (target: 81%)
- **Test Count**: 80 tests across all domains
- **Code Smells**: Maintainability issues
- **Bugs**: Potential runtime errors
- **Vulnerabilities**: Security issues
- **Security Hotspots**: Security-sensitive code
- **Duplications**: Code duplication percentage

### Frontend (TypeScript/React)
- **Code Coverage**: Vitest with lcov reports (92.79%)
- **Test Count**: 40 tests with React Testing Library
- **TypeScript**: Full type safety with strict mode
- **Code Smells**: Maintainability issues
- **Bugs**: Potential runtime errors
- **Vulnerabilities**: Security issues (including npm dependencies)
- **Security Hotspots**: Security-sensitive code

### Overall
- **Technical Debt**: Combined time to fix all issues
- **Maintainability Rating**: Overall project maintainability
- **Reliability Rating**: Overall project reliability
- **Security Rating**: Overall project security

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

## Setting Up the SonarCloud Project

### First-Time Setup

1. **Sign in to SonarCloud**
   - Go to [https://sonarcloud.io](https://sonarcloud.io)
   - Click "Log in" and choose "GitHub"
   - Authorize SonarCloud to access your GitHub account

2. **Import Your Repository**
   - Click the "+" icon in the top right
   - Select "Analyze new project"
   - Find and select `axeljanssen/anki-simple` from your repositories
   - Click "Set Up"

3. **Configure Analysis Method**
   - Choose "With GitHub Actions" (recommended)
   - SonarCloud will show you the project key and organization
   - Verify they match the values in `sonar-project.properties`:
     - Project Key: `axeljanssen_anki-simple`
     - Organization: `axeljanssen`

4. **Generate Token**
   - In SonarCloud, go to "My Account" → "Security"
   - Click "Generate Tokens"
   - Name: `anki-simple-github-actions`
   - Type: "Global Analysis Token" or "Project Analysis Token"
   - Expiration: Choose appropriate duration
   - Click "Generate" and copy the token immediately

5. **Add Token to GitHub Secrets**
   - Go to your GitHub repository: `https://github.com/axeljanssen/anki-simple`
   - Click "Settings" → "Secrets and variables" → "Actions"
   - Click "New repository secret"
   - Name: `SONAR_TOKEN`
   - Value: Paste the token from step 4
   - Click "Add secret"

6. **Trigger First Analysis**
   - The GitHub Actions workflow will run automatically on:
     - Push to `main` or `develop` branches
     - Pull requests
   - To trigger manually: Make any commit and push to `main`
   - Or go to "Actions" tab and manually trigger the workflow

7. **View Results**
   - Go to [https://sonarcloud.io/dashboard?id=axeljanssen_anki-simple](https://sonarcloud.io/dashboard?id=axeljanssen_anki-simple)
   - Wait for the analysis to complete (usually 2-5 minutes)
   - Review code quality metrics, coverage, and issues

### Project Configuration

The project is configured as a multi-module project analyzing both:
- **Backend**: Java/Spring Boot code with JaCoCo coverage (81%)
- **Frontend**: TypeScript/React code with Vitest coverage (92.79%)

All configuration is in:
- `sonar-project.properties` - SonarQube project settings
- `.github/workflows/sonarqube.yml` - CI/CD automation
- `backend/pom.xml` - Maven plugins for backend analysis

## Resources

- [SonarCloud Documentation](https://docs.sonarcloud.io/)
- [SonarCloud Multi-Module Analysis](https://docs.sonarcloud.io/advanced-setup/languages/javascript/)
- [SonarQube Maven Plugin](https://docs.sonarqube.org/latest/analysis/scan/sonarscanner-for-maven/)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [Vitest Coverage](https://vitest.dev/guide/coverage.html)
