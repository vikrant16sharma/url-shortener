URL Shortener — Spring Boot

A backend URL shortening service built with Java 21 and Spring Boot, designed to demonstrate production-oriented backend development concepts including REST APIs, DTOs, validation, layered architecture, JPA, PostgreSQL, Base62 encoding, exception handling, auditing, and automated testing.

Current status: Core REST API and test suite are complete. PostgreSQL/JPA infrastructure is configured; persistent URL storage is the next development phase.

Features
Implemented
Create shortened URLs
Base62 short-code generation
RESTful API design
Request DTO validation
Response DTOs
URL format validation
HTTP redirect using 302 Found
Global exception handling
Service-layer architecture
Repository/JPA foundation
PostgreSQL configuration
JPA auditing
Automated controller tests
Automated service tests
Spring application context testing
Maven build and test workflow
Planned
Persist URLs completely through PostgreSQL
URL expiration
Click analytics
Authentication and authorization
Rate limiting
API documentation with OpenAPI/Swagger
Docker containerization
Production deployment
Performance optimization
Redis caching
Architecture

The application follows a layered backend architecture:

                    Client
                      │
                      ▼
                REST Controller
                      │
                      ▼
                    DTO
                      │
                      ▼
                 Validation
                      │
                      ▼
                  Service
                 /       \
                /         \
               ▼           ▼
       Code Generator   Repository
                            │
                            ▼
                       PostgreSQL
Request flow

For URL creation:

POST /api/urls
      │
      ▼
UrlController
      │
      ▼
CreateUrlRequest
      │
      ▼
Bean Validation
      │
      ▼
UrlShortenerService
      │
      ▼
Base62CodeGenerator
      │
      ▼
UrlRepository
      │
      ▼
PostgreSQL

For redirection:

GET /{code}
      │
      ▼
UrlController
      │
      ▼
UrlShortenerService
      │
      ▼
UrlRepository
      │
      ▼
Original URL
      │
      ▼
302 Found
Location: original URL
Tech Stack
Technology	Purpose
Java 21	Programming language
Spring Boot 4.1.1	Backend framework
Spring Web MVC	REST API
Spring Data JPA	Persistence abstraction
Hibernate	ORM
PostgreSQL	Relational database
Maven	Build & dependency management
JUnit	Testing
Mockito	Mocking
Jakarta Bean Validation	Request validation
Git & GitHub	Version control
Project Structure
urlshortener/
│
├── .mvn/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── vikrant/
│   │   │           └── urlshortener/
│   │   │               │
│   │   │               ├── controller/
│   │   │               │   └── UrlController.java
│   │   │               │
│   │   │               ├── dto/
│   │   │               │   ├── CreateUrlRequest.java
│   │   │               │   └── CreateUrlResponse.java
│   │   │               │
│   │   │               ├── entity/
│   │   │               │   └── Url.java
│   │   │               │
│   │   │               ├── exception/
│   │   │               │   ├── GlobalExceptionHandler.java
│   │   │               │   └── ShortUrlNotFoundException.java
│   │   │               │
│   │   │               ├── repository/
│   │   │               │   └── UrlRepository.java
│   │   │               │
│   │   │               ├── service/
│   │   │               │   └── UrlShortenerService.java
│   │   │               │
│   │   │               └── ...
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── vikrant/
│                   └── urlshortener/
│
├── .gitignore
├── .gitattributes
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
API
1. Create Short URL
Request
POST /api/urls
Content-Type: application/json
Body
{
  "url": "https://google.com"
}
Response
201 Created
{
  "shortUrl": "short.ly/x7Kp91a"
}
2. Redirect to Original URL
Request
GET /x7Kp91a
Response
302 Found
Location: https://google.com

The browser/client can then follow the Location header.

Validation

The URL creation endpoint validates incoming requests.

For example:

{
  "url": "google.com"
}

is rejected because the URL must begin with:

http://

or:

https://

Example valid URLs:

https://google.com
http://example.com
https://github.com/vikrant16sharma
Error Handling

The application uses centralized exception handling.

Example:

GET /doesNotExist

returns:

404 Not Found

The application uses:

GlobalExceptionHandler

to convert application exceptions and validation errors into appropriate HTTP responses.

Base62 Encoding

Short codes are generated using Base62 encoding.

The character set is:

0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ

This gives:

62 possible characters

per position.

Examples:

Decimal    Base62
-----------------
0          0
9          9
10         a
35         z
36         A
61         Z
62         10
63         11
125        21
3844       100

Base62 provides compact URL-friendly identifiers compared with directly exposing sequential decimal IDs.

Database

The project uses:

PostgreSQL

Database:

urlshortener

The Url entity contains:

id
originalUrl
shortCode
createdAt
updatedAt

The short code has a uniqueness constraint:

uk_urls_short_code

This prevents multiple URLs from using the same short code.

JPA Auditing

The project uses Spring Data JPA auditing for automatically maintaining:

createdAt
updatedAt

The entity uses:

@CreatedDate

and:

@LastModifiedDate

This removes the need to manually assign timestamps in service code.

Configuration

Database credentials are supplied through environment variables rather than being hardcoded.

spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/urlshortener}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD}

Set the password before running the application.

PowerShell
$env:DB_PASSWORD="your_password"

Optional:

$env:DB_URL="jdbc:postgresql://localhost:5432/urlshortener"
$env:DB_USERNAME="postgres"
Running the Application
Prerequisites

Make sure you have:

Java 21
PostgreSQL
Maven

Verify Java:

java -version

Verify Maven:

mvn -version
Clone the repository
git clone https://github.com/vikrant16sharma/url-shortener.git

Navigate into the project:

cd url-shortener
Configure PostgreSQL

Create the database:

CREATE DATABASE urlshortener;

Set the database password:

$env:DB_PASSWORD="your_password"
Run the application

Using Maven:

mvn spring-boot:run

Or using the Maven wrapper:

.\mvnw spring-boot:run

The application will run on the default Spring Boot port:

http://localhost:8080
Testing

The project currently contains tests for:

Controller
UrlControllerTest

Tests include:

Successful URL creation
Redirect response
Missing short-code handling
Invalid URL validation
Service
UrlShortenerServiceImplTest

Tests the service behavior independently using mocks.

Application Context
UrlshortenerApplicationTests

Verifies that the Spring application context can start successfully.

Run the complete test suite:

mvn clean test

Current checkpoint:

Tests run: 9
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
Development Workflow

The project is being developed incrementally.

Phase 1 — Core URL Shortener
 URL creation
 Base62 generation
 REST endpoints
 DTOs
 Validation
 Exception handling
 Unit tests
 Controller tests
Phase 2 — Persistence
 PostgreSQL setup
 JPA configuration
 URL entity
 Repository foundation
 JPA auditing
 Persist URL records
 Retrieve URLs from PostgreSQL
 Remove remaining in-memory storage
Phase 3 — Production Features
 URL expiration
 Click tracking
 Analytics
 Authentication
 Rate limiting
 Caching
Phase 4 — Productionization
 OpenAPI/Swagger
 Docker
 Integration tests
 CI/CD
 Production deployment
 Monitoring
 Performance testing
Design Goals

The project is being developed with the following principles:

Separation of concerns
Dependency injection
Interface-based design
DTO-based API contracts
Centralized exception handling
Input validation
Database constraints
Automated testing
Environment-based configuration
Production-oriented project structure
Git Workflow

The project uses incremental commits for major development checkpoints.

Example:

git add .
git commit -m "feat: complete URL shortener API with tests"
git push

Future feature commits will follow a similar convention:

feat: persist urls with postgresql
feat: add url expiration
feat: add click analytics
feat: add authentication
feat: add rate limiting
Future Architecture

The intended architecture will evolve toward:

                         Client
                           │
                           ▼
                    Spring Boot API
                           │
                    ┌──────┴──────┐
                    │             │
              Controller       Security
                    │
                    ▼
                  Service
                 /       \
                /         \
               ▼           ▼
       Code Generator   Repository
                           │
                           ▼
                      PostgreSQL
                           │
                    ┌──────┴──────┐
                    │             │
                  Redis        Analytics
                    │
                    ▼
                  Cache

The goal is to use this project to explore the transition from a simple backend application to a more production-oriented distributed service.

Author

Vikrant Sharma

B.Tech — Computer Science & Engineering (Artificial Intelligence)

GitHub: https://github.com/vikrant16sharma
