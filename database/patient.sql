-- ============================================================
-- TẮT SAFE MODE
-- ============================================================
SET SQL_SAFE_UPDATES = 0;

-- ============================================================
-- XÓA DỮ LIỆU CŨ CỦA BỆNH NHÂN NÀY (để chạy lại sạch sẽ)
-- ============================================================
SET @email = 'duongquockiet0511@gmail.com';

-- Lấy patient_id
SELECT patient_id INTO @pid FROM patient p
JOIN account a ON p.account_id = a.account_id
WHERE a.email = @email COLLATE utf8mb4_unicode_ci;

-- Nếu chưa có patient thì tạo mới (để tránh lỗi)
INSERT INTO account (email, password, is_active, created_at)
SELECT @email, '$2a$10$dummyHash', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM account WHERE email = @email COLLATE utf8mb4_unicode_ci);

SELECT account_id INTO @accid FROM account WHERE email = @email COLLATE utf8mb4_unicode_ci;

INSERT INTO patient (account_id, full_name, gender, date_of_birth, phone, address, created_at)
SELECT @accid, 'Kiệt Đường Quốc', 'Nam', '1995-05-11', '0912345678', '123 Đường ABC, Quận 1, TP.HCM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM patient WHERE account_id = @accid);

SELECT patient_id INTO @pid FROM patient WHERE account_id = @accid;

-- Xóa feedback trước (vì FK tới medical_record)
DELETE FROM feedback WHERE record_id IN (
    SELECT record_id FROM medical_record WHERE patient_id = @pid
);

-- Xóa các bảng con theo thứ tự
DELETE FROM prescription_item WHERE prescription_id IN (
    SELECT prescription_id FROM prescription WHERE record_id IN (
        SELECT record_id FROM medical_record WHERE patient_id = @pid
    )
);
DELETE FROM prescription WHERE record_id IN (
    SELECT record_id FROM medical_record WHERE patient_id = @pid
);
DELETE FROM service_result WHERE order_id IN (
    SELECT order_id FROM service_order WHERE record_id IN (
        SELECT record_id FROM medical_record WHERE patient_id = @pid
    )
);
DELETE FROM service_order WHERE record_id IN (
    SELECT record_id FROM medical_record WHERE patient_id = @pid
);
DELETE FROM medical_record WHERE patient_id = @pid;
DELETE FROM appointment WHERE patient_id = @pid;

-- ============================================================
-- 1. LẤY BÁC SĨ VÀ DỊCH VỤ
-- ============================================================
SET @doctor_id = (SELECT staff_id FROM staff WHERE staff_type = 'DOCTOR' LIMIT 1);
SET @exam_service = (SELECT service_id FROM service WHERE service_type = 'EXAM' LIMIT 1);
SET @lab_service = (SELECT service_id FROM service WHERE service_type = 'LAB_TEST' LIMIT 1);
SET @imaging_service = (SELECT service_id FROM service WHERE service_type = 'IMAGING' LIMIT 1);
SET @med1 = (SELECT medicine_id FROM medicine WHERE name LIKE 'Paracetamol%' LIMIT 1);
SET @med2 = (SELECT medicine_id FROM medicine WHERE name LIKE 'Amoxicillin%' LIMIT 1);
SET @med3 = (SELECT medicine_id FROM medicine WHERE name LIKE 'Vitamin C%' LIMIT 1);

-- Nếu chưa có thuốc, tạo mới
INSERT IGNORE INTO medicine (name, active_element, packing_standard, base_unit, usage_note, created_at) VALUES
('Paracetamol 500mg', 'Paracetamol', 'Hộp 10 vỉ x 10 viên', 'Viên', 'Uống sau ăn', NOW()),
('Amoxicillin 250mg', 'Amoxicillin', 'Hộp 20 viên', 'Viên', 'Uống trước ăn', NOW()),
('Vitamin C 1000mg', 'Vitamin C', 'Lọ 30 viên', 'Viên', 'Uống mỗi sáng', NOW());

SET @med1 = (SELECT medicine_id FROM medicine WHERE name LIKE 'Paracetamol%' LIMIT 1);
SET @med2 = (SELECT medicine_id FROM medicine WHERE name LIKE 'Amoxicillin%' LIMIT 1);
SET @med3 = (SELECT medicine_id FROM medicine WHERE name LIKE 'Vitamin C%' LIMIT 1);

-- ============================================================
-- 2. TẠO NHIỀU LỊCH HẸN VỚI CÁC TRẠNG THÁI KHÁC NHAU
-- ============================================================
-- 2.1. Lịch PENDING (chưa xác nhận) - ngày mai
INSERT INTO appointment (
    patient_id, main_doctor_id, service_id,
    appointment_date, time_start, time_end,
    appointment_type, status, created_by, queue_number, created_at
) VALUES (
    @pid, @doctor_id, @exam_service,
    DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', '09:30:00',
    'ONLINE', 'PENDING', 'PATIENT', 1, NOW()
);

-- 2.2. Lịch CONFIRMED (đã xác nhận) - ngày kia
INSERT INTO appointment (
    patient_id, main_doctor_id, service_id,
    appointment_date, time_start, time_end,
    appointment_type, status, created_by, queue_number, created_at
) VALUES (
    @pid, @doctor_id, @exam_service,
    DATE_ADD(CURDATE(), INTERVAL 2 DAY), '10:00:00', '10:30:00',
    'WALK_IN', 'CONFIRMED', 'STAFF', 2, NOW()
);

-- 2.3. Lịch COMPLETED (đã khám xong) - hôm qua
INSERT INTO appointment (
    patient_id, main_doctor_id, service_id,
    appointment_date, time_start, time_end,
    appointment_type, status, created_by, queue_number, created_at
) VALUES (
    @pid, @doctor_id, @exam_service,
    DATE_SUB(CURDATE(), INTERVAL 1 DAY), '08:30:00', '09:00:00',
    'WALK_IN', 'COMPLETED', 'PATIENT', 3, NOW()
);
SET @app_completed1 = LAST_INSERT_ID();

-- 2.4. Lịch COMPLETED khác - 3 ngày trước
INSERT INTO appointment (
    patient_id, main_doctor_id, service_id,
    appointment_date, time_start, time_end,
    appointment_type, status, created_by, queue_number, created_at
) VALUES (
    @pid, @doctor_id, @exam_service,
    DATE_SUB(CURDATE(), INTERVAL 3 DAY), '14:00:00', '14:30:00',
    'ONLINE', 'COMPLETED', 'PATIENT', 4, NOW()
);
SET @app_completed2 = LAST_INSERT_ID();

-- 2.5. Lịch CANCELLED (bị hủy) - 5 ngày trước
INSERT INTO appointment (
    patient_id, main_doctor_id, service_id,
    appointment_date, time_start, time_end,
    appointment_type, status, created_by, queue_number, created_at,
    cancelled_by, cancel_reason
) VALUES (
    @pid, @doctor_id, @exam_service,
    DATE_SUB(CURDATE(), INTERVAL 5 DAY), '11:00:00', '11:30:00',
    'WALK_IN', 'CANCELLED', 'PATIENT', 5, NOW(),
    'PATIENT', 'Bệnh nhân bận đột xuất'
);

-- 2.6. Lịch NO_SHOW (không đến) - 2 ngày trước
INSERT INTO appointment (
    patient_id, main_doctor_id, service_id,
    appointment_date, time_start, time_end,
    appointment_type, status, created_by, queue_number, created_at
) VALUES (
    @pid, @doctor_id, @exam_service,
    DATE_SUB(CURDATE(), INTERVAL 2 DAY), '15:00:00', '15:30:00',
    'ONLINE', 'NO_SHOW', 'PATIENT', 6, NOW()
);

-- 2.7. Lịch SKIPPED (bỏ qua) - 4 ngày trước
INSERT INTO appointment (
    patient_id, main_doctor_id, service_id,
    appointment_date, time_start, time_end,
    appointment_type, status, created_by, queue_number, created_at
) VALUES (
    @pid, @doctor_id, @exam_service,
    DATE_SUB(CURDATE(), INTERVAL 4 DAY), '16:00:00', '16:30:00',
    'WALK_IN', 'SKIPPED', 'STAFF', 7, NOW()
);

-- ============================================================
-- 3. TẠO HỒ SƠ BỆNH ÁN CHO CÁC LỊCH COMPLETED
-- ============================================================
-- 3.1. Hồ sơ cho lịch hẹn COMPLETED 1 (hôm qua)
INSERT INTO medical_record (
    patient_id, appointment_id, main_doctor_id,
    diagnosis, treatment, note, status,
    consultation_fee, service_fee, created_at
) VALUES (
    @pid, @app_completed1, @doctor_id,
    'Cảm cúm, viêm họng nhẹ',
    'Nghỉ ngơi, uống nhiều nước, dùng thuốc theo đơn',
    'Bệnh nhân sốt nhẹ, đau họng, mệt mỏi',
    'DONE', 200000, 350000, NOW()
);
SET @rec1 = LAST_INSERT_ID();

-- 3.2. Hồ sơ cho lịch hẹn COMPLETED 2 (3 ngày trước)
INSERT INTO medical_record (
    patient_id, appointment_id, main_doctor_id,
    diagnosis, treatment, note, status,
    consultation_fee, service_fee, created_at
) VALUES (
    @pid, @app_completed2, @doctor_id,
    'Đau dạ dày, trào ngược axit',
    'Uống thuốc kháng axit, ăn uống điều độ',
    'Bệnh nhân đau thượng vị, ợ nóng',
    'DONE', 200000, 400000, NOW()
);
SET @rec2 = LAST_INSERT_ID();

-- ============================================================
-- 4. CHỈ ĐỊNH XÉT NGHIỆM CHO MỖI HỒ SƠ
-- ============================================================
-- 4.1. Chỉ định xét nghiệm cho hồ sơ 1
INSERT INTO service_order (record_id, service_id, ordered_by, status, created_at)
VALUES (@rec1, @lab_service, @doctor_id, 'DONE', NOW());
SET @order1 = LAST_INSERT_ID();

-- 4.2. Chỉ định xét nghiệm khác cho hồ sơ 1 (thêm 1 dịch vụ IMAGING)
INSERT INTO service_order (record_id, service_id, ordered_by, status, created_at)
VALUES (@rec1, @imaging_service, @doctor_id, 'DONE', NOW());
SET @order2 = LAST_INSERT_ID();

-- 4.3. Chỉ định xét nghiệm cho hồ sơ 2
INSERT INTO service_order (record_id, service_id, ordered_by, status, created_at)
VALUES (@rec2, @lab_service, @doctor_id, 'DONE', NOW());
SET @order3 = LAST_INSERT_ID();

-- ============================================================
-- 5. NHẬP KẾT QUẢ XÉT NGHIỆM
-- ============================================================
INSERT INTO service_result (order_id, result_data, conclusion, entered_by, entered_at) VALUES
(@order1, 'Glucose: 5.2, Cholesterol: 4.8, Triglycerides: 1.2', 'Bình thường', @doctor_id, NOW()),
(@order2, 'CT scan ngực: không phát hiện bất thường', 'Bình thường', @doctor_id, NOW()),
(@order3, 'Glucose: 6.1, Cholesterol: 5.5, Triglycerides: 2.3', 'Tăng nhẹ, cần theo dõi', @doctor_id, NOW());

-- ============================================================
-- 6. TẠO ĐƠN THUỐC CHO MỖI HỒ SƠ
-- ============================================================
-- 6.1. Đơn thuốc cho hồ sơ 1
INSERT INTO prescription (record_id, status, created_at) VALUES (@rec1, 'COMPLETED', NOW());
SET @pres1 = LAST_INSERT_ID();
INSERT INTO prescription_item (prescription_id, medicine_id, unit, quantity, dosage) VALUES
(@pres1, @med1, 'Viên', 10, 'Uống 1 viên/ngày sau ăn'),
(@pres1, @med2, 'Viên', 20, 'Uống 2 viên/lần, 3 lần/ngày');

-- 6.2. Đơn thuốc cho hồ sơ 2
INSERT INTO prescription (record_id, status, created_at) VALUES (@rec2, 'COMPLETED', NOW());
SET @pres2 = LAST_INSERT_ID();
INSERT INTO prescription_item (prescription_id, medicine_id, unit, quantity, dosage) VALUES
(@pres2, @med2, 'Viên', 15, 'Uống 1 viên/lần, 2 lần/ngày'),
(@pres2, @med3, 'Gói', 10, 'Pha 1 gói/ngày');

-- ============================================================
-- 7. THÊM FEEDBACK CHO HỒ SƠ ĐÃ KHÁM (tuỳ chọn)
-- ============================================================
INSERT INTO feedback (record_id, rating, comment, created_at) VALUES
(@rec1, 5, 'Rất hài lòng với dịch vụ', NOW()),
(@rec2, 4, 'Dịch vụ tốt, nhưng chờ hơi lâu', NOW());

-- ============================================================
-- 8. KIỂM TRA KẾT QUẢ TỔNG HỢP
-- ============================================================
SELECT '=== TỔNG SỐ LỊCH HẸN ===' AS '';
SELECT status, COUNT(*) AS total FROM appointment WHERE patient_id = @pid GROUP BY status;

SELECT '=== DANH SÁCH LỊCH HẸN ===' AS '';
SELECT appointment_id, appointment_date, time_start, status 
FROM appointment 
WHERE patient_id = @pid 
ORDER BY appointment_date DESC;

SELECT '=== HỒ SƠ BỆNH ÁN ===' AS '';
SELECT * FROM medical_record WHERE patient_id = @pid;

SELECT '=== CHỈ ĐỊNH XÉT NGHIỆM ===' AS '';
SELECT so.*, s.service_name 
FROM service_order so
JOIN service s ON so.service_id = s.service_id
WHERE so.record_id IN (SELECT record_id FROM medical_record WHERE patient_id = @pid);

SELECT '=== ĐƠN THUỐC ===' AS '';
SELECT p.*, pr.record_id 
FROM prescription p
JOIN medical_record pr ON p.record_id = pr.record_id
WHERE pr.patient_id = @pid;

-- ============================================================
-- BẬT LẠI SAFE MODE
-- ============================================================
SET SQL_SAFE_UPDATES = 1;
-- Fix is_deleted NULL issue
UPDATE appointment SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE medical_record SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE prescription SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE service_order SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE service_result SET is_deleted = 0 WHERE is_deleted IS NULL;
