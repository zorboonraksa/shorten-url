# URL Shortener API

A REST API that supports user authentication, URL shortening, URL management, and public redirects.

## Technology

- Java 25
- Spring Boot 4
- Spring Security with JWT
- JDBC Template
- PostgreSQL 17
- Maven
- Docker Compose
- JUnit, Mockito, and JaCoCo

## Prerequisites

For local development:

- JDK 25
- Maven 3.9+
- Docker with Docker Compose

For containerized execution:

- Docker with Docker Compose

## Environment variables

Create `.env` by copying `.env.template`, then replace the example values before running the application.

| Variable | Description | Example |
|---|---|---|
| `DB_HOST` | PostgreSQL host for local execution. Docker Compose overrides this with `db`. | `localhost` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `shorten_url` |
| `DB_USERNAME` | Database username | `shorten_url` |
| `DB_PASSWORD` | Database password | `change-me` |
| `SERVER_PORT` | Application HTTP port | `9090` |
| `JWT_SECRET` | Base64-encoded JWT secret containing at least 32 decoded bytes | See below |
| `JWT_EXPIRATION_SECONDS` | JWT lifetime in seconds | `3600` |

Generate a secure JWT secret. For example, with OpenSSL:

```shell
openssl rand -base64 32
```

Copy the resulting value into `JWT_SECRET` in `.env`. Do not commit the `.env` file or use the example password and secret in production.

## Run with Docker Compose

Compose automatically overrides `DB_HOST` with the PostgreSQL service name `db` for the application container. Keep `DB_HOST=localhost` in `.env` for local execution.

#### Build and start the application and database:

```shell
docker compose up --build
```

#### Run in the background instead:

```shell
docker compose up --build -d
```

#### Check container status and stop the stack:

```shell
docker compose ps
docker compose down
```

PostgreSQL data is retained in the `postgres_data` Docker volume. `docker compose down` does not remove this volume.

## Run locally

For local development, run only PostgreSQL in Docker and run the application with Maven.

1. Ensure `DB_HOST=localhost` in `.env`.
2. Start the database:

```shell
docker compose up db -d
```

3. Build and test the project:

```shell
mvn clean verify
```

4. Start the API:

```shell
mvn spring-boot:run
```

The schema in `src/main/resources/db/schema.sql` is applied automatically at startup. The API is available at `http://localhost:9090` when using the example configuration.

Stop the database when it is no longer needed:

```shell
docker compose stop db
```

## API examples

The examples use standard cURL syntax and can also be copied into **Import > Raw text** in Postman. The default base URL is `http://localhost:9090`. Replace `ACCESS_TOKEN` in authenticated requests with the `access_token` returned by login.

### Health check

#### cURL / Postman import:

```bash
curl --location 'http://localhost:9090/api/health-check'
```

#### Expected status: `200 OK`

```text
OK
```

### Register

#### cURL / Postman import:

```bash
curl --location 'http://localhost:9090/api/register' \
  --header 'Content-Type: application/json' \
  --data-raw '{"email":"user@example.com","password":"password123"}'
```

#### Expected status: `201 Created`

### Login

#### cURL / Postman import:

```bash
curl --location 'http://localhost:9090/api/login' \
  --header 'Content-Type: application/json' \
  --data-raw '{"email":"user@example.com","password":"password123"}'
```

#### Expected status: `200 OK`

```json
{
  "access_token": "eyJ...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

Copy `access_token` from the response and use it in place of `ACCESS_TOKEN` below.

### Shorten a URL

#### cURL / Postman import:

```bash
curl --location 'http://localhost:9090/api/shorten' \
  --header 'Authorization: Bearer ACCESS_TOKEN' \
  --header 'Content-Type: application/json' \
  --data-raw '{"original_url":"https://example.com/some/very/long/path"}'
```

#### Expected status: `201 Created`

```json
{
  "short_url": "http://localhost:9090/r/abc1234"
}
```

### View the current user's URLs

#### cURL / Postman import:

```bash
curl --location 'http://localhost:9090/api/urls' \
  --header 'Authorization: Bearer ACCESS_TOKEN'
```

#### Expected status: `200 OK`

```json
[
  {
    "id": 1,
    "original_url": "https://example.com/some/very/long/path",
    "short_url": "http://localhost:9090/r/abc1234",
    "created_at": "2026-09-05T07:00:00Z"
  }
]
```

### Delete a URL

Replace `1` with an ID returned by `GET /api/urls`:

#### cURL / Postman import:

```bash
curl --location --request DELETE 'http://localhost:9090/api/urls/1' \
  --header 'Authorization: Bearer ACCESS_TOKEN'
```

#### Expected status: `204 No Content`

### Redirect to the original URL

Replace `abc1234` with an actual short code:

#### cURL / Postman import:

```bash
curl --request GET 'http://localhost:9090/r/abc1234'
```

#### Expected status: `302 Found` with the original URL in the `Location` response header.

#### Follow the redirect automatically:

```bash
curl --location 'http://localhost:9090/r/abc1234'
```

Postman follows redirects by default. This behavior can be changed with the **Automatically follow redirects** request setting.

## Error response

API errors use a consistent JSON structure:

```json
{
  "timestamp": "2026-09-05T07:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Short URL not found",
  "path": "/r/missing"
}
```

The API can return `400`, `401`, `403`, `404`, `405`, `409`, and `500` depending on the request.

## Tests and coverage

Run the tests and generate the JaCoCo report:

```shell
mvn clean verify
```

Open the generated report at:

```text
target/site/jacoco/index.html
```
