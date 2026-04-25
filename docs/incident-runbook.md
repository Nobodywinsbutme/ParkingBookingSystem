# Runbook — Xử lý sự cố (Spring Boot + MySQL)

Hướng dẫn nhanh khi app hoặc DB lỗi. Stack: **một** Spring Boot + **MySQL** (không Node/Prisma).

## Checklist 10 phút

1. Xác minh **MySQL** đang chạy và database (vd. `smart_parking`) tồn tại.
2. So khớp `DATABASE_USER` / `DATABASE_PASSWORD` với `application.yml` / `application-local.yml`.
3. `JPA_DDL_AUTO=validate`: cấu trúc bảng phải khớp `be/src/main/resources/schema.sql` và entity.
4. Cổng `server.port` (mặc định 8080) không bị chiếm.
5. Lỗi vòng **redirect** đăng nhập khi xem JSP: đã cấu hình `DispatcherType.FORWARD` permit trong `SecurityConfig` (xem `docs/luong-ket-noi-fe-be-db-chi-tiet.md`).

## Triệu chứng thường gặp

- **Lỗi kết nối MySQL:** host/port, firewall, quyền user.
- **Schema validation failed:** chạy lại/đồng bộ `schema.sql` trên MySQL, hoặc tạo DB mới theo `docs/chạy.md`.
- **403 CSRF:** form thiếu token; ngoại lệ: webhook Stripe, SockJS theo cấu hình.
- **Admin không vào `/admin`:** tài khoản phải có role `ADMIN` (xem `AdminUserSeed` / seed).

## Tài liệu liên quan

- `docs/chạy.md` — thứ tự chạy local
- `docs/deployment-guide.md` — JAR, biến môi trường
- `docs/luong-ket-noi-fe-be-db-chi-tiet.md` — kết nối, session, API
- `README.md`
