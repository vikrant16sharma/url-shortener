🚀 Project Overview

URL Shortener is a backend service that converts long URLs into compact, shareable URLs.

For example:

https://www.example.com/products/category/item?id=12345
                              ↓
                    short.ly/x7Kp91a

When a user accesses the shortened URL, the service resolves the short code and redirects them to the original URL.

The project is being built incrementally with a focus on clean architecture, maintainability, testability, and production-oriented backend practices.

✨ Current Capabilities
Capability	Status
REST API	✅
URL validation	✅
Base62 code generation	✅
DTO-based API contracts	✅
Global exception handling	✅
Layered architecture	✅
JPA Entity	✅
PostgreSQL configuration	✅
JPA Auditing	✅
Controller tests	✅
Service tests	✅
Application context tests	✅
Persistent URL storage	🚧
URL expiration	🔜
Click analytics	🔜
Authentication	🔜
Rate limiting	🔜
Redis caching	🔜
Docker	🔜
CI/CD	🔜

Current milestone: Core API + testing infrastructure is complete. The next milestone is connecting the service's URL storage flow completely to PostgreSQL.

🏗️ Architecture

The application follows a layered architecture designed to keep business logic independent from HTTP and persistence concerns.

                         ┌──────────────┐
                         │    Client    │
                         └──────┬───────┘
                                │
                                ▼
                    ┌─────────────────────┐
                    │   REST Controller   │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │        DTOs         │
                    │ Request / Response  │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │     Validation      │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │       Service       │
                    │   Business Logic    │
                    └───────┬───────┬─────┘
                            │       │
                            ▼       ▼
                 ┌────────────┐  ┌────────────┐
                 │   Base62   │  │ Repository │
                 │ Generator  │  │    Layer   │
                 └────────────┘  └──────┬─────┘
                                        │
                                        ▼
                                ┌──────────────┐
                                │  PostgreSQL  │
                                └──────────────┘
Design principle

Each layer has one primary responsibility:

Controller   → HTTP
DTO          → API contract
Validation   → Input correctness
Service      → Business logic
Generator    → Short-code generation
Repository   → Data access
Entity       → Database representation
PostgreSQL   → Persistent storage
🔄 Request Lifecycle
Create Short URL
POST /api/urls
       │
       ▼
┌─────────────────┐
│  UrlController  │
└────────┬────────┘
         │
         ▼
┌──────────────────────┐
│ CreateUrlRequest     │
│ { "url": "..." }     │
└──────────┬───────────┘
           │
           ▼
     Bean Validation
           │
           ▼
┌──────────────────────┐
│ UrlShortenerService  │
└──────────┬───────────┘
           │
           ▼
   Base62 Code Generator
           │
           ▼
┌──────────────────────┐
│ CreateUrlResponse    │
│ { "shortUrl": "..." }│
└──────────────────────┘
Example

Request

POST /api/urls
Content-Type: application/json
{
  "url": "https://google.com"
}

Response

201 Created
{
  "shortUrl": "short.ly/x7Kp91a"
}
↪️ URL Redirection
GET /x7Kp91a
       │
       ▼
 UrlController
       │
       ▼
 UrlShortenerService
       │
       ▼
   URL lookup
       │
       ▼
Original URL
       │
       ▼
  302 Found
       │
       ▼
Location: https://google.com

Example:

GET /x7Kp91a

Response:

302 Found
Location: https://google.com
🔢 Base62 Short-Code Generation

The project uses Base62 encoding to generate compact URL-friendly codes.

Character set:

0123456789
abcdefghijklmnopqrstuvwxyz
ABCDEFGHIJKLMNOPQRSTUVWXYZ

That provides:

62 characters

per position.

Examples
Decimal	Base62
0	0
9	9
10	a
35	z
36	A
61	Z
62	10
63	11
125	21
3844	100

Base62 allows the generated identifiers to remain relatively short while using only URL-friendly characters.

🗄️ Data Model

The Url entity currently represents:

┌──────────────────────────────────┐
│              urls                │
├──────────────────────────────────┤
│ id            BIGINT             │
│ original_url  VARCHAR            │
│ short_code    VARCHAR UNIQUE     │
│ created_at    TIMESTAMP          │
│ updated_at    TIMESTAMP          │
└──────────────────────────────────┘

The short code has a unique constraint:

uk_urls_short_code

This ensures that two URL records cannot share the same short code.

🕒 JPA Auditing

The project uses Spring Data JPA auditing to automatically maintain timestamps.

@CreatedDate
private LocalDateTime createdAt;

@LastModifiedDate
private LocalDateTime updatedAt;

This means timestamp management does not need to be manually implemented inside the service layer.

🧩 API Design
POST /api/urls

Creates a shortened URL.

Request
{
  "url": "https://example.com"
}
Response
{
  "shortUrl": "short.ly/x7Kp91a"
}
Status
201 Created
GET /{code}

Redirects to the original URL.

Example
GET /x7Kp91a
Response
302 Found
Location: https://example.com
🛡️ Validation & Error Handling

Incoming URLs are validated before reaching the business layer.

Valid:

https://google.com
http://example.com
https://github.com/vikrant16sharma

Invalid:

google.com
example

The application uses a centralized:

GlobalExceptionHandler

to handle:

Validation failures
Missing short codes
Application exceptions
Appropriate HTTP status codes

Example:

GET /doesNotExist
404 Not Found
🧪 Testing

Testing is treated as part of the development process rather than something added at the end.

Test layers
┌─────────────────────────────┐
│   Application Context Test  │
├─────────────────────────────┤
│       Controller Tests      │
├─────────────────────────────┤
│         Service Tests       │
└─────────────────────────────┘
Current test suite

UrlControllerTest

Create short URL
Redirect
Non-existent short code
Invalid URL

UrlShortenerServiceImplTest

Service behavior
Mocked dependencies
Short-code generation flow

UrlshortenerApplicationTests

Spring application context startup
Current result
Tests run:     9
Failures:      0
Errors:        0
Skipped:       0

BUILD SUCCESS

Run the test suite:

mvn clean test
⚙️ Tech Stack
Technology	Role
Java 21	Application language
Spring Boot 4.1.1	Backend framework
Spring Web MVC	REST API
Spring Data JPA	Persistence abstraction
Hibernate	ORM
PostgreSQL	Database
Maven	Build & dependency management
JUnit	Testing
Mockito	Mocking
Jakarta Validation	Request validation
Git / GitHub	Version control
📁 Project Structure
urlshortener/
│
├── .mvn/
│
├── src/
│   │
│   ├── main/
│   │   │
│   │   ├── java/
│   │   │   └── com/vikrant/urlshortener/
│   │   │       │
│   │   │       ├── controller/
│   │   │       │   └── UrlController.java
│   │   │       │
│   │   │       ├── dto/
│   │   │       │   ├── CreateUrlRequest.java
│   │   │       │   └── CreateUrlResponse.java
│   │   │       │
│   │   │       ├── entity/
│   │   │       │   └── Url.java
│   │   │       │
│   │   │       ├── exception/
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   └── ShortUrlNotFoundException.java
│   │   │       │
│   │   │       ├── repository/
│   │   │       │   └── UrlRepository.java
│   │   │       │
│   │   │       ├── service/
│   │   │       │   └── UrlShortenerService.java
│   │   │       │
│   │   │       └── ...
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       └── java/
│
├── .gitignore
├── .gitattributes
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
🔐 Configuration

Database credentials are not hardcoded.

The application uses environment variables:

spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/urlshortener}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD}
PowerShell
$env:DB_PASSWORD="your_password"

Optional:

$env:DB_URL="jdbc:postgresql://localhost:5432/urlshortener"
$env:DB_USERNAME="postgres"

This keeps credentials outside the source code.

🏃 Getting Started
Requirements
Java 21
PostgreSQL
Maven
Git

Verify:

java -version
mvn -version
Clone
git clone https://github.com/vikrant16sharma/url-shortener.git
cd url-shortener
Create Database
CREATE DATABASE urlshortener;

Set your PostgreSQL password:

$env:DB_PASSWORD="your_password"
Start Application

Using Maven:

mvn spring-boot:run

or Maven Wrapper:

.\mvnw spring-boot:run

Application:

http://localhost:8080
🗺️ Development Roadmap

The project is intentionally being developed in stages.

Phase 1 — Core API
 REST API
 URL creation
 Base62 generation
 Redirect
 DTOs
 Validation
 Exception handling
 Unit tests
 Controller tests
Phase 2 — Persistence
 PostgreSQL configuration
 JPA
 Hibernate
 URL entity
 Repository foundation
 JPA auditing
 Persist URLs through repository
 Retrieve URLs through repository
 Remove remaining in-memory storage
Phase 3 — Advanced Backend
 URL expiration
 Click analytics
 Authentication
 Authorization
 Rate limiting
 Redis caching
Phase 4 — Productionization
 OpenAPI / Swagger
 Docker
 Integration testing
 CI/CD
 Deployment
 Monitoring
 Performance testing
🔮 Planned Architecture

As the system evolves, the architecture will move toward a more production-oriented design:

                         ┌─────────────┐
                         │   Client    │
                         └──────┬──────┘
                                │
                                ▼
                    ┌─────────────────────┐
                    │   Spring Boot API   │
                    └──────────┬──────────┘
                               │
                  ┌────────────┴────────────┐
                  │                         │
                  ▼                         ▼
             Controller                 Security
                  │
                  ▼
               Service
             /    │     \
            /     │      \
           ▼      ▼       ▼
      Generator  Cache  Repository
                        │
                        ▼
                   PostgreSQL
                        │
               ┌────────┴────────┐
               │                 │
               ▼                 ▼
          URL Storage        Analytics

Potential future infrastructure:

                    ┌──────────────┐
                    │   Clients    │
                    └──────┬───────┘
                           │
                           ▼
                    ┌──────────────┐
                    │ Load Balancer│
                    └──────┬───────┘
                           │
              ┌────────────┼────────────┐
              ▼            ▼            ▼
          API Instance  API Instance  API Instance
              │            │            │
              └────────────┼────────────┘
                           │
                    ┌──────┴──────┐
                    │    Redis    │
                    └──────┬──────┘
                           │
                    ┌──────┴──────┐
                    │ PostgreSQL  │
                    └─────────────┘
🧠 Engineering Principles

This project focuses on practicing backend engineering concepts that extend beyond simply making an API work.

Separation of Concerns

Each layer has a clearly defined responsibility.

Dependency Injection

Dependencies are provided through constructors rather than created directly inside classes.

Interface-Based Design

Business components are designed around abstractions where appropriate.

DTO-Based APIs

Internal entities are separated from external API contracts.

Validation

Invalid input is rejected at the API boundary.

Centralized Error Handling

Application errors are translated into consistent HTTP responses.

Database Constraints

Important invariants are enforced at the database level.

Automated Testing

Changes are verified through unit, MVC, and application-context tests.

Environment-Based Configuration

Credentials and environment-specific configuration remain outside the source code.

📈 Development Philosophy

The project is intentionally not being built as a single large implementation.

The development approach is:

Fundamentals
     ↓
Working API
     ↓
Clean Architecture
     ↓
Automated Tests
     ↓
Persistence
     ↓
Performance
     ↓
Security
     ↓
Scalability
     ↓
Production Deployment

Each stage introduces a new engineering problem and builds on the previous one.

📝 Git Development

The project uses incremental feature-based commits.

Example:

git add .
git commit -m "feat: persist urls with postgresql"
git push

Current checkpoint:

feat: complete URL shortener API with tests

Future checkpoints:

feat: persist urls with postgresql
feat: add url expiration
feat: add click analytics
feat: add authentication
feat: add rate limiting
feat: add redis caching
feat: dockerize application
👨‍💻 Author
Vikrant Sharma

B.Tech — Computer Science & Engineering (Artificial Intelligence)

Building backend systems with Java, Spring Boot, PostgreSQL and AI/ML technologies.

GitHub:
https://github.com/vikrant16sharma
