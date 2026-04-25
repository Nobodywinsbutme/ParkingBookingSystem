# Hướng dẫn chạy project (MySQL → Spring Boot)

## Bước 1 — Bật MySQL

Đảm bảo **MySQL đang chạy** (Windows Service, XAMPP, v.v.).

---

## Bước 2 — Tạo lại database `smart_parking`

Mở **MySQL Workbench** (hoặc `mysql` CLI), đăng nhập bằng user có quyền tạo DB (thường `root`).

Chạy:

```sql
CREATE DATABASE smart_parking
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

*(Bạn đã `DROP` rồi thì không cần `DROP` lại nếu chắc DB đã mất.)*

**Kiểm tra:** trong Workbench chọn schema `smart_parking` — database trống, chưa có bảng là đúng.

---

## Bước 3 — Cho Spring kết nối đúng user/password

App mặc định (trong `application.yml`) dùng:

- **Host:** `localhost`, **port:** `3306`
- **Database:** `smart_parking`
- **User:** `root`
- **Password:** `root`

Nếu MySQL của bạn **khác** (ví dụ mật khẩu không phải `root`):

### Cách A — File `application-local.yml` (khuyến nghị)

Trong `be/src/main/resources/`:

1. Copy `application-local.yml.example` → `application-local.yml`
2. Sửa `spring.datasource.username` / `password` đúng với MySQL của bạn.

### Cách B — Biến môi trường PowerShell (trước khi chạy Maven)

```powershell
$env:DATABASE_USER = "root"
$env:DATABASE_PASSWORD = "mat_khau_that_cua_ban"
```

*(Nếu dùng file `application-local.yml`, bước 4 cần bật profile `local`.)*

### (Tùy chọn) Stripe Checkout + webhook

Để thử thanh toán Stripe trên máy local, cần thêm biến môi trường (hoặc `application-local.yml` nếu project map các key này):

- `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET`
- `APP_PUBLIC_BASE_URL` — URL công khai mà Stripe redirect về (thường dùng tunnel như ngrok nếu test webhook)

Chi tiết triển khai: `docs/deployment-guide.md`.

---

## Bước 4 — Chạy Spring Boot (tự tạo bảng + seed)

Mở **PowerShell**:

```powershell
cd c:\ParkingBookingSystem\be
```

Nếu dùng **`application-local.yml`**:

```powershell
$env:SPRING_PROFILES_ACTIVE = "local"
```

Chạy app:

```powershell
mvn spring-boot:run
```

*(Hoặc từ thư mục gốc repo: `npm run be:run` — cùng lệnh Maven qua `package.json`.)*

### Điều gì xảy ra lúc khởi động

1. Spring kết nối JDBC tới `smart_parking`.
2. Vì `spring.sql.init.mode` mặc định là **`always`**, nó chạy:
   - **`schema.sql`** → `CREATE TABLE IF NOT EXISTS ...` (bảng tối giản: users, parking_area, parking_slot, booking, payment)
   - **`data.sql`** → `INSERT IGNORE` 2 khu đỗ + 3 ô demo.
3. Hibernate **`ddl-auto: validate`** — chỉ **kiểm tra** entity khớp bảng, **không** tự xóa/tạo lại bảng.

*(Khi `mvn compile` / chạy app, Maven copy resource từ `be/src/main/resources` sang `be/target/classes/` — nội dung giống bản nguồn; **chỉ chỉnh file trong `src/main/resources`**, không sửa tay trong `target`.)*

Đợi log có **`Started Application`** → mở trình duyệt: **http://localhost:8080**

---

## Bước 5 — Kiểm tra nhanh

- Trang chủ load được.
- Đăng ký / đăng nhập (nếu cần).
- **Parking areas** có 2 bãi demo.
- Trong Workbench: mở `smart_parking` → thấy các bảng và vài dòng seed trong `parking_area`, `parking_slot`.

---

## Lỗi thường gặp

| Triệu chứng | Hướng xử lý |
|-------------|-------------|
| `Unknown database 'smart_parking'` | Chưa tạo DB — làm lại Bước 2. |
| `Access denied for user ...` | Sai user/password — Bước 3. |
| `Communications link failure` | MySQL chưa bật hoặc sai host/port. |
| Schema-validation / missing table | DB trống nhưng `sql.init` tắt — kiểm tra không đặt `SQL_INIT_MODE=never` trừ khi bạn cố ý. |

---

## Tóm tắt

**Tạo DB `smart_parking` → chỉnh user/pass khớp MySQL → `cd be` → (optional) `SPRING_PROFILES_ACTIVE=local` → `mvn spring-boot:run` → Spring tự chạy `schema.sql` + `data.sql` → mở http://localhost:8080.**

---

## (Tuỳ chọn) API demo môn DB — truy vấn JOIN / EXISTS / GROUP BY

Sau khi app chạy, có thể gọi JSON (không cần đăng nhập) để thử mẫu truy vấn trong `BookingRepository` + `QueryDemoRestController`:

| `GET` | Mô tả ngắn |
|-------|------------|
| `http://localhost:8080/api/demo/queries/bookings/join` | Kết quả **JOIN** booking + user + slot + area |
| `http://localhost:8080/api/demo/queries/bookings/stats` | Thống kê **GROUP BY** theo trạng thái và theo bãi |
| `.../bookings/overlap-check?slotId=...&startAt=...&endAt=...` | So **COUNT (JPQL)** vs **EXISTS (SQL)** cho trùng lịch; thời gian dạng ISO-8601 (ví dụ `2025-12-01T00:00:00Z`) |

Chi tiết câu lệnh và nội dung slide: xem **`docs/sql.md`** (cùng thư mục `docs/` với file này; từ gốc repo: `docs/sql.md`).

---

## Tài liệu liên quan

- `README.md` — tổng quan repo
- `docs/sql.md` — slide + tham chiếu file code (JPQL / native)
- `docs/he-thong-fe-be-db-flow.md` — luồng FE/BE/DB
- `docs/deployment-guide.md` — biến môi trường production (Stripe, JDBC, …)
- `docs/incident-runbook.md` — xử lý sự cố thường gặp
