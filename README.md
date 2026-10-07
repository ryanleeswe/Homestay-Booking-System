# Homestay Booking System

Backend REST API built with Java 21, Spring Boot, Spring Security, JWT, JPA, and MySQL.

## Authentication and authorization

- `GET /` returns API status. Log in with `POST /auth/login` and a JSON body containing `email` and `password`.
- Spring Security loads the account through `UserDetailsServiceImpl` and verifies the submitted password against its BCrypt hash.
- Login returns a short-lived signed JWT access token and a random refresh token inside `data`. Send `data.accessToken` as `Authorization: Bearer <accessToken>` on protected requests.
- `POST /auth/refresh` accepts the refresh token, revokes it, and returns a rotated token pair. Only a SHA-256 hash of each refresh token is stored in MySQL.
- `POST /auth/logout` revokes the supplied refresh token. An already issued access token remains valid until its short expiry.
- `/admin/**` requires `ROLE_ADMIN`. Account creation with `POST /users` is public; other `/users` API operations require a valid access token. Other routes require authentication unless explicitly public.
- The API is stateless and CSRF is disabled because credentials are sent explicitly in JSON and the Authorization header, not automatically attached session cookies.
- Unauthenticated protected API requests receive JSON `401`, and forbidden requests receive JSON `403`.
- Existing passwords that are not BCrypt hashes are converted at startup by `LegacyPasswordMigrationRunner`.

`UserService` encodes new and updated passwords with BCrypt, forces new accounts to
the USER role, and checks ownership or ADMIN privileges for individual user reads,
updates and deletions. Only admins can change email; duplicate emails return 409.
Newly created accounts can log in immediately without restarting the application.

## First administrator

Create an account through `POST /users`, then promote that account directly in the database:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';
```

Use a unique email in place of the example.

## Database configuration

Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` for the MySQL instance. Set `JWT_SECRET` to a Base64-encoded random secret that decodes to at least 32 bytes. The default database URL targets `homestay_db` on localhost. `application-local.properties` can hold machine-specific settings and is excluded from Git.

## Run locally on Windows

Use JDK 21 and a running MySQL server on port 3306. Maven Wrapper is included.
In MySQL, create the database if the application account cannot create it:

```sql
CREATE DATABASE IF NOT EXISTS homestay_db CHARACTER SET utf8mb4;
```

Set the following in the same PowerShell window used to start the application:

```powershell
$env:DB_USERNAME = 'root'
$env:DB_PASSWORD = 'your-mysql-password'
# For local development, generate a secret once and retain it for later runs.
$env:JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
.\mvnw.cmd spring-boot:run
```

The API runs at `http://localhost:8080/`. Login tokens are now under
`data.accessToken` and `data.refreshToken` in Postman. Regenerating `JWT_SECRET`
invalidates previously issued access tokens.

## Common response format

`domain/dto/RestResponse<T>` is the shared DTO for all JSON API responses:

```json
{
  "statusCode": 200,
  "message": "Lấy danh sách thành công",
  "error": null,
  "data": { "items": [] }
}
```

`data` preserves the original endpoint payload: users remain a list, a user remains
an object, and authentication returns a token pair. An error instead has an HTTP
reason such as `Forbidden` in `error` and `data: null`; `statusCode` matches HTTP.

- Keep using `util/annotation/ApiMessage` on controller methods to choose the success
  message. Without it, the default message is `Thành công`.
- `util/ResponseBodyAdvice` wraps only successful 2xx responses, including String
  and empty 200 responses. Existing envelopes are not wrapped again.
- `util/error/GlobalException` handles general exceptions, invalid IDs, missing
  resources, validation, malformed JSON, authentication and access-denied failures.
- `SecurityExceptionHandler` forwards JWT/security-filter failures to
  `GlobalException` and serializes its result. JWT, stateless sessions, existing
  route permissions and disabled CSRF are retained.
- Controllers return business data and HTTP status. Throw an exception for errors
  instead of returning an unwrapped error body. HTTP HEAD, 204, 205 and 304 responses
  have no body as required by HTTP; use 200 when an envelope is needed.
- User lookup, update and deletion reject null/non-positive IDs with 400 and
  nonexistent users with 404, using the same JSON error envelope. User pagination defaults to `current=1` and
  `pageSize=10`; invalid values produce 400.

## Room relationships

`Homestay` maps to `homestays`, `Room` to `rooms`, and `Booking` to `bookings`.
A homestay has many rooms; a room has many bookings, and a booking references one
room. Required foreign keys are `rooms.homestay_id` and `bookings.room_id`.

Room contains `id`, `name`, `capacity`, and `pricePerNight` (decimal 12,2), plus the
entity associations. Set `room.setHomestay(homestay)` and `booking.setRoom(room)`
before saving. Parent deletion does not cascade into booking history. Associations
are excluded from entity JSON to avoid recursive serialization; future endpoint
DTOs should expose the needed relationship IDs.

Booking and Homestay are minimal supporting entities; room CRUD and a booking
workflow are not part of this change. The existing `ddl-auto=update` configuration
creates the tables/foreign keys when the application connects to MySQL.

## Focused tests

```powershell
.\mvnw.cmd "-Dtest=ApiResponseTests,RoomPersistenceTests,UserIdValidationTests,UserSecurityTests" test
```

These tests use mocked application services for HTTP tests and an isolated H2
database for entity relationships. They require neither MySQL nor a real JWT secret
and do not modify the application database. The existing `contextLoads` integration
test still requires its database and JWT configuration.
