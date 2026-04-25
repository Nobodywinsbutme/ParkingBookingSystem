# Hướng dẫn triển khai — Spring Boot + MySQL

Tóm tắt; chi tiết biến môi trường cũng có trong `README.md` (mục Stripe & chạy local).

## Kiến trúc gợi ý

- Một tiến trình **JAR Spring Boot** (Tomcat nhúng) + **MySQL 8+** riêng.
- **HTTPS** qua reverse proxy nếu public internet.

## Build

```bash
cd be
mvn -q -DskipTests package
```

JAR: `be/target/parking-booking-0.0.1-SNAPSHOT.jar` (tên có thể đổi theo `pom.xml`).

## Biến môi trường (production — ví dụ)

| Biến | Ghi chú |
|------|--------|
| `DATABASE_URL` | JDBC `jdbc:mysql://HOST:3306/DB?...` |
| `DATABASE_USER`, `DATABASE_PASSWORD` | User chỉ thao tác trên schema ứng dụng |
| `JPA_DDL_AUTO` | Khuyến nghị `validate` — không `create-drop` trên DB có dữ liệu |
| `SQL_INIT_MODE` | Thường `never` sau lần provision (tránh chạy lại `data.sql` tự động) |
| `SPRING_PROFILES_ACTIVE` | Tùy: `prod` nếu dùng `application-prod.yml` |
| `APP_PUBLIC_BASE_URL` | URL công khai (HTTPS) — Stripe redirect |
| `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET` | Thanh toán thẻ + webhook `POST /payment/webhook` |

## Chạy

```bash
export SPRING_PROFILES_ACTIVE=prod
export JPA_DDL_AUTO=validate
export SQL_INIT_MODE=never
java -jar be/target/parking-booking-0.0.1-SNAPSHOT.jar
```

**Local / dev:** xem `docs/chạy.md`.

## Tài liệu liên quan

- `README.md` — Stripe, cấu hình tổng quan
- `docs/chạy.md` — MySQL, profile `local`, chạy `mvn spring-boot:run`
- `docs/incident-runbook.md` — xử lý sự cố nhanh
