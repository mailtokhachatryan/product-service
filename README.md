# Product Service

A Spring Boot REST service for a product catalog. It uses Spring MVC, Spring Data JPA, and PostgreSQL. The application still prints `hello world` on startup as part of its initial task.

## Requirements

- Java 21
- PostgreSQL with a database named `product_service` and a login that can create tables

The application uses the `local` profile by default. Its database defaults to `jdbc:postgresql://localhost:5432/product_service` with username `product_service`:

```shell
export DB_PASSWORD=your-local-password
./gradlew bootRun
```

Flyway applies versioned database migrations automatically at startup, while Hibernate validates the resulting schema.

## Runtime profiles

Select the environment with `SPRING_PROFILES_ACTIVE`. The application includes `local`, `dev`, `stg`, and `prod` profiles, each with its own PostgreSQL connection settings in `application-{profile}.yml`.

```shell
# Local is the default; the profile can also be selected explicitly.
export SPRING_PROFILES_ACTIVE=local
export DB_URL=jdbc:postgresql://localhost:5432/product_service
export DB_USERNAME=product_service
export DB_PASSWORD=your-local-password

# Non-local profiles use environment-specific connection variables.
export SPRING_PROFILES_ACTIVE=dev
export DEV_DB_URL=jdbc:postgresql://dev-db:5432/product_service
export DEV_DB_USERNAME=product_service
export DEV_DB_PASSWORD=your-dev-password

./gradlew bootRun
```

The `stg` and `prod` profiles use the corresponding `STG_DB_*` and `PROD_DB_*` variables. Their passwords have no committed defaults, so those values must be supplied by the runtime environment.

## Database migrations

Flyway runs migrations from `src/main/resources/db/migration` during application startup. The initial `V1__create_products_table.sql` migration creates the `products` table. Add future schema changes as new versioned migrations, such as `V2__add_product_category.sql`; do not edit a migration that has already been applied.

## Monitoring

Spring Boot Actuator exposes health and metrics endpoints under `/actuator`. The custom `productCatalog` health indicator checks the product repository and reports the current product count. Product create and delete operations publish custom Micrometer metrics, available through the secured `/actuator/metrics` endpoint and the Prometheus-compatible `/actuator/prometheus` endpoint. Health and info are public; operational metrics require an administrator JWT.

## OAuth2 and JWT security

Set local credentials before starting the application. Keep these values outside version control:

```shell
export APP_USER_PASSWORD=your-user-password
export APP_ADMIN_PASSWORD=your-admin-password
export OAUTH_CLIENT_SECRET=your-client-secret
export DB_PASSWORD=your-database-password
./gradlew bootRun
```

The local authorization server registers the confidential client `product-client` and supports Authorization Code with PKCE and refresh tokens. It exposes these standard endpoints:

- Authorization: `http://localhost:8080/oauth2/authorize`
- Token: `http://localhost:8080/oauth2/token`
- JWK Set: `http://localhost:8080/oauth2/jwks`
- OpenID Connect discovery: `http://localhost:8080/.well-known/openid-configuration`

To obtain a token in Postman, configure OAuth 2.0 with callback URL `https://oauth.pstmn.io/v1/callback`, client ID `product-client`, the value of `OAUTH_CLIENT_SECRET`, and scopes `openid profile product.read product.write`. Enable PKCE with `S256`, request a token, and sign in as one of the local users. A loopback callback at `http://127.0.0.1:8081/callback` is also registered for local command-line verification.

- `user` has role `USER` and may read products.
- `admin` has roles `USER` and `ADMIN` and may create, update, and delete products.

The access token is a signed JWT containing a `roles` claim. Copy it into an environment variable before using the API examples:

```shell
export ACCESS_TOKEN=your-jwt-access-token
```

## Product API

`Product` has an ID, name, optional description, price, and stock quantity. Names must not be blank; price and stock quantity must be non-negative.

```shell
# Create: 201 Created, with a Location header such as /api/products/1
curl -i -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Keyboard","description":"Mechanical","price":79.99,"stockQuantity":4}'

# List: 200 OK
curl -i http://localhost:8080/api/products \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Read: 200 OK, or 404 Not Found
curl -i http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Update: 200 OK, or 404 Not Found
curl -i -X PUT http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Keyboard","description":"Updated","price":69.99,"stockQuantity":6}'

# Delete: 204 No Content, or 404 Not Found
curl -i -X DELETE http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Use the ID from the create response's `Location` header in the read, update, and delete examples. Invalid request fields return `400 Bad Request` with field-specific errors.

## Test

```shell
./gradlew test
```

The tests use an in-memory database and require no running PostgreSQL server. They exercise the HTTP endpoints, authorization rules, JWT validation, OAuth2 metadata, and main error responses.
