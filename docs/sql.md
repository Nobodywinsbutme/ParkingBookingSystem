# Smart Parking — Database & SQL Layer (Presentation)

Bộ nội dung slide (PowerPoint / Google Slides), sẵn sàng copy. Thay **[Your Name]** bằng tên thật.

## Tham chiếu mã nguồn (trong `be/`)

| Mẫu / API | Vị trí |
|-----------|--------|
| JPQL **JOIN** (booking + user + slot + area) | `be/src/main/java/com/app/repository/BookingRepository.java` — `findAllWithUserSlotAreaJoin` |
| **COUNT (JPQL)** trùng lịch | Cùng file — `existsOverlappingBooking` (query `count(b) > 0`); `overlapsUsingCountJpql` ủy quyền tới phương thức này |
| **EXISTS** (native MySQL) | Cùng file — `existsOverlappingBookingNativeEx` |
| **GROUP BY** theo trạng thái / theo bãi | Cùng file — `countBookingsGroupByStatus`, `countBookingsGroupByParkingArea` |
| DTO `select new` | `be/src/main/java/com/app/dto/demo/` (`BookingJoinRow`, `BookingStatusCountRow`, `BookingAreaCountRow`, …) |
| JSON demo (read-only) | `be/src/main/java/com/app/web/QueryDemoRestController.java` — base path `/api/demo/queries` (Spring Security: `permitAll`; xem `SecurityConfig`) |

Cách chạy nhanh: `docs/chạy.md`.

---

## Slide 1 — Title

**Title:** Smart Parking — Database & SQL Layer

**Bullet points**

- Bài thuyết trình: Môn Cơ sở dữ liệu / Đồ án
- Dự án: **Hệ thống đặt chỗ bãi đỗ xe (Parking Booking System)**
- **Presenter:** [Your Name]

---

## Slide 2 — Introduction

**Title:** Vấn đề & mục tiêu hệ thống

**Bullet points**

- **Vấn đề:** Tài xế cần đặt trước ô đỗ, tránh trùng giờ; quản lý cần dữ liệu nhất quán, thanh toán gắn với booking
- **Giải pháp:** Ứng dụng web (Spring Boot + JPA + **MySQL**)
- **Trọng tâm DB:** lưu user, bãi, ô đỗ, **booking theo thời gian**, thanh toán, kiểm tra **chồng lịch** và (tuỳ chế độ) **xóa mềm** qua cột `deleted_at`

---

## Slide 3 — Database Overview

**Title:** Tổng quan 5 bảng

**Bullet points**

| Bảng | Vai trò ngắn gọn |
|------|------------------|
| **users** | Tài khoản, email đăng nhập |
| **parking_area** | Tên bãi, địa chỉ, thành phố |
| **parking_slot** | Từng ô đỗ (mã, tầng), thuộc một bãi |
| **booking** | Khung thời gian đặt, trạng thái, liên kết user + bãi + slot |
| **payment** | Thanh toán gắn 1-1 với booking (thể hiện bằng ràng buộc **UNIQUE**) |

- **Soft delete:** bảng dùng `deleted_at` — truy vấn thường lọc `deleted_at IS NULL`

---

## Slide 4 — ERD Explanation

**Title:** Mối quan hệ (ERD)

**Bullet points**

- **User → Booking:** mỗi user có nhiều booking; booking thuộc **một** user (`user_id` → `users.id`)
- **Area → Slot:** một **parking_area** có nhiều **parking_slot** (`parking_area_id` → `parking_area.id`)
- **Slot → Booking:** một slot có nhiều booking theo thời gian (lịch); booking trỏ **một** slot (`parking_slot_id`)
- **Booking → Payment:** theo nghiệp vụ **1 booking = 1 bản ghi thanh toán** (đảm bảo bằng **UNIQUE** trên `booking_id`)

*Gợi ý hình: vẽ 4 cạnh: users–booking, area–slot, slot–booking, booking–payment.*

---

## Slide 5 — Constraints

**Title:** Ràng buộc trong schema

**Bullet points**

- **Primary Key (PK):** mỗi bảng có `id` (VARCHAR) — định danh duy nhất hàng
- **Foreign Key (FK):** ví dụ `booking` → `users`, `parking_area`, `parking_slot`; `payment` → `booking` — tham chiếu tồn tại, giữ toàn vẹn quan hệ
- **UNIQUE:** mỗi cặp **(bãi + mã slot)** trên `parking_slot` không trùng; **(booking_id)** trên `payment` — 1 thanh toán/1 booking
- **ENUM:** trạng thái hạn chế hợp lệ: ví dụ booking: `PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED`; slot: `AVAILABLE`, `BOOKED`, `MAINTENANCE`

Ví dụ gợi nhớ: `UNIQUE (booking_id)` trên bảng `payment`.

---

## Slide 6 — Schema Example (simplified)

**Title:** Ví dụ: bảng `booking` (rút gọn)

**Bullet points**

- Mục: minh hoạ **PK**, **FK**, kiểu thời gian, **ENUM** `status`, **soft delete**

**Code block**

```sql
CREATE TABLE IF NOT EXISTS booking (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    user_id VARCHAR(32) NOT NULL,
    parking_area_id VARCHAR(32) NOT NULL,
    parking_slot_id VARCHAR(32) NOT NULL,
    start_at DATETIME(6) NOT NULL,
    end_at DATETIME(6) NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED') NOT NULL,
    ...
    deleted_at DATETIME(6) NULL,
    CONSTRAINT fk_booking_user   FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_booking_area   FOREIGN KEY (parking_area_id) REFERENCES parking_area (id),
    CONSTRAINT fk_booking_slot  FOREIGN KEY (parking_slot_id) REFERENCES parking_slot (id)
);
```

---

## Slide 7 — JOIN Query

**Title:** Nối bảng: Booking + User + Slot + Area (JPQL)

**Bullet points**

- Mục đích: **một câu truy vấn** lấy thông tin booking kèm **email**, **mã slot**, **tên bãi**, **start/end**
- Dùng **JOIN** rõ ràng: `UserEntity` ← `BookingEntity` → `ParkingSlotEntity` + `ParkingAreaEntity`
- Chỉ booking chưa xóa mềm: `b.deletedAt is null`
- Từ JPQL, Hibernate tạo SQL tương ứng với **INNER JOIN**

**Code block (trích project)**

```text
select new ...BookingJoinRow(
    b.id, u.email, s.code, a.name, b.startAt, b.endAt
)
from BookingEntity b
    join UserEntity u on b.userId = u.id
    join ParkingSlotEntity s on b.parkingSlotId = s.id
    join ParkingAreaEntity a on b.parkingAreaId = a.id
where b.deletedAt is null
order by b.createdAt desc
```

*Highlight:* **JOIN**, `where` (lọc soft delete), `order by`

---

## Slide 8 — EXISTS Query (overlap)

**Title:** Trùng lịch & native **EXISTS**

**Bullet points**

- Cần biết: có **đặt nào đang hiệu lực** (ví dụ `PENDING`, `CONFIRMED`) trùng **khoảng thời gian** mới trên cùng **slot** không
- Giao hai khoảng thời gian **(start, end)** trùng nhau khi:  
  **`existing.start < newEnd` VÀ `existing.end > newStart`** (trong code: so với tham số `:startAt` / `:endAt`)
- Câu native SQL dùng **`EXISTS (SELECT 1 …)`** — dừng sớm khi tìm thấy một dòng thỏa, phù hợp bài tập/seminar

**Code block (native — trích project)**

```sql
SELECT EXISTS(
    SELECT 1
    FROM booking b
    WHERE b.parking_slot_id = :slotId
      AND b.deleted_at IS NULL
      AND b.status IN ('PENDING', 'CONFIRMED')
      AND b.start_at < :endAt
      AND b.end_at > :startAt
)
```

*Highlight:* **EXISTS**, điều kiện **khoảng thời gian**, lọc `deleted_at` và `status`

---

## Slide 9 — GROUP BY Query

**Title:** Thống kê: theo trạng thái & theo bãi

**Bullet points**

- **Theo trạng thái:** `GROUP BY` `status` — đếm số booking (bản ghi chưa xóa mềm)
- **Theo bãi:** `JOIN` lấy tên bãi, `GROUP BY` `parkingAreaId` + tên bãi, `count(b)`

**Code block 1 — by status (JPQL)**

```text
select new ...BookingStatusCountRow(b.status, count(b))
from BookingEntity b
where b.deletedAt is null
group by b.status
order by b.status
```

**Code block 2 — by area (JPQL)**

```text
select new ...BookingAreaCountRow(b.parkingAreaId, a.name, count(b))
from BookingEntity b
    join ParkingAreaEntity a on b.parkingAreaId = a.id
where b.deletedAt is null
group by b.parkingAreaId, a.name
order by a.name
```

*Highlight:* **GROUP BY**, `count(…)`, (lần 2) kết hợp **JOIN** + thống kê

---

## Slide 10 — Demo APIs

**Title:** API demo (JSON) — kiểm tra nhanh

**Bullet points**

| Phương thức & đường dẫn | Nội dung trả về (ý tưởng) |
|------------------------|----------------------------|
| `GET` **`/api/demo/queries/bookings/join`** | Danh sách booking với user email, mã slot, tên bãi, thời gian (kết quả **JOIN**) |
| `GET` **`/api/demo/queries/bookings/overlap-check`** + query `slotId`, `startAt`, `endAt` (ISO-8601) | So sánh cùng rule trùng lịch: bản **JPQL count** vs **native EXISTS** (boolean) |
| `GET` **`/api/demo/queries/bookings/stats`** | Thống kê **GROUP BY** theo `status` và theo `parking area` |

- **Gợi ý khi thuyết:** mở trình duyệt, gọi 3 URL (localhost + port app), chụp màn hình cho slide phụ nếu cần

**Code (ví dụ URL / overlap)**

```text
GET /api/demo/queries/bookings/overlap-check
    ?slotId=slot_demo_01
    &startAt=2025-12-01T00:00:00Z
    &endAt=2025-12-01T23:59:59Z
```

---

## Slide 11 — Key Strengths

**Title:** Điểm mạnh tầng dữ liệu

**Bullet points**

- **Toàn vẹn dữ liệu:** PK/FK/UNIQUE/ENUM hạn chế dữ liệu xấu, quan hệ rõ
- **Không (hoặc hạn chế) trùng lịch:** logic **khoảng thời gian** + trạng thái; có thể minh hoạ cả **COUNT** lẫn **EXISTS**
- **Truy vấn có mục đích:** **JOIN** cho báo cáo/ghép thông tin; **GROUP BY** cho dashboard thống kê
- **Mở rộng:** index theo nhu cầu (ví dụ cột thời gian/slot) — nếu giảng viên hỏi tối ưu

---

## Slide 12 — Conclusion

**Title:** Tóm tắt & hướng phát triển

**Bullet points**

- **Tóm tắt:** Schema 5 bảng, ràng buộc rõ, nghiệp vụ **booking theo thời gian** + **thanh toán 1-1**; trình bày thực tế **JOIN**, **EXISTS**, **GROUP BY** trong project
- **Cải tiến gợi ý (15–20 giây nói thêm):** soft delete thống nhất qua mọi luồng; báo cáo theo tháng; cache đọc cho thống kê; bài test tích hợp cho trùng lịch

---

## Gợi ý trình chiếu (~15 phút)

- Khoảng **1:00** / slide (slide 1–2, 12 nhanh hơn; slide 7–9 chi tiết hơn).
- Giữ mỗi slide **tối đa 5–6 bullet** khi dán vào PPT; code thu gọn 1 cột, font monospace **nhỏ vừa phải**.
- Thay “[Your Name]” ở Slide 1; có thể thêm logo trường / mã lớp ở slide đầu nếu yêu cầu.
