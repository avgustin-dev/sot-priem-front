# sot-reception-api

Бэкенд платформы приёма граждан руководством Верховного суда КР — Java 21, Spring Boot 4.1.0, Liquibase.

Реализует контракт `docs/backend/openapi.yaml` из фронтенд-репозитория (`sot_digital_appeals`) **дословно**: пути, методы, тела запросов/ответов и коды ошибок не менялись.

## Структура

Классическая слоистая структура на пакет `kg.sot.reception`:

```
config/       свойства приложения, Security, OpenAPI
controller/   REST-контроллеры (/api/v1/...)
dto/          тела запросов/ответов и вложенные JSON-объекты (records)
exception/    ApiException + единый обработчик ошибок → { status, code, message }
model/        JPA-сущности и enum'ы
repository/   Spring Data JPA репозитории
security/     JWT (jjwt), фильтр, UserDetailsService
seed/         первичное наполнение БД (тексты сайта, дерево допуска, опросник, демо-данные)
service/      бизнес-логика (порт логики src/lib/store.ts, slots.ts, targets.ts фронта)
util/         генераторы id/кода/PIN, расчёт слотов, JSON-конвертеры для JPA
```

## Хранение CMS-контента

`ServiceContent` (тексты сайта), дерево допуска и опросник в контракте описаны как объекты, которые сохраняются/отдаются **целиком** (`PUT` заменяет всё), а не по полям. Поэтому они хранятся как JSON-колонки (`TEXT` в Liquibase) в таблицах-синглтонах (`site_content`, `eligibility_tree`, `survey_config`) вместо разворачивания в десятки реляционных колонок — это соответствует `additionalProperties: true` для `/staff/content` в самой спеке. Вложенные списки записи/обращения (`companions`, `history`, `controlLog`, `notifications`, `receptionProtocol`, `assignment`, `feedback`) хранятся так же — через `AttributeConverter` (`util/converter/*`).

## Запуск

### Вариант 1 — без Docker (H2, по умолчанию)

```bash
mvn spring-boot:run
```

Профиль `h2` активен по умолчанию (`SPRING_PROFILES_ACTIVE` не задан) — файловая БД `./data/sot-reception.mv.db`, миграции применяются автоматически при старте.

### Вариант 2 — PostgreSQL

```bash
docker compose up -d
SPRING_PROFILES_ACTIVE=postgres mvn spring-boot:run
```

## Переменные окружения

| Переменная | По умолчанию | Назначение |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `h2` | `h2` или `postgres` |
| `SERVER_PORT` | `8080` | порт API |
| `APP_CORS_ORIGINS` | `http://localhost:3000` | список через запятую |
| `APP_JWT_SECRET` | dev-заглушка | **обязательно сменить в проде** (≥32 байт) |
| `APP_JWT_EXPIRATION_MINUTES` | `480` | срок жизни JWT |
| `APP_SEED_DEMO_DATA` | `true` | демо-сотрудники и демо-заявки; выключить в проде |
| `APP_SITE_BASE_URL` | `https://sot.kg` | для ссылок в письмах (когда подключите реальную отправку) |

Фронт (`sot_digital_appeals`) указывает на бэкенд через `NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1`.

## Демо-доступы (только при `APP_SEED_DEMO_DATA=true`)

| login | password | роль |
| --- | --- | --- |
| `admin` | `admin123` | admin |
| `priemnaya` | `priem123` | reception |
| `rukovodstvo` | `sud2026` | leadership |
| `predsedatel` | `vs2026` | leadership |
| `otvet1` / `otvet2` | `otvet123` | responsible |

## Почта

Реальная отправка не подключена — `NotificationService` пишет письмо/уведомление в лог и в карточку обращения (`notifications[]`), как и полагается по контракту (см. `docs/backend/README.md` → «Почта и талон»). Зависимость `spring-boot-starter-mail` уже добавлена — для реальной отправки останется настроить `spring.mail.*` и заменить `log.info(...)` на `JavaMailSender`.

## Аудит-лог

Каждый HTTP-запрос к API пишется отдельной строкой в `logs/audit.log` (ротация раз в сутки, хранится 90 дней) — кто (логин+роль сотрудника или `public`), с какого IP, что (метод + путь + параметры запроса), с каким статусом ответа и за сколько миллисекунд. Ловятся и отказы (`401`/`403`), не только успешные запросы. Тела запросов и ответов не пишутся — там PIN и пароли.

Пример строки:
```
2026-08-20T12:03:11.482+06:00 actor=admin(admin) ip=127.0.0.1 method=POST path=/api/v1/staff/appointments/apt-123/confirm status=200 durationMs=41
2026-08-20T12:04:02.117+06:00 actor=public ip=203.0.113.7 method=POST path=/api/v1/public/appointments/VS-2026-4821/unlock status=409 durationMs=6
```

Реализация — `audit/AuditLogFilter.java`, конфигурация вывода — `logback-spring.xml`.

## Swagger

`http://localhost:8080/swagger-ui.html`

## Не реализовано в v1 (по контракту)

CRUD сотрудников, смена пароля, удаление ответов анкеты, вложения файлов — намеренно вне контракта (см. `docs/backend/README.md`).
