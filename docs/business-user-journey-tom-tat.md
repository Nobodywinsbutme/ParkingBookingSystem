# Smart Parking — Hành trình nghiệp vụ (phiên bản hiện tại)

Tóm tắt giá trị sản phẩm và luồng sử dụng theo **codebase hiện tại**: một ứng dụng **Spring Boot + JSP + MySQL**, không có Next.js, Prisma, PayOS hay module subscription trong repo.

---

## 1) Mục tiêu sản phẩm

**Người dùng cuối**

- Xem danh sách bãi đỗ đang hoạt động.
- Đặt chỗ theo slot và khung giờ, với kiểm tra trùng lịch (slot và theo user).
- Xem danh sách booking của mình; hủy khi nghiệp vụ cho phép.
- Thanh toán trong DB (`payment`): **CASH** (xác nhận nội bộ) hoặc **STRIPE** khi cấu hình `STRIPE_SECRET_KEY` / `APP_PUBLIC_BASE_URL` — redirect **Stripe Checkout**, cập nhật trạng thái qua **webhook** (`POST /payment/webhook`).

**Vận hành**

- Tài khoản **ADMIN** truy cập khu vực `/admin` (mức tối thiểu tùy triển khai).
- Dữ liệu bãi/slot/booking tập trung trên **MySQL**, chỉ Spring/JPA truy cập.

---

## 2) Hành trình người dùng (User)

### Bước 1 — Khám phá

- Vào `/` (trang chủ — hero, giới thiệu nhanh, CTA tới bãi đỗ).

### Bước 2 — Chọn bãi

- Vào `/parking-areas`, chọn bãi còn hoạt động.

### Bước 3 — Đặt chỗ

- Đăng nhập nếu chưa có session (`/login`).
- `/bookings/new?areaId=...` — chọn slot, nhập `start` / `end`, ghi chú tùy chọn; trang có thể gọi API availability và lắng nghe **WebSocket** (`/ws`, topic `/topic/slots`) để cập nhật ô trống theo thời gian thực.
- POST `/bookings` — hệ thống kiểm tra:
  - thời gian hợp lệ;
  - slot **active** và trạng thái slot phù hợp (`AVAILABLE`, …);
  - không overlap booking **PENDING/CONFIRMED** (cùng slot hoặc cùng user tùy luật service).
- Nếu chọn **STRIPE** và cổng đã cấu hình: redirect sang Stripe; sau khi thanh toán, webhook đồng bộ `payment` và có thể xác nhận booking.

### Bước 4 — Sau đặt chỗ

- `/bookings` — theo dõi trạng thái; có thể hủy (POST) trong điều kiện cho phép.
- `/dashboard` — tổng quan nhanh sau đăng nhập (mặc định redirect từ login).

---

## 3) Hành trình quản trị (Admin)

- Đăng nhập bằng tài khoản có role **ADMIN** (ví dụ seed `admin@example.com` trong môi trường dev).
- Truy cập `/admin` để các thao tác quản trị được triển khai trong `AdminMvcController` / view tương ứng.

---

## 4) Giá trị kinh doanh (rút gọn)

| Đối tượng | Lợi ích |
|-----------|---------|
| Khách hàng | Đặt chỗ có ràng buộc thời gian, giảm nguy cơ double-booking ở tầng ứng dụng + index hỗ trợ truy vấn. |
| Vận hành | Một nguồn dữ liệu MySQL, MVC rõ ràng, dễ bảo trì cho bài lab / đồ án. |

---

## 5) KPI gợi ý (nếu mở rộng)

- Tỷ lệ đặt chỗ thành công / số lần thử (validation fail, conflict).
- Tỷ lệ hủy booking.
- Mức sử dụng slot theo khung giờ (cần thêm báo cáo hoặc export).

---

## 6) Tài liệu liên quan

- `docs/chạy.md` — chạy local
- `docs/sql.md` (thuyết trình / truy vấn mẫu môn DB; API demo JSON)
- `he-thong-fe-be-db-flow.md`
- `erd-smart-parking.md`
- `docs/deployment-guide.md` (biến môi trường Stripe, JAR)
- `README.md`
