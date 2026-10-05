# Homestay Booking System

Backend REST API built with Java 21, Spring Boot, Spring Security, JWT, JPA, and MySQL.

## Authentication and authorization

- `GET /` returns API status. Log in with `POST /auth/login` and a JSON body containing `email` and `password`.
- Spring Security loads the account through `UserDetailsServiceImpl` and verifies the submitted password against its BCrypt hash.
- Login returns a short-lived signed JWT access token and a random refresh token. Send the access token as `Authorization: Bearer <accessToken>` on protected requests.
- `POST /auth/refresh` accepts the refresh token, revokes it, and returns a rotated token pair. Only a SHA-256 hash of each refresh token is stored in MySQL.
- `POST /auth/logout` revokes the supplied refresh token. An already issued access token remains valid until its short expiry.
- `/admin/**` requires `ROLE_ADMIN`. Account creation with `POST /users` is public; other `/users` API operations require a valid access token. Other routes require authentication unless explicitly public.
- The API is stateless and CSRF is disabled because credentials are sent explicitly in JSON and the Authorization header, not automatically attached session cookies.
- New accounts receive `USER`; public account creation cannot assign `ADMIN`. Passwords are encoded in `UserService` before persistence.
- Unauthenticated protected API requests receive `401`, and forbidden requests receive `403`; an authenticated user can access only their own user record, while admins can access all users. The service enforces this ownership check as well as the URL rules.
- Existing passwords that are not BCrypt hashes are converted at startup by `LegacyPasswordMigrationRunner`. New and updated passwords are encoded before saving.

The self-service update endpoint does not allow a user to change their email; admins can change it. Email changes for a user should be added with an email verification flow.

## First administrator

Create an account through `POST /users` (include a valid CSRF token), then promote that account directly in the database:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';
```

Use a unique email in place of the example. The account's existing password remains a BCrypt hash.

## Database configuration

Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` for the MySQL instance. Set `JWT_SECRET` to a Base64-encoded random secret that decodes to at least 32 bytes. The default database URL targets `homestay_db` on localhost. `application-local.properties` can hold machine-specific settings and is excluded from Git.
