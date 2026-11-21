# ShareIt

ShareIt — сервис для обмена вещами между пользователями. В нём можно публиковать вещи, искать доступные предложения, создавать и подтверждать бронирования, оставлять комментарии после завершённой аренды и размещать запросы на отсутствующие вещи.

## Архитектура

Проект состоит из трёх Gradle-модулей:

- `gateway` — внешний HTTP-вход на порту `8080`. Проверяет входные данные и проксирует запросы в `server` через `RestTemplate` и Apache HttpClient 5;
- `server` — приложение на порту `9090` с бизнес-логикой, транзакциями, Spring Data JPA/Hibernate и доступом к PostgreSQL;
- `api-contract` — общий Java-модуль с HTTP DTO. Модели генерируются как Java records из OpenAPI-спецификаций и используются обоими приложениями.

Между `gateway` и `server` нет прямой Gradle-зависимости: во время выполнения они взаимодействуют только по HTTP. Оба модуля зависят от `api-contract` на этапе компиляции.

```text
Клиент -> gateway :8080 -> HTTP -> server :9090 -> PostgreSQL
                \                    /
                 +-- api-contract --+
```

Для операций от имени пользователя применяется заголовок `X-Sharer-User-Id`.

## Функциональность

- пользователи: создание, получение, изменение и удаление;
- вещи: создание и изменение, просмотр вещей владельца, поиск доступных вещей;
- бронирования: создание, подтверждение или отклонение владельцем, выборки по состоянию с пагинацией;
- комментарии: публикация пользователем после завершённого бронирования;
- запросы вещей: создание запроса, просмотр собственных и чужих запросов, получение запроса по идентификатору.

Основные HTTP-контракты находятся в:

- [`specs/openapi/users.yaml`](specs/openapi/users.yaml);
- [`specs/openapi/items.yaml`](specs/openapi/items.yaml);
- [`specs/openapi/bookings.yaml`](specs/openapi/bookings.yaml);
- [`specs/openapi/requests.yaml`](specs/openapi/requests.yaml).

## Технологии

- Java 21;
- Spring Boot 3.5.6, Spring MVC и Jakarta Validation;
- Spring Data JPA / Hibernate;
- PostgreSQL, H2 в тестах;
- Gradle Wrapper 8.14.6;
- OpenAPI Generator 7.27.0;
- Lombok;
- Springdoc OpenAPI;
- JUnit 5, Mockito, MockMvc, `@DataJpaTest` и `@JsonTest`;
- Checkstyle, PMD и JaCoCo.

## Спецификации и генерация DTO

Разработка контрактов ведётся от спецификаций:

- [`specs/system.yaml`](specs/system.yaml) описывает архитектуру и системные ограничения;
- `specs/domains/*.yaml` фиксируют бизнес-правила и наблюдаемое поведение предметных областей;
- `specs/openapi/*.yaml` являются источником HTTP DTO.

Модуль `api-contract` генерирует records для областей `users`, `items`, `bookings` и `requests`. Для генерации всех моделей выполните:

```powershell
.\gradlew.bat generateAllDto
```

Доступны и отдельные задачи: `generateUserDto`, `generateItemDto`, `generateBookingDto` и `generateRequestDto`. Генерация также автоматически запускается перед компиляцией `api-contract`.

Результат помещается в `api-contract/build/generated/openapi-*`. Эти файлы не следует редактировать или добавлять в Git: контракт меняется через YAML-спецификации и повторную генерацию.

## Сборка и проверка

В репозитории находится Gradle Wrapper, поэтому устанавливать Gradle отдельно не требуется.

Полная проверка проекта:

```powershell
.\gradlew.bat clean build
```

Тесты отдельных приложений:

```powershell
.\gradlew.bat :gateway:test
.\gradlew.bat :server:test
```

Задача `test` использует JUnit Platform и профиль Spring `test`, формирует отчёт JaCoCo и проверяет минимальное покрытие строк `80%`. В `build` также входят Checkstyle и PMD. SpotBugs в текущей конфигурации отключён.

## Локальный запуск

Для `server` требуется PostgreSQL. Значения по умолчанию находятся в [`server/src/main/resources/application.yml`](server/src/main/resources/application.yml):

```text
URL:      jdbc:postgresql://localhost:5432/shareit
user:     postgres
password: admin
```

После запуска базы данных приложения можно поднять в двух терминалах:

```powershell
.\gradlew.bat :server:bootRun
```

```powershell
.\gradlew.bat :gateway:bootRun
```

Клиентские запросы следует отправлять в `gateway` по адресу `http://localhost:8080`. По умолчанию он обращается к `server` по адресу `http://localhost:9090`.

### Swagger UI

Springdoc подключён в модуле `server`:

- Swagger UI: [http://localhost:9090/swagger-ui/index.html](http://localhost:9090/swagger-ui/index.html);
- runtime OpenAPI: [http://localhost:9090/v3/api-docs](http://localhost:9090/v3/api-docs).

Файлы `specs/openapi/*.yaml` используются как источники DTO и хранятся в репозитории независимо от runtime-документации Springdoc.

## Docker

Dockerfile используют multi-stage сборку: Gradle Wrapper создаёт исполняемый `bootJar` на JDK 21, после чего приложение запускается в отдельном JRE 21-образе от непривилегированного пользователя `shareit`.

Для сборки образов и запуска всего приложения выполните:

```powershell
docker compose up --build
```

Compose запускает сервисы последовательно по health checks: PostgreSQL → `server` → `gateway`. После запуска доступны:

- внешний API через `gateway`: [http://localhost:8080](http://localhost:8080);
- `server` и Swagger UI: [http://localhost:9090](http://localhost:9090);
- PostgreSQL с хоста: `localhost:6541`.

Остановить контейнеры:

```powershell
docker compose down
```

Данные PostgreSQL сохраняются в именованном volume `shareit-db-data`. Команда `docker compose down -v` дополнительно удалит этот volume и все хранящиеся в нём данные.

## Структура проекта

```text
java-shareit/
├── api-contract/             # генерация и публикация общих HTTP DTO
├── gateway/                  # валидация внешних запросов и HTTP-прокси
├── server/                   # бизнес-логика и persistence
├── specs/
│   ├── domains/              # правила предметных областей
│   ├── openapi/              # HTTP-контракты и источник DTO
│   └── system.yaml           # архитектурная спецификация
├── openapi-templates/spring/ # шаблоны генерации Java records
├── .dockerignore             # исключения из Docker build context
├── build.gradle
├── settings.gradle
└── docker-compose.yml
```

Схема базы данных создаётся из [`server/src/main/resources/schema.sql`](server/src/main/resources/schema.sql); Liquibase и Flyway в проекте не настроены.
