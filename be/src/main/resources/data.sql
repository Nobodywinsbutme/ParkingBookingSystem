-- Demo seed data (idempotent). Safe to re-run with INSERT IGNORE.

INSERT IGNORE INTO parking_area (id, city, name, address_line1, is_active, created_at, updated_at)
VALUES
    ('area_demo_01', 'Ho Chi Minh City', 'Central Parking A', '1 Nguyen Hue', TRUE, NOW(6), NOW(6)),
    ('area_demo_02', 'Ho Chi Minh City', 'Airport Lot B', 'Near TSN Terminal', TRUE, NOW(6), NOW(6));

INSERT IGNORE INTO parking_slot (id, parking_area_id, code, floor, status, is_active, created_at, updated_at)
VALUES
    ('slot_demo_01', 'area_demo_01', 'A-101', '1', 'AVAILABLE', TRUE, NOW(6), NOW(6)),
    ('slot_demo_02', 'area_demo_01', 'A-102', '1', 'AVAILABLE', TRUE, NOW(6), NOW(6)),
    ('slot_demo_03', 'area_demo_02', 'B-201', '2', 'AVAILABLE', TRUE, NOW(6), NOW(6));
