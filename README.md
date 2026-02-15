# Banking API

A simple **REST API** for bank account operations, built to practice REST design, Spring Boot, and core Java concepts.

## Purpose

This project is a learning exercise that demonstrates:

- **REST API** design (resources, HTTP methods, status codes)
- **Spring Boot** (Web, JPA, Validation)
- **Layered architecture** (Controller → Service → Repository)
- **Java** features: DTOs, entities, exceptions, dependency injection

## Tech Stack

| Area | Technology |
|------|------------|
| Framework | Spring Boot 2.7 |
| Java | 11 |
| API | Spring Web (REST) |
| Persistence | Spring Data JPA |
| Database | H2 (in-memory) |
| Validation | Bean Validation (`javax.validation`) |
| Utilities | Lombok |

## Project Structure

```
src/main/java/org/example/bankingapi/
├── BankingApiApplication.java    # Entry point
├── controller/
│   └── AccountController.java    # REST endpoints
├── service/
│   └── AccountService.java       # Business logic
├── repository/
│   └── AccountRepository.java    # Data access (JPA)
├── entity/
│   └── Account.java              # JPA entity
├── dto/
│   ├── CreateAccountRequest.java
│   ├── UpdateAccountRequest.java
│   └── AccountResponse.java
└── exception/
    ├── AccountNotFoundException.java
    └── DuplicateAccountException.java
```

## API Overview

Base path: **`/api/accounts`**

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/accounts` | Create a new account |
| `GET` | `/api/accounts` | List all accounts |
| `GET` | `/api/accounts/{id}` | Get one account by ID |
| `PUT` | `/api/accounts/{id}` | Update an account |
| `DELETE` | `/api/accounts/{id}` | Delete an account |

### Create account (POST)

**Request body:**
```json
{
  "accountNumber": "ACC001",
  "accountHolderName": "John Doe",
  "balance": 1000.00,
  "currency": "USD"
}
```

- **201 Created** – Account created; response body contains the created account (with `id`, timestamps, etc.).
- **409 Conflict** – Account number already exists.

### Get account by ID / Update / Delete

- **404 Not Found** – No account with the given ID (for GET, PUT, DELETE).

## Concepts Practiced

- **REST**: Resource URLs, HTTP verbs, status codes (200, 201, 204, 404, 409).
- **Layered design**: Controllers handle HTTP; services hold logic; repositories handle persistence.
- **DTOs**: Request/response objects separate from the `Account` entity.
- **Validation**: `@Valid`, `@NotBlank`, `@NotNull`, `@DecimalMin`, etc., on request DTOs.
- **Exception handling**: Custom exceptions and `@ExceptionHandler` in the controller to map errors to HTTP responses.
- **JPA**: Entity, repository, in-memory H2 database.

## Run the Application

```bash
./mvnw spring-boot:run
```

Or with Maven installed:

```bash
mvn spring-boot:run
```

The API is available at `http://localhost:8080`. The H2 console is enabled (check `application.yaml` for path and credentials).

## Run Tests

```bash
./mvnw test
```

Unit tests cover the service and controller layers (JUnit 5, Mockito, MockMvc).
