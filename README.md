# Smart Parking Booking System

Ứng dụng web đặt chỗ bãi đỗ xe — **một ứng dụng Spring Boot** (giao diện JSP + HTML/CSS/JS thuần), kết nối **MySQL** qua **JPA/Hibernate**.

## Cấu trúc thư mục

| Đường dẫn | Mô tả |
|-----------|--------|
| `be/` | Toàn bộ mã nguồn: Spring Boot 3, MVC (`@Controller` trong `com.app.web`), JSP, static assets, JPA |
| `be/target/` | **Output Maven** (`.jar`, bản copy `schema.sql` / `application.yml` vào `classes/`) — không chỉnh tay; `mvn clean` để xóa |
| `docs/` | Kiến trúc, ERD, `chạy.md`, `sql.md`, triển khai, runbook (tất cả dưới `docs/`) |
| `package.json` (gốc) | Script tiện gọi Maven (`npm run be:run`, `npm run build`, …) — không có app Node/`fe/` |

**Lưu ý:** Không còn frontend Next.js/Prisma trong repo; mọi truy cập DB đi qua `be/`.

## Công nghệ

- **Java 17**, **Spring Boot 3.3** (Web MVC, Security, Validation, Data JPA)
- **JSP** + **JSTL** + **Spring Security taglibs** (`WEB-INF/views/`)
- **CSS/JS** tĩnh: `be/src/main/resources/static/`
- **MySQL 8+** (chuỗi JDBC trong `application.yml`)
- **BCrypt** (đăng ký/đăng nhập), phiên **HttpSession** qua Spring Security form login
- **Bootstrap 5** + **Bootstrap Icons** (CDN), **`app.css`** / **`app-ui.js`** (toast, UX), **SockJS + STOMP** cho cập nhật slot theo thời gian thực
- **Stripe Checkout** (thẻ) — webhook tùy chọn để đồng bộ sau redirect

### Stripe (local / production)

1. Tạo tài khoản [Stripe](https://stripe.com), lấy **Secret key** (test `sk_test_...`).
2. Đặt biến môi trường trước khi chạy app:
   - `STRIPE_SECRET_KEY` — bắt buộc để bật thanh toán thẻ
   - `STRIPE_WEBHOOK_SECRET` — `whsec_...` (CLI hoặc Dashboard → Webhooks) cho endpoint `POST /payment/webhook`
3. Đặt `APP_PUBLIC_BASE_URL` đúng URL công khai của app (ví dụ `http://localhost:8080` khi dev) — Stripe dùng cho `success_url` / `cancel_url`.
4. Stripe CLI (test webhook): `stripe listen --forward-to localhost:8080/payment/webhook` rồi dán signing secret vào `STRIPE_WEBHOOK_SECRET`.

Số tiền booking dùng **USD** và **đơn vị nhỏ nhất** (cents), khớp Stripe `unit_amount`.

## Cấu hình & chạy local

Hướng dẫn từng bước (MySQL rỗng → chạy app): xem **`docs/chạy.md`**.

1. Tạo database (ví dụ `smart_parking`) và cấp quyền user MySQL.
2. (Khuyến nghị) Sao chép `be/src/main/resources/application-local.yml.example` → `application-local.yml`, điền `spring.datasource.username` / `password`.
3. Chạy (PowerShell):

```powershell
cd be
$env:SPRING_PROFILES_ACTIVE = 'local'   # nếu dùng application-local.yml
mvn spring-boot:run
```

*(Tùy chọn từ thư mục gốc repo: `npm run be:run` — gọi Maven tương đương.)*

4. Mở **http://localhost:8080** (cổng mặc định `server.port`).

Schema + seed mẫu: `be/src/main/resources/schema.sql`, `data.sql` (chạy khi `spring.sql.init.mode` bật — xem `application.yml`).

Tài khoản admin demo (profile khác `prod`): **admin@example.com** / **Admin1234!** — xem `AdminUserSeed`.

## Luồng HTTP chính (MVC)

- `/` — trang chủ  
- `/login`, `/register` — xác thực  
- `/parking-areas` — danh sách bãi  
- `/dashboard` — tổng quan (số booking của user, slot AVAILABLE toàn hệ thống)
- `/bookings`, `/bookings/new` — đặt chỗ (cần đăng nhập)  
- `/payment/checkout`, `/payment/success`, `/payment/cancel`, `/payment/webhook` — Stripe  
- `/api/parking-areas/{id}/slots` — JSON availability (theo `startAt` / `endAt`)  
- `/api/demo/queries/...` — JSON demo môn DB (**JOIN** / **EXISTS** / **GROUP BY**; không dùng cho core business) — xem `docs/sql.md`  
- `/ws` — SockJS endpoint STOMP (topic `/topic/slots`)  
- `/admin`, `/admin/home` — bảng điều khiển admin  
- `/admin/areas`, `/admin/slots` — CRUD bãi / ô (delete = tắt `is_active`)  
- `/admin/users` — danh sách user (chỉ xem)  

## Kiểm tra chất lượng (backend)

```bash
cd be
mvn -q test          # hiện có thể chạy 0 test nếu chưa thêm lại `src/test/java`
mvn -q -DskipTests compile
```

Hoặc từ gốc repo: `npm test` / `npm run build`.

## Tài liệu (index)

| Tài liệu | Mục đích |
|----------|----------|
| **`docs/chạy.md`** | Chạy local: MySQL, profile `local`, `schema.sql` / `data.sql` |
| **`AGENTS.md`** (gốc repo) | Ghi chú cho contributor (stack, bảo trì) |
| **`docs/sql.md`** | Slide + giải thích môn DB: **JOIN**, **EXISTS**, **GROUP BY**; tham chiếu `BookingRepository` / `QueryDemoRestController` |
| `docs/he-thong-fe-be-db-flow.md` | Luồng Web → Spring → DB |
| `docs/luong-ket-noi-fe-be-db-chi-tiet.md` | Kết nối chi tiết, API + WebSocket + CSRF |
| `docs/erd-smart-parking.md` | ERD / DBML khớp `schema.sql` |
| `docs/business-user-journey-tom-tat.md` | Hành trình người dùng |
| `docs/deployment-guide.md` | JAR, biến môi trường, MySQL |
| `docs/incident-runbook.md` | Runbook sự cố |
| `docs/principles-of-database-management-project-report.md` | Báo cáo / đề cương môn PDM |
| `package.json` (gốc) | Script `npm run be:run`, `build`, … (wrap Maven) |
