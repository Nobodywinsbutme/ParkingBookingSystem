# Principles of Database Management — Báo cáo / đề cương dự án

**Môn:** Nguyên lý Quản trị Cơ sở dữ liệu (tham chiếu khung S1 2025–26)

**Đề tài:** Smart Parking Booking System — hệ thống đặt chỗ bãi đỗ xe (web)

**Ngày:** *(điền)*

**Giảng viên:** *(điền)*

**Nhóm thực hiện — Contributors**

| STT | MSSV | Họ tên | Điện thoại | Vai trò |
|-----|------|--------|------------|---------|
| 1 | | | | *(Leader / Backend / …)* |
| 2 | | | | |
| … | | | | *(thêm dòng)* |

---

## Mục lục

1. [Project Idea (Ý tưởng dự án)](#1-project-idea-ý-tưởng-dự-án)
2. [Work Plan (Kế hoạch triển khai)](#2-work-plan-kế-hoạch-triển-khai)
   - [Requirement Analysis & Domain Modeling](#requirement-analysis--domain-modeling)
   - [Database Schema Design](#database-schema-design)
   - [Backend Development](#backend-development)
   - [Frontend Development](#frontend-development)
   - [Integration & Testing](#integration--testing)
   - [Deployment & Documentation](#deployment--documentation)
3. [Tools and Languages](#3-tools-and-languages)
4. [Current Progress (Tiến độ hiện tại — theo repo)](#4-current-progress-tiến-độ-hiện-tại--theo-repo)

---

## 1. Project Idea (Ý tưởng dự án)

- **Smart Parking** là nền tảng web gom các bước **xem bãi → chọn ô (slot) → chọn khung giờ → thanh toán (tiền mặt hoặc thẻ)** vào **một hệ thống tập trung**, thay cho quy trình rời rạc (gọi điện, giữ chỗ thủ công, double-booking).

- Hệ thống hỗ trợ vòng đời chính:
  - Đăng ký / đăng nhập, phân quyền **USER** và **ADMIN**
  - Danh sách **parking area** và **slot**; kiểm tra **trùng lịch** (cùng slot hoặc cùng user trong trạng thái đang giữ chỗ)
  - Tạo **booking** + một **payment** tối đa cho mỗi booking (ràng buộc DB)
  - **Stripe Checkout** (tùy cấu hình) + **webhook** đồng bộ trạng thái thanh toán; **CASH** xác nhận nội bộ
  - **WebSocket (SockJS + STOMP)** để làm mới **availability** slot trên trang đặt chỗ
  - **Dashboard** thống kê nhanh (số booking của user, slot AVAILABLE toàn hệ thống)

- Hệ thống hướng tới:
  - Giảm xung đột đặt chỗ nhờ kiểm tra overlap ở tầng dịch vụ + chỉ mục phục vụ truy vấn thời gian
  - Minh bạch trạng thái booking / thanh toán trong DB
  - Một **MySQL** làm nguồn dữ liệu chuẩn; truy cập qua **JPA/Hibernate**

- **Stakeholders (các bên liên quan)**

| Bên | Vai trò |
|-----|---------|
| Người dùng (khách) | Đặt chỗ, thanh toán, xem / hủy booking trong điều kiện cho phép |
| Quản trị | Truy cập `/admin` (mở rộng tùy môn học) |
| Giảng viên / nhóm | Đánh giá thiết kế DB, ORM, MVC, tính toàn vẹn dữ liệu |

---

## 2. Work Plan (Kế hoạch triển khai)

### Lịch theo tuần (gợi ý 12 tuần — chỉnh lại theo nhóm)

| Tuần | Công việc | Gợi ý phân công |
|------|-----------|------------------|
| 1 | Thu thập yêu cầu; xác định scope; actor & use case sơ bộ; phân vai | Cả nhóm |
| 2 | Sơ đồ use case; khái niệm miền (area, slot, booking, payment); ERD khái niệm | Thiết kế DB / báo cáo |
| 3 | Chuẩn hóa 1NF–3NF; bảng, PK/FK, ENUM; viết `schema.sql`; seed `data.sql` | Backend + DB |
| 4 | Khởi tạo Spring Boot; cấu hình MySQL; JPA entity/repository khớp schema | Backend |
| 5 | Spring Security (form login, BCrypt); đăng ký user; phân quyền ADMIN | Backend |
| 6 | `BookingService`: overlap slot/user; tạo/hủy booking; exception handler | Backend |
| 7 | Payment 1–1 booking; Stripe Checkout + webhook (tùy môn học); CASH | Backend |
| 8 | JSP: parking areas, form đặt chỗ, bookings, dashboard; Bootstrap 5 + static JS | Full-stack |
| 9 | REST availability; WebSocket broadcast khi booking thay đổi; tích hợp trang đặt chỗ | Backend + Frontend |
| 10 | Kiểm thử API / luồng tay; seed dữ liệu; kiểm tra ràng buộc & index | QA / Báo cáo |
| 11 | Tối ưu truy vấn; sửa lỗi tích hợp; hoàn thiện ERD & tài liệu | Cả nhóm |
| 12 | Đóng gói JAR; demo E2E; nộp báo cáo + slide | Cả nhóm |

### Requirement Analysis & Domain Modeling

- Vai trò: **USER** (đặt chỗ), **ADMIN** (`/admin/**`).
- Miền chính: **ParkingArea**, **ParkingSlot**, **Booking**, **Payment**, **User**.
- Luật nghiệp vụ cốt lõi:
  - Không overlap booking **PENDING** / **CONFIRMED** trên cùng slot hoặc cùng user (theo implementation hiện tại)
  - Slot phải **active**, trạng thái slot phù hợp (`AVAILABLE`, …)
  - Một booking tối đa **một** dòng payment (unique `booking_id`)
- Tài liệu bổ sung trong repo: `docs/business-user-journey-tom-tat.md`, `docs/he-thong-fe-be-db-flow.md`.

### Database Schema Design

- **RDBMS:** MySQL 8+ (InnoDB), charset khuyến nghị `utf8mb4`.
- **Thực thể chính (schema tối giản hiện tại):**

| Thực thể | Mô tả ngắn |
|----------|------------|
| `users` | Email unique, mật khẩu hash, `role`, `is_active` |
| `parking_area` | Bãi: tên, địa chỉ, city; soft-delete tùy `deleted_at` |
| `parking_slot` | Thuộc area; `code` unique trong area; `status` ENUM; soft-delete tùy |
| `booking` | User, area, slot, `start_at`/`end_at`, `status` ENUM, số tiền, ghi chú |
| `payment` | Gắn booking (unique); `method` STRIPE/CASH; `status` PENDING/PAID/… |

- **Ràng buộc & chỉ mục:** FK đầy đủ; `idx_booking_slot_time (parking_slot_id, start_at, end_at)` phục vụ overlap.
- **Nguồn chân lý file:** `be/src/main/resources/schema.sql`, seed `data.sql`.
- **ERD chi tiết:** `docs/erd-smart-parking.md`.

### Backend Development

- **Ngôn ngữ:** Java 17  
- **Framework:** Spring Boot 3.x (Web MVC, Security, Validation, Data JPA)  
- **Persistence:** Spring Data JPA / Hibernate (không dùng JDBC thuần cho CRUD chính)  
- **Điểm vào & package chính:** `com.app.Application`  
- **Lớp ứng dụng:**
  - `com.app.web.*` — MVC (`HomeController`, `AuthMvcController`, `BookingMvcController`, `PaymentMvcController`, `ParkingAvailabilityRestController`, …)
  - `com.app.service` — `BookingService`, `UserService`, `ParkingService`, `StripePaymentService`, `ParkingAvailabilityService`, …
  - `com.app.repository` — JPA repositories
  - `com.app.domain.entity`, `com.app.domain.enums` — map bảng / ENUM MySQL
  - `com.app.config` — Security, WebSocket, Stripe, …
- **API nội bộ (ví dụ):** `GET /api/parking-areas/{id}/slots?startAt=&endAt=` (JSON availability)  
- **Webhook:** `POST /payment/webhook` (Stripe)

### Frontend Development

- **Công nghệ:** JSP + JSTL + Spring Security taglibs; **Bootstrap 5** + **Bootstrap Icons** (CDN); CSS/JS tĩnh.
- **Thư mục view:** `be/src/main/webapp/WEB-INF/views/` (landing, login, register, parking areas, booking form, bookings, dashboard, admin, …).
- **Static:** `be/src/main/resources/static/css/app.css`, `js/app-ui.js`, `js/booking-live.js`, `js/validation.js`.
- **Trải nghiệm:** toast, bước trên form đặt chỗ, làm mới slot realtime qua STOMP.

### Integration & Testing

- **Tích hợp:** Stripe (Checkout + webhook); SockJS/STOMP cùng origin với app.
- **Kiểm thử gợi ý:** JUnit (có thể bổ sung `be/src/test/java`); thử tay luồng đăng ký → đặt chỗ → thanh toán / hủy; kiểm tra constraint DB.
- **Stripe test:** Stripe CLI / Dashboard; biến môi trường xem `README.md`, `docs/deployment-guide.md`.

### Deployment & Documentation

- Đóng gói: `mvn -f be/pom.xml package` → JAR (ví dụ `parking-booking-0.0.1-SNAPSHOT.jar`).
- Chạy production: `JPA_DDL_AUTO=validate`, `SQL_INIT_MODE` cân nhắc `never` khi DB đã có dữ liệu — xem `docs/deployment-guide.md`.
- Tài liệu repo: `README.md`, `docs/chạy.md`, `docs/incident-runbook.md`.

---

## 3. Tools and Languages

| Nhóm | Công cụ / công nghệ |
|------|---------------------|
| Ngôn ngữ | Java 17, SQL |
| Framework backend | Spring Boot 3, Spring Security, Spring Data JPA |
| CSDL | MySQL 8+ |
| View | JSP, HTML, CSS, JavaScript (vanilla) |
| UI | Bootstrap 5, Bootstrap Icons |
| Realtime | SockJS, STOMP |
| Thanh toán (tùy chọn) | Stripe Checkout + webhook |
| Build | Maven (`be/pom.xml`) |
| Version control | Git *(GitHub — điền URL repo nếu cần)* |
| Gợi ý thiết kế | ERD (DBML trong `docs/erd-smart-parking.md`), UML tùy môn học |

Shortcut từ gốc repo (nếu có `package.json`): `npm run be:run`, `npm run build`.

---

## 4. Current Progress (Tiến độ hiện tại — theo repo)

### Trạng thái tổng quan

Ứng dụng **monolith** trong thư mục **`be/`**: đã có luồng **đăng ký/đăng nhập**, **danh sách bãi**, **đặt chỗ** (kiểm tra overlap), **payment** (CASH / STRIPE), **dashboard**, **admin** tối thiểu, **WebSocket** cập nhật slot, **REST** availability; bổ sung **API JSON demo** (`/api/demo/queries/*`) và truy vấn minh hoạ **JOIN** / **EXISTS** / **GROUP BY** (xem `docs/sql.md` + `BookingRepository`). Schema và entity **đã khớp** `schema.sql` (phiên bản tối giản).

### Tech stack (thực tế)

- Spring Boot + **JSP** (không dùng JTE/Next.js trong repo này)
- MySQL + JPA/Hibernate
- Bootstrap 5 + custom `app.css`

### Cấu trúc thư mục chính (rút gọn)

**Java (`be/src/main/java/com/app/`):**

```text
Application.java
config/          # Security, WebSocket, Stripe, properties, seed admin
domain/
  entity/        # User, ParkingArea, ParkingSlot, Booking, Payment
  enums/         # BookingStatus, SlotStatus
dto/             # booking/, parking/ (JSON / API); demo/course: dto/demo/ (projections JOIN, GROUP BY)
repository/      # Spring Data JPA (có mẫu JOIN, EXISTS native, GROUP BY cho demo — `BookingRepository` phần coursework)
service/         # Booking, User, Parking, ParkingArea/Slot (admin), Stripe, availability, notifier
security/        # UserDetails, session
support/         # ApiException, GlobalExceptionHandler, …
web/             # *MvcController, Admin* (areas/slots/users), RestController, form/
```

**View:** `be/src/main/webapp/WEB-INF/views/` (JSP + `common/*.jspf`)

**Resource:** `be/src/main/resources/` — `application.yml`, `schema.sql`, `data.sql`, `static/css`, `static/js`

**Build output:** `be/target/` — biên dịch + bản copy resource *(không chỉnh tay; dùng `mvn clean` nếu cần)*

### Việc có thể làm tiếp (đề xuất báo cáo)

- Bổ sung **JUnit / integration test** dưới `be/src/test/java`
- Báo cáo occupancy, export
- Mở rộng **admin** (đã có CRUD bãi/slot, xem danh sách user)
- Sơ đồ UML (use case, sequence) nếu giảng viên yêu cầu — có thể đặt trong `docs/` hoặc phụ lục

---

## Tài liệu liên quan (trong repo)

- `docs/chạy.md` — chạy local MySQL + Spring Boot
- `docs/sql.md` — slide môn DB + tham chiếu JPQL / native + API demo truy vấn
- `docs/erd-smart-parking.md` — ERD / DBML  
- `docs/he-thong-fe-be-db-flow.md` — luồng Web → Spring → DB  
- `docs/deployment-guide.md` — triển khai JAR + biến môi trường  
- `README.md` — tổng quan kỹ thuật  

*Báo cáo này căn chỉnh **cấu trúc** theo mẫu đề cương PDM (Project Idea → Work Plan chi tiết → Tools → Current Progress) và nội dung **bám codebase Smart Parking** trong `be/`.*
