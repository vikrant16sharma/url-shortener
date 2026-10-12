# URL Shortener

A backend-focused URL Shortener built with **Java 21, Spring Boot,
Spring Security, JWT, Spring Data JPA, and PostgreSQL**. The project is
being developed incrementally with an emphasis on clean layering,
authentication, ownership checks, analytics, automated tests, and
production-readiness.

**Repository:**
[vikrant16sharma/url-shortener](https://github.com/vikrant16sharma/url-shortener)\
**Default branch:** `main`\
**Current focus:** Phase 8 --- Rate Limiting (design and algorithm
selection; implementation has not started yet).

------------------------------------------------------------------------

## Table of Contents

-   [Project Goals](#project-goals)
-   [Technology Stack](#technology-stack)
-   [Current Status](#current-status)
-   [Features Implemented](#features-implemented)
-   [API Overview](#api-overview)
-   [Architecture](#architecture)
-   [Current Directory Structure](#current-directory-structure)
-   [Configuration](#configuration)
-   [Run Locally](#run-locally)
-   [Testing](#testing)
-   [Roadmap](#roadmap)
-   [Engineering Notes](#engineering-notes)
-   [Git Workflow](#git-workflow)

------------------------------------------------------------------------

## Project Goals

The project is intended to demonstrate practical backend engineering
concepts:

-   REST API design and HTTP status-code semantics.
-   Short-code generation and URL redirection.
-   Layered architecture and constructor-based dependency injection.
-   Relational data modeling with PostgreSQL and JPA.
-   JWT-based authentication and endpoint authorization.
-   Refresh-token rotation and logout.
-   URL ownership enforcement.
-   Click analytics and query optimization.
-   Automated unit, repository, controller, and security tests.
-   Incremental production hardening.

The current scope is the backend. A frontend is not part of the current
implementation.

## Technology Stack

Area               Technology
  ------------------ ----------------------------------------------
Language           Java 21
Framework          Spring Boot 4.1.1
Build              Maven
Web                Spring MVC / Spring Web MVC
Security           Spring Security
Authentication     JWT using JJWT
Persistence        Spring Data JPA, Hibernate
Database           PostgreSQL 18.x
Validation         Jakarta Bean Validation
Password hashing   BCrypt via Spring Security `PasswordEncoder`
Testing            JUnit, Mockito, Spring Boot Test, MockMvc
Version control    Git and GitHub

**Planned, not yet implemented:** Redis, OpenAPI/Swagger documentation,
Docker, CI/CD, deployment, and monitoring.

------------------------------------------------------------------------

## Current Status

Last project checkpoint reported: **64 tests passing, 0 failures, 0
errors**. Re-run the test suite on the current checkout to verify the
latest state.

  -----------------------------------------------------------------------
Area                    Status                  Notes
  ----------------------- ----------------------- -----------------------
Core URL shortening     Complete                Generate and persist
short codes

Redirect endpoint       Complete                Redirects with HTTP 302

Request validation      Complete                URL format and
expiration validation

Global exception        Complete                Consistent handling for
handling                                        application errors

PostgreSQL persistence  Complete                JPA entities and
repositories

URL expiration          Complete                Expired links return
HTTP 410

Click analytics         Complete                Click events and
analytics endpoint

Database query          Complete                Composite index for URL
optimization                                    click timestamps

User registration       Complete                Email validation and
BCrypt password hashing

Login and JWT access    Complete                Token generation and
tokens                                          validation

Refresh tokens          Complete                Persistence, rotation,
expiry, and revocation

Logout                  Complete                Refresh-token
revocation

Authorization and       Complete                Analytics access
ownership                                       restricted to the URL
owner

Automated tests         Green at last           64 passing tests were
checkpoint              reported

Rate limiting           **Current task**        Design first; no
limiter implementation
yet

Redis caching           Planned                 Not implemented

OpenAPI / Swagger       Planned                 Not implemented

Docker / CI/CD /        Planned                 Not implemented
deployment
  -----------------------------------------------------------------------

------------------------------------------------------------------------

## Features Implemented

### URL shortening and redirects

-   Accepts a long URL and creates a short URL.
-   Generates short codes using Base62 encoding.
-   Stores URL records in PostgreSQL.
-   Redirects short URLs to their original destination.
-   Returns HTTP `404 Not Found` for unknown short codes.
-   Validates submitted URLs.
-   Supports optional expiration timestamps.
-   Returns HTTP `410 Gone` for expired short URLs.

### Authentication and authorization

-   User registration and login endpoints.
-   Password hashing with BCrypt.
-   JWT access-token generation and validation.
-   Stateless Spring Security configuration.
-   Refresh-token persistence, rotation, expiration, and revocation.
-   Logout through refresh-token revocation.
-   Public short-link redirects.
-   Authenticated URL creation and analytics.
-   Owner-only access to a URL's analytics.
-   HTTP `401 Unauthorized` and `403 Forbidden` handling.

### Click analytics

-   Records click events when a valid, non-expired short URL is
    redirected.
-   Provides an analytics endpoint with total clicks, first click, and
    most recent click.
-   Uses a composite database index on `(url_id, clicked_at)` to support
    analytics queries.

### Testing

-   Request DTO validation tests.
-   Password encoder tests.
-   Service and controller tests.
-   Repository tests.
-   Refresh-token tests.
-   Security integration tests for missing, invalid, and valid JWTs.
-   Ownership checks for analytics.

------------------------------------------------------------------------

## API Overview

The following endpoints reflect the current project design and
implementation described in the project checkpoint. Verify against the
controllers in the repository before treating this list as a formal API
contract.

  ------------------------------------------------------------------------------------
Method            Endpoint                       Access            Purpose
  ----------------- ------------------------------ ----------------- -----------------
`POST`            `/api/auth/register`           Public            Register a user

`POST`            `/api/auth/login`              Public            Authenticate and
receive tokens

`POST`            `/api/auth/refresh`            Public; refresh   Rotate refresh
token required    token and issue a
new access token

`POST`            `/api/auth/logout`             Public; refresh   Revoke refresh
token required    token

`POST`            `/api/urls`                    Authenticated     Create or
retrieve the
user's short URL

`GET`             `/{code}`                      Public            Redirect to the
original URL

`GET`             `/api/urls/{code}/analytics`   Authenticated     Retrieve click
owner             analytics
  ------------------------------------------------------------------------------------

### Expected response behavior

Scenario                                                            HTTP status
  ----------------------------------------- -------------------------------------
URL created                                                       `201 Created`
Redirect succeeds                                                   `302 Found`
Invalid request                                               `400 Bad Request`
Missing short code                                              `404 Not Found`
Expired short URL                                                    `410 Gone`
Missing or invalid authentication                            `401 Unauthorized`
Authenticated user does not own the URL                         `403 Forbidden`
Duplicate registered email                                       `409 Conflict`
Rate limit exceeded                         `429 Too Many Requests` --- planned

------------------------------------------------------------------------

## Architecture

The application follows a layered request flow:

``` text
HTTP Client
    |
    v
Spring Security Filter Chain
    |
    v
Controller
    |
    v
DTO validation
    |
    v
Service
    |
    +------> Code Generator
    |
    v
Repository (Spring Data JPA)
    |
    v
PostgreSQL
```

For redirects, the public redirect endpoint looks up the short code,
checks expiration, records a click event, and returns a redirect
response.

### Responsibilities

-   **Controller:** HTTP mapping, request/response handling, and
    validation entry points.
-   **DTO:** Defines API request and response shapes; prevents entities
    from becoming the public API contract.
-   **Service:** Owns business rules and transaction boundaries.
-   **Repository:** Encapsulates persistence queries.
-   **Entity:** Represents persisted domain data.
-   **Security:** Authenticates requests and enforces access rules.
-   **Exception handling:** Maps application exceptions to appropriate
    HTTP responses.
-   **Code generator:** Generates compact short codes.
-   **Click event model:** Stores individual redirect events for
    analytics.

------------------------------------------------------------------------

## Current Directory Structure

The exact repository tree was not available for direct inspection when
this README was prepared. The tree below is a **best-effort structure
based on the classes and packages discussed during implementation**, not
a verified live directory listing. Compare it with the actual repository
and update it if package names or files differ.

``` text
url-shortener/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── vikrant/
│   │   │           └── urlshortener/
│   │   │               ├── UrlshortenerApplication.java
│   │   │               ├── config/
│   │   │               │   ├── AppProperties.java
│   │   │               │   ├── JwtProperties.java
│   │   │               │   ├── JpaAuditingConfig.java
│   │   │               │   └── PasswordConfig.java
│   │   │               ├── controller/
│   │   │               │   ├── AuthController.java
│   │   │               │   └── UrlController.java
│   │   │               ├── dto/
│   │   │               │   ├── auth/
│   │   │               │   └── url/
│   │   │               ├── entity/
│   │   │               │   ├── User.java
│   │   │               │   ├── Url.java
│   │   │               │   ├── ClickEvent.java
│   │   │               │   └── RefreshToken.java
│   │   │               ├── repository/
│   │   │               │   ├── UserRepository.java
│   │   │               │   ├── UrlRepository.java
│   │   │               │   ├── ClickEventRepository.java
│   │   │               │   └── RefreshTokenRepository.java
│   │   │               ├── service/
│   │   │               │   ├── AuthService.java
│   │   │               │   ├── JwtService.java
│   │   │               │   └── RefreshTokenService.java
│   │   │               ├── service/impl/
│   │   │               │   ├── AuthServiceImpl.java
│   │   │               │   ├── JwtServiceImpl.java
│   │   │               │   └── ...
│   │   │               ├── security/
│   │   │               │   ├── SecurityConfig.java
│   │   │               │   ├── JwtAuthenticationFilter.java
│   │   │               │   └── CustomUserDetailsService.java
│   │   │               ├── exception/
│   │   │               │   └── ...
│   │   │               └── ...
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/
│               └── vikrant/
│                   └── urlshortener/
│                       ├── controller/
│                       ├── repository/
│                       ├── security/
│                       ├── service/
│                       └── ...
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

### Verify the actual tree on Windows

From the repository root in PowerShell:

``` powershell
tree /F /A
```

To save the listing to a text file:

``` powershell
tree /F /A > project-tree.txt
```

Review the output and update the tree above to match the actual files.
Do not commit generated build output such as `target/`.

------------------------------------------------------------------------

## Configuration

The application uses environment variables for database credentials and
JWT secrets. Do not commit real credentials, tokens, or `.env` files.

Known configuration keys:

  -------------------------------------------------------------------------------------------------------
Variable                      Purpose                 Example / default
  ----------------------------- ----------------------- -------------------------------------------------
`DB_URL`                      JDBC connection URL     `jdbc:postgresql://localhost:5432/urlshortener`

`DB_USERNAME`                 PostgreSQL username     `postgres` for local development

`DB_PASSWORD`                 PostgreSQL password     Set locally; no default should be committed

`JWT_SECRET`                  Secret used to sign     Supply a strong Base64-encoded secret
JWTs

`JWT_REFRESH_EXPIRATION_MS`   Refresh-token lifetime  `604800000` ms
override

`APP_BASE_URL`                Base URL used to build  `http://localhost:8080/`
short links
  -------------------------------------------------------------------------------------------------------

The application properties discussed during development also use: -
Access-token lifetime: `3600000` milliseconds (1 hour). - Refresh-token
lifetime default: `604800000` milliseconds (7 days). - Hibernate schema
setting: `spring.jpa.hibernate.ddl-auto=update` during development.

**Production note:** use a managed secret store or deployment
environment variables, review schema migrations before production, and
do not enable verbose SQL logging unnecessarily.

### Example local environment setup (PowerShell)

Set values for the current terminal session:

``` powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/urlshortener"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "<your-local-postgres-password>"
$env:JWT_SECRET = "<your-base64-encoded-secret>"
$env:APP_BASE_URL = "http://localhost:8080/"
```

Replace the placeholders locally. Do not paste real secrets into source
control or public issues.

------------------------------------------------------------------------

## Run Locally

### Prerequisites

-   JDK 21
-   PostgreSQL
-   Git
-   Maven, or the included Maven Wrapper

### 1. Clone the repository

``` bash
git clone https://github.com/vikrant16sharma/url-shortener.git
cd url-shortener
```

### 2. Create the database

Create a PostgreSQL database named `urlshortener` using your preferred
PostgreSQL client:

``` sql
CREATE DATABASE urlshortener;
```

Ensure the database username and password match the environment
variables.

### 3. Configure environment variables

Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` as shown
above. Configure `APP_BASE_URL` if your local port or host differs.

### 4. Run tests

On Windows:

``` powershell
.\mvnw.cmd clean test
```

Or, if Maven is installed:

``` powershell
mvn clean test
```

### 5. Start the application

On Windows:

``` powershell
.\mvnw.cmd spring-boot:run
```

Or:

``` powershell
mvn spring-boot:run
```

The local application is expected to run at `http://localhost:8080`
unless configured otherwise.

------------------------------------------------------------------------

## Testing

Run the complete suite before merging a milestone:

``` powershell
.\mvnw.cmd clean test
```

At the latest recorded checkpoint, the suite reported:

``` text
Tests run: 64
Failures: 0
Errors: 0
Skipped: 0
```

This is a historical checkpoint, not a guarantee that every current
checkout will produce the same result. Use the latest test output as the
source of truth.

### Test isolation note

Repository tests previously encountered duplicate-email collisions
because multiple tests used the same fixed email. UUID-based test emails
were introduced to avoid collisions.

Use a **dedicated test database** for integration and repository tests.
Avoid repository-wide `deleteAll()` cleanup against a development
database containing personal or real project data. Tests should clean up
only records they created, or use an isolated disposable test database.

------------------------------------------------------------------------

## Roadmap

### Phase 1 --- Core URL Shortening

-   [x] URL-to-short-code flow
-   [x] Base62 code generation
-   [x] Redirect behavior

### Phase 2 --- Spring Boot API

-   [x] Layered controller/service/repository design
-   [x] DTOs and request validation
-   [x] Global exception handling
-   [x] HTTP response semantics

### Phase 3 --- Persistence

-   [x] PostgreSQL integration
-   [x] JPA entities and repositories
-   [x] URL expiration

### Phase 4 --- Analytics

-   [x] Click event persistence
-   [x] Analytics endpoint
-   [x] Query/index optimization

### Phase 5 --- Authentication and Authorization

-   [x] Registration and BCrypt password hashing
-   [x] Login and JWT access tokens
-   [x] Refresh-token persistence and rotation
-   [x] Logout and token revocation
-   [x] URL ownership enforcement
-   [x] Security integration tests

### Phase 6 --- Test Stabilization

-   [x] Resolve duplicate test-email collisions
-   [x] Reach a reported green checkpoint of 64 tests
-   [ ] Confirm tests use a dedicated test database and isolated cleanup

### Phase 7 --- Rate Limiting (current phase)

-   [x] Identify target endpoints and abuse scenarios
-   [x] Compare fixed-window, sliding-window, and token-bucket
    strategies
-   [ ] Implement a rate-limiting component
-   [ ] Integrate limits with URL creation and authentication endpoints
-   [ ] Return a structured `429 Too Many Requests` response
-   [ ] Test allowed requests, rejected requests, reset/refill behavior,
    and concurrency
-   [ ] Document limitations of in-memory state

### Phase 8 --- Redis Caching and Distributed Limits

-   [ ] Cache frequently accessed short-code lookups
-   [ ] Define cache expiry and invalidation behavior
-   [ ] Evaluate Redis-backed distributed rate limiting
-   [ ] Test behavior across multiple application instances

### Phase 9 --- API Documentation and Hardening

-   [ ] Add OpenAPI/Swagger
-   [ ] Review validation, error responses, and API contracts
-   [ ] Review authentication and token handling
-   [ ] Review database migrations and indexes
-   [ ] Add pagination where appropriate

### Phase 10 --- Packaging and Deployment

-   [ ] Add Dockerfile
-   [ ] Add Docker Compose for local app/database development
-   [ ] Configure production profiles and secrets
-   [ ] Add CI workflow to build and run tests
-   [ ] Deploy the backend
-   [ ] Add health checks, structured logging, and monitoring

### Later Enhancements

-   [ ] Frontend client
-   [ ] Custom aliases
-   [ ] User-level usage quotas
-   [ ] Additional analytics and reporting
-   [ ] Load and performance testing

------------------------------------------------------------------------

## Engineering Notes

-   **Base62 is encoding, not hashing.** Uniqueness still needs to be
    enforced by the database and handled by application logic.
-   **Redirects remain public.** Creating URLs and reading private
    analytics require authentication according to the current security
    design.
-   **Analytics are owner-restricted.** A user should not be able to
    read another user's URL analytics.
-   **Refresh tokens are credentials.** Keep them out of logs and source
    control; store and revoke them securely.
-   **Rate limiting is not implemented yet.** The next development task
    is to implement and test it, starting with a simple in-memory
    algorithm before considering Redis.
-   **In-memory rate limits are per instance.** They are not suitable
    for consistent limits across multiple server replicas.
-   **Schema management needs production review.** `ddl-auto=update` is
    convenient in development; use controlled database migrations for
    production.
-   **Keep commits small.** Commit after a tested milestone, not after
    every unverified change.

------------------------------------------------------------------------

## Git Workflow

Before a significant change:

``` powershell
git status
git add <specific-files>
git commit -m "feat: describe the completed milestone"
git push origin main
```

Before staging everything, inspect `git status` and confirm that no
secrets, local database dumps, generated files, or environment files are
included.

Suggested commit examples:

``` text
feat: add URL expiration
feat: add click analytics
feat: implement JWT authentication
test: stabilize repository tests
feat: add API rate limiting
docs: document API and deployment
```

------------------------------------------------------------------------

## Current Development Checkpoint

**Completed:** URL shortening, persistence, expiration, click analytics,
JWT authentication, refresh-token rotation, logout, URL ownership
checks, and test stabilization.

**Working on now:** Phase 8 --- rate limiting. The strategy and endpoint
scope have been discussed; the implementation and tests are still
pending.

**Next concrete task:** implement an isolated rate-limiter component,
write focused unit tests for its algorithm, then integrate it with
`POST /api/urls` and selected authentication endpoints.

------------------------------------------------------------------------

## License

No license has been selected yet. Add a `LICENSE` file before presenting
this repository as open source.
