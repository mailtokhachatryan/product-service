# Product Service

A Spring Boot service for managing a product catalog. The service will grow through focused merge requests covering CRUD operations, OAuth2/JWT security, environment-specific configuration, schema migrations, observability, and automated testing.

## Initial application

The project was generated with Spring Initializr using:

- Java 21
- Spring Boot 4.1.1
- Gradle with the Groovy DSL
- Packaging: JAR
- Dependency: Spring Boot Starter only

The initial application implements `CommandLineRunner` and prints `hello world` after the Spring context starts.

## Run

```shell
./gradlew bootRun
```

Look for this line in the startup output:

```text
hello world
```

## Test

```shell
./gradlew test
```
