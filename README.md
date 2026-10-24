# Product Service

A Spring Boot REST service for a product catalog. It uses Spring MVC, Spring Data JPA, and PostgreSQL. The application still prints `hello world` on startup as part of its initial task.

## Requirements

- Java 21
- PostgreSQL with a database named `product_service` and a login that can create tables

The application defaults to `jdbc:postgresql://localhost:5432/product_service` and username `product_service`. Supply the password and override the connection settings when needed:

```shell
export DB_URL=jdbc:postgresql://localhost:5432/product_service
export DB_USERNAME=product_service
export DB_PASSWORD=your-local-password
./gradlew bootRun
```

For this CRUD stage, Hibernate creates or updates the local schema on startup. A later migration task will replace this with versioned schema changes.

## Product API

`Product` has an ID, name, optional description, price, and stock quantity. Names must not be blank; price and stock quantity must be non-negative.

```shell
# Create: 201 Created, with a Location header such as /api/products/1
curl -i -X POST http://localhost:8080/api/products \
  -H 'Content-Type: application/json' \
  -d '{"name":"Keyboard","description":"Mechanical","price":79.99,"stockQuantity":4}'

# List: 200 OK
curl -i http://localhost:8080/api/products

# Read: 200 OK, or 404 Not Found
curl -i http://localhost:8080/api/products/1

# Update: 200 OK, or 404 Not Found
curl -i -X PUT http://localhost:8080/api/products/1 \
  -H 'Content-Type: application/json' \
  -d '{"name":"Keyboard","description":"Updated","price":69.99,"stockQuantity":6}'

# Delete: 204 No Content, or 404 Not Found
curl -i -X DELETE http://localhost:8080/api/products/1
```

Use the ID from the create response's `Location` header in the read, update, and delete examples. Invalid request fields return `400 Bad Request` with field-specific errors.

## Test

```shell
./gradlew test
```

The tests use an in-memory database and require no running PostgreSQL server. They exercise the HTTP endpoints and their main error responses.
