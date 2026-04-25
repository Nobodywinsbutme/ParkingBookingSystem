-- MySQL schema for Parking Booking (syllabus stack). Run against database `smart_parking`.

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NULL
);

CREATE TABLE IF NOT EXISTS parking_area (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    city VARCHAR(255) NULL,
    name VARCHAR(255) NOT NULL,
    address_line1 VARCHAR(512) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL
);

CREATE TABLE IF NOT EXISTS parking_slot (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    parking_area_id VARCHAR(32) NOT NULL,
    code VARCHAR(64) NOT NULL,
    floor VARCHAR(32) NULL,
    status ENUM('AVAILABLE', 'BOOKED', 'MAINTENANCE') NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    CONSTRAINT fk_slot_area FOREIGN KEY (parking_area_id) REFERENCES parking_area (id),
    CONSTRAINT unique_slot_per_area UNIQUE (parking_area_id, code)
);

CREATE TABLE IF NOT EXISTS booking (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    user_id VARCHAR(32) NOT NULL,
    parking_area_id VARCHAR(32) NOT NULL,
    parking_slot_id VARCHAR(32) NOT NULL,
    start_at DATETIME(6) NOT NULL,
    end_at DATETIME(6) NOT NULL,
    checked_in_at DATETIME(6) NULL,
    checked_out_at DATETIME(6) NULL,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED') NOT NULL,
    currency VARCHAR(8) NOT NULL,
    amount_subtotal INT NOT NULL,
    amount_total INT NOT NULL,
    notes TEXT NULL,
    cancelled_at DATETIME(6) NULL,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_booking_area FOREIGN KEY (parking_area_id) REFERENCES parking_area (id),
    CONSTRAINT fk_booking_slot FOREIGN KEY (parking_slot_id) REFERENCES parking_slot (id),
    KEY idx_booking_slot_time (parking_slot_id, start_at, end_at)
);

CREATE TABLE IF NOT EXISTS payment (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    booking_id VARCHAR(32) NOT NULL,
    method VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    amount INT NOT NULL,
    currency VARCHAR(8) NOT NULL,
    provider_txn_ref VARCHAR(255) NULL,
    paid_at DATETIME(6) NULL,
    refunded_at DATETIME(6) NULL,
    deleted_at DATETIME(6) NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES booking (id),
    CONSTRAINT unique_booking_payment UNIQUE (booking_id)
);
