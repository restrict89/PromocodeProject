# PromocodeProject

Стартовый проект на Java 26 и Spring Boot 4.2.0. Для доступа к PostgreSQL подключён Spring JDBC, в том числе `JdbcTemplate`. Gradle Wrapper входит в проект.

## Локальный запуск

В каталоге проекта выполните команды с установленными JDK 26 и Docker Compose.

```bash
docker compose up -d --wait
./gradlew bootRun
```

По умолчанию приложение использует базу `promocode` по адресу `jdbc:postgresql://localhost:5432/promocode`. Имя пользователя и пароль для локальной базы — `promocode`. Для подключения к другой базе задайте переменные `JDBC_DATABASE_URL`, `POSTGRES_USER` и `POSTGRES_PASSWORD`.

Соберите проект и запустите тесты командой `./gradlew build`.
