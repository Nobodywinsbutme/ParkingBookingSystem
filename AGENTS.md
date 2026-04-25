# Agent / contributor notes — ParkingBookingSystem

## Stack (source of truth)

- **Single app** in `be/`: **Spring Boot 3**, **JSP** (`WEB-INF/views/`), **Bootstrap 5 + Bootstrap Icons** (CDN trong `common/head-bootstrap.jspf`), **`static/css/app.css`**, **`static/js`** (`app-ui.js`, `booking-live.js`, `validation.js`).
- **MySQL** via **Spring Data JPA**; schema: `be/src/main/resources/schema.sql`, optional seed `data.sql`.
- **Auth**: Spring Security **form login**, **HttpSession**, **BCrypt** passwords. No Next.js, no Prisma, no JWT cookie API layer in this repo.

## Before changing code

- Read nearby controllers/services/repositories and match naming, packages (`com.app.web` cho MVC — không dùng package `com.app.controller` trống; `com.app.service`, …).
- DB changes: update `schema.sql` (and `data.sql` if seed changes), keep JPA entities aligned (`snake_case` columns). For old DBs, align tables with `schema.sql` manually or recreate the database.
- Do not reintroduce a separate `fe/` or PostgreSQL unless the project owner explicitly asks.

## Running locally

```powershell
cd be
$env:SPRING_PROFILES_ACTIVE = 'local'   # if using application-local.yml
mvn spring-boot:run
```

Hoặc từ gốc repo: `npm run be:run` (xem `package.json`).

Copy `application-local.yml.example` → `application-local.yml` for local DB credentials (file is gitignored).

**Tests:** `be/src/test/java` có thể chưa có class — `mvn test` vẫn chạy được; thêm test khi cần.

## Documentation

- Run locally (DB + Maven): `docs/chạy.md`
- Architecture: `docs/he-thong-fe-be-db-flow.md`, `docs/luong-ket-noi-fe-be-db-chi-tiet.md`
- ERD: `docs/erd-smart-parking.md`
- SQL / DB coursework: `docs/sql.md` — code: `be/.../repository/BookingRepository.java` (phần demo), `com.app.web.QueryDemoRestController` (`/api/demo/queries/*`, read-only, `permitAll`)
- User journey: `docs/business-user-journey-tom-tat.md`
- Deploy / incidents: `docs/deployment-guide.md`, `docs/incident-runbook.md`
- DB course report: `docs/principles-of-database-management-project-report.md`

## Next.js note

This repository **does not** use Next.js. Ignore training-data assumptions about App Router / `fe/node_modules/next`; if a rule elsewhere still mentions Next.js for this path, treat it as **obsolete** for this repo.
