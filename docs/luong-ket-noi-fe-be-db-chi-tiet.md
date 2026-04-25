# Luồng kết nối chi tiết: Trình duyệt — Spring Boot — MySQL

Mô tả cách **một** ứng dụng Spring Boot phục vụ JSP và truy cập **MySQL** qua JPA — phù hợp môn **Web Application Development** (MVC, session, ORM).

Tổng quan ngắn: `he-thong-fe-be-db-flow.md`.

---

## 1) Sơ đồ tổng thể

```mermaid
flowchart TB
  subgraph client[Máy người dùng]
    B[Trình duyệt]
  end

  subgraph app[Một tiến trình Spring Boot]
    DS[DispatcherServlet]
    C[@Controller]
    S[Service]
    REP[Repository JPA / Hibernate]
  end

  subgraph db[MySQL]
    MY[(smart_parking)]
  end

  B -->|GET/POST cùng origin ví dụ :8080| DS
  DS --> C
  C --> S
  S --> REP
  REP --> MY
```

**Ý chính**

- Không có lớp Node/Next hay proxy `/api` bắt buộc: trình duyệt gọi trực tiếp **Tomcat nhúng** (cùng origin).
- Session đăng nhập: cookie **JSESSIONID** + **Spring Security** lưu `SecurityContext` trong **HttpSession**.

**API & realtime (cùng origin, cùng app):**

- `GET /api/parking-areas/{areaId}/slots?startAt=&endAt=` — JSON availability (cần đăng nhập).
- `GET /api/demo/queries/bookings/join` | `.../stats` | `.../overlap-check?...` — JSON mẫu môn DB (**JOIN** / **GROUP BY** / overlap **COUNT** vs **EXISTS**); `permitAll` (xem `docs/sql.md` + `QueryDemoRestController`).
- SockJS + STOMP: endpoint `/ws`, subscribe topic **`/topic/slots`** để trang đặt chỗ refetch availability khi có booking mới/hủy.

---

## 2) View layer (JSP)

- View resolver: `spring.mvc.view.prefix=/WEB-INF/views/`, `suffix=.jsp`
- **UI:** Bootstrap 5 + Bootstrap Icons (CDN trong `common/head-bootstrap.jspf`), stylesheet `static/css/app.css`, toast/helper `static/js/app-ui.js` (sau `bootstrap.bundle` trong `footer.jspf`).
- Form đăng nhập POST tới `/login` (Spring Security xử lý); đăng ký POST tới `/register` (`AuthMvcController`).
- CSRF: `<sec:csrfInput/>` trong form (taglib `spring-security-taglibs`).

**Lưu ý bảo mật:** Đã cấu hình `permitAll` cho `DispatcherType.FORWARD` (và ERROR) để forward nội bộ tới file JSP không bị vòng redirect đăng nhập. CSRF **bỏ qua** cho `POST /payment/webhook` và `/ws/**` (Stripe webhook + SockJS handshake).

---

## 3) Kết nối cơ sở dữ liệu

**File:** `be/src/main/resources/application.yml`

- `spring.datasource.url` — JDBC MySQL, ví dụ  
  `jdbc:mysql://localhost:3306/smart_parking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=utf8`
- `spring.datasource.username` / `password`
- JPA: dialect MySQL, `ddl-auto` thường `validate`; khởi tạo script: `schema.sql`, `data.sql` khi bật `sql.init`

**File cục bộ (không commit):** `application-local.yml` — override user/password MySQL.

---

## 4) Schema & migration

- **Nguồn chân lý cấu trúc bảng hiện tại:** `be/src/main/resources/schema.sql` + entity JPA (tên cột `snake_case`).
- **Seed mẫu:** `data.sql`
- **Nâng cấp DB đã tồn tại:** so sánh với `schema.sql` và chạy các lệnh `ALTER`/`UPDATE` cần thiết trên MySQL, hoặc tạo lại database.

---

## 5) Checklist khi “không chạy được”

1. MySQL đã bật, database tồn tại, user/password đúng.
2. `SPRING_PROFILES_ACTIVE=local` nếu dùng `application-local.yml`.
3. Cổng 8080 không bị chiếm; đổi `SERVER_PORT` nếu cần.
4. `ddl-auto=validate`: schema DB phải khớp entity (đã chạy `schema.sql` / migration).
5. Cookie / session: trình duyệt chặn cookie third-party không áp dụng (same-origin).

---

## 6) Thứ tự chạy local gợi ý

1. Khởi động MySQL, tạo DB.
2. `cd be` → `mvn spring-boot:run` (kèm profile `local` nếu cần).
3. Mở `http://localhost:8080`.

---

## 7) Tài liệu liên quan

- `docs/chạy.md` — tạo DB và chạy app
- `docs/sql.md` (slide + mẫu truy vấn, API demo)
- `he-thong-fe-be-db-flow.md`
- `deployment-guide.md`
- `README.md`
