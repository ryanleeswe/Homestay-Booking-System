# Homestay Booking System

REST API and server-rendered pages built with Java 21, Spring Boot, Spring Security, Thymeleaf, JPA, and MySQL.

## Authentication and authorization

- `GET /` and `GET /login` are public. The login page posts the email and password to Spring Security's `POST /login` endpoint.
- Spring Security loads the account through `UserDetailsServiceImpl` and verifies the submitted password against its BCrypt hash.
- Successful login creates a server-side session. The browser sends the session cookie on later requests.
- `/admin/**` requires `ROLE_ADMIN`. Account creation with `POST /users` is public; other `/users` API operations require a signed-in user. Other routes require sign-in unless explicitly public.
- CSRF protection is enabled. Logout uses a CSRF-protected `POST /logout` and invalidates the session.
- New accounts receive `USER`; public account creation cannot assign `ADMIN`. Passwords are encoded in `UserService` before persistence.
- Unauthenticated protected `/users` API requests receive `401`; an authenticated user can access only their own user record, while admins can access all users. The service enforces this ownership check as well as the URL rules.
- Existing passwords that are not BCrypt hashes are converted at startup by `LegacyPasswordMigrationRunner`. New and updated passwords are encoded before saving.

The self-service update endpoint does not allow a user to change their email; admins can change it. Email changes for a user should be added with an email verification flow.

## First administrator

Create an account through `POST /users` (include a valid CSRF token), then promote that account directly in the database:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';
```

Use a unique email in place of the example. The account's existing password remains a BCrypt hash.

## Database configuration

Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` for the MySQL instance. The default URL targets `homestay_db` on localhost. `application-local.properties` can hold machine-specific settings and is excluded from Git.
