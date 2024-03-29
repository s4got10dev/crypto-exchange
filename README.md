# Crypto Exchange

## 🔎 About
Application provides crypto exchange service with User and Wallet creation.
Functionality for making deposits and withdrawals, as well as placing orders.
Orders are matched with each other and executed in FIFO order.

## 📖 How to use
The Project already has gradle wrapper (so you don't need to have it installed).
It will download all other dependencies.

## 🚀 Execution
Execute gradle build (It may take a while in the first run):
```sh
./gradlew build
```
Run docker compose:
```sh
cd docker
docker-compose up
```
After the message of BUILD SUCCESS, the application is ready to run:
```sh
./gradlew bootRun
```

Application will be available on [http://localhost:8080](http://localhost:8080)
(Can be changed in [application.yaml](src/main/resources/application.yaml)).

Open API documentation will be available at [http://localhost:8080/api-docs/v3/openapi](http://localhost:8080/api-docs/v3/openapi)
Swagger UI will be available at [http://localhost:8080/api-docs/swagger-ui](http://localhost:8080/api-docs/swagger-ui)


## 💡Tech stack
- 🧠 **Backend**
    - ☕️ **Kotlin**
    - 🍃 **Spring Boot**
        - WebFlux
      - Security
      - Validation
        - Data R2DBC
      - Kotlin Coroutines
        - SpringDoc OpenAPI+Swagger
    - ⚙️ **Misc**
        - Liquibase
    - 🧪 **Tests**
        - JUnit 5
        - Mockk
        - Testcontainers
        - Awaitility
- 💾 **Storage**
    - 🔗 PostgreSQL
- 🏗 **Gradle**
- 🐳 **Docker compose**

