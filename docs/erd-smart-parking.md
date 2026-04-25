# ERD — Smart Parking (MySQL, phiên bản hiện tại)

Sơ đồ dưới đây **khớp** `be/src/main/resources/schema.sql` và entity JPA trong `be/`. Không còn bảng subscription, audit, promotion riêng trong schema tối giản cho môn WAD.

---

## DBML (tham chiếu nhanh)

```dbml
Table users {
  id varchar(32) [pk]
  email varchar(255) [unique, not null]
  password_hash varchar(255) [not null]
  role varchar(20) [not null]
  is_active boolean [not null, default: true]
  created_at datetime(6)
}

Table parking_area {
  id varchar(32) [pk]
  city varchar(255)
  name varchar(255) [not null]
  address_line1 varchar(512)
  is_active boolean [not null, default: true]
  deleted_at datetime(6)
  created_at datetime(6)
  updated_at datetime(6)
}

Table parking_slot {
  id varchar(32) [pk]
  parking_area_id varchar(32) [not null, ref: > parking_area.id]
  code varchar(64) [not null]
  floor varchar(32)
  status enum('AVAILABLE','BOOKED','MAINTENANCE') [not null]
  is_active boolean [not null, default: true]
  deleted_at datetime(6)
  created_at datetime(6)
  updated_at datetime(6)
  indexes {
    (parking_area_id, code) [unique, name: 'unique_slot_per_area']
  }
}

Table booking {
  id varchar(32) [pk]
  user_id varchar(32) [not null, ref: > users.id]
  parking_area_id varchar(32) [not null, ref: > parking_area.id]
  parking_slot_id varchar(32) [not null, ref: > parking_slot.id]
  start_at datetime(6) [not null]
  end_at datetime(6) [not null]
  checked_in_at datetime(6)
  checked_out_at datetime(6)
  status enum('PENDING','CONFIRMED','CANCELLED','COMPLETED') [not null]
  currency varchar(8) [not null]
  amount_subtotal int [not null]
  amount_total int [not null]
  notes text
  cancelled_at datetime(6)
  deleted_at datetime(6)
  created_at datetime(6)
  updated_at datetime(6)
  indexes {
    (parking_slot_id, start_at, end_at) [name: 'idx_booking_slot_time']
  }
}

Table payment {
  id varchar(32) [pk]
  booking_id varchar(32) [not null, ref: > booking.id, unique]
  method varchar(32) [not null]
  status varchar(32) [not null]
  amount int [not null]
  currency varchar(8) [not null]
  provider_txn_ref varchar(255)
  paid_at datetime(6)
  refunded_at datetime(6)
  deleted_at datetime(6)
  created_at datetime(6)
  updated_at datetime(6)
}
```

---

## Quan hệ (tóm tắt)

| Quan hệ | Ý nghĩa |
|---------|---------|
| `parking_area` 1 — N `parking_slot` | Mỗi bãi có nhiều ô; `(parking_area_id, code)` **duy nhất**. |
| `users` 1 — N `booking` | Một user nhiều booking. |
| `booking` N — 1 `parking_slot` | Mỗi booking gắn một slot. |
| `booking` N — 1 `parking_area` | Denormalize `parking_area_id` để truy vấn nhanh. |
| `booking` 1 — 0..1 `payment` | Ràng buộc **UNIQUE(`booking_id`)** → tối đa một payment/booking. |

---

## Enum & toàn vẹn

- **`parking_slot.status`**: MySQL `ENUM` — `AVAILABLE`, `BOOKED`, `MAINTENANCE` (Java: `SlotStatus`).
- **`booking.status`**: MySQL `ENUM` — `PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED` (Java: `BookingStatus`).
- **`payment.method` / `status`**: cột `VARCHAR`; ứng dụng map sang `PaymentEntity.PaymentMethod` (`STRIPE`, `CASH`) và `PaymentStatus` (**`PENDING`**, **`PAID`**, **`FAILED`**, **`CANCELLED`**).

---

## Chỉ mục phục vụ overlap

- `idx_booking_slot_time (parking_slot_id, start_at, end_at)` hỗ trợ truy vấn JPQL kiểm tra trùng khung giờ cho booking **PENDING/CONFIRMED**.

---

## Tài liệu liên quan

- `docs/chạy.md` — thứ tự chạy local
- `docs/sql.md` (thuyết trình môn DB + câu **JOIN** / **EXISTS** / **GROUP BY** trong `BookingRepository`)
- `he-thong-fe-be-db-flow.md`
- `luong-ket-noi-fe-be-db-chi-tiet.md`
- `README.md`
