SELECT * FROM patient WHERE account_id = (SELECT account_id FROM account WHERE email = 'duongquockiet0511@gmail.com');

SELECT a.account_id, a.email, r.role_name 
FROM account a
JOIN account_role ar ON a.account_id = ar.account_id
JOIN role r ON ar.role_id = r.role_id
WHERE a.email = 'duongquockiet0511@gmail.com';

UPDATE role SET role_code = 'PATIENT' WHERE role_code = 'patient';

UPDATE role SET role_name = 'PATIENT', role_code = 'PATIENT' WHERE role_name = 'patient';

-- Xem role_id của 'patient'
SELECT role_id, role_code, role_name FROM role WHERE role_code = 'patient';

-- Sau đó update theo role_id (dùng khóa chính để bypass safe mode)
UPDATE role SET role_code = 'PATIENT', role_name = 'PATIENT' WHERE role_id = (SELECT role_id FROM (SELECT role_id FROM role WHERE role_code = 'patient') AS tmp);


-- Xem account_role hiện tại của account_id = 15
SELECT * FROM account_role WHERE account_id = 15;


-- =====================================================
-- UPDATE DISCOUNT PRICE RANDOM 30-50% FOR FEATURED SERVICES
-- =====================================================

USE clinic_system;
SET SQL_SAFE_UPDATES = 1;

-- Cập nhật discount_price giảm random 30-50%
UPDATE service 
SET discount_price = ROUND(original_price * (0.5 + RAND() * 0.2), 0),
    updated_at = NOW()
WHERE is_featured = 1 
  AND original_price > 0;

-- Kiểm tra
SELECT 
    service_id,
    service_name,
    original_price,
    discount_price,
    CONCAT(ROUND((original_price - discount_price) / original_price * 100, 0), '%') as off
FROM service 
WHERE is_featured = 1
ORDER BY featured_priority;


-- =====================================================
-- MOCK DATA FOR PATIENT (account_id = 15)
-- SỬ DỤNG ID CÓ THẬT TỪ DỮ LIỆU CRAWL
-- =====================================================

USE clinic_system;

-- Lấy patient_id hiện có của account_id = 15
SET @patient_id = (SELECT patient_id FROM patient WHERE account_id = 15 LIMIT 1);

-- Kiểm tra patient_id
SELECT @patient_id as patient_id;

-- Nếu chưa có patient thì tạo mới (chỉ chạy nếu @patient_id IS NULL)
INSERT INTO patient (account_id, full_name, gender, date_of_birth, phone, address, avatar_url, is_deleted, created_at, updated_at)
SELECT 15, 'Nguyễn Đức Kiệt', 'Nam', '1995-05-11', '0987654321', '123 Đường ABC, Quận 1, TP.HCM', NULL, 0, NOW(), NOW()
WHERE @patient_id IS NULL;

-- Cập nhật lại @patient_id nếu vừa tạo mới
SET @patient_id = (SELECT patient_id FROM patient WHERE account_id = 15 LIMIT 1);

-- 2. HỒ SƠ SỨC KHỎE (dùng @patient_id)
INSERT INTO patient_vital_profile (patient_id, height, blood_type, allergies, chronic_diseases, medical_history, updated_at) 
SELECT @patient_id, 175, 'A', 'Hải sản, phấn hoa', 'Không', 'Chưa từng phẫu thuật', NOW()
WHERE NOT EXISTS (SELECT 1 FROM patient_vital_profile WHERE patient_id = @patient_id);

-- 3. LỊCH HẸN (dùng @patient_id)
INSERT INTO appointment (appointment_id, patient_id, main_doctor_id, service_id, appointment_date, time_start, time_end, appointment_type, status, created_by, note, is_deleted, created_at) VALUES
(1000, @patient_id, 1, 1, CURDATE(), '09:00:00', '09:30:00', 'ONLINE', 'COMPLETED', 'PATIENT', 'Khám theo gói xét nghiệm', 0, NOW()),
(1001, @patient_id, 1, 2, CURDATE() + INTERVAL 1 DAY, '14:00:00', '14:30:00', 'ONLINE', 'CONFIRMED', 'PATIENT', 'Tái khám', 0, NOW()),
(1002, @patient_id, 2, 3, CURDATE() + INTERVAL 3 DAY, '10:30:00', '11:00:00', 'WALK_IN', 'PENDING', 'PATIENT', 'Xét nghiệm bổ sung', 0, NOW());
-- Xem danh sách ENUM của cột status
SHOW COLUMNS FROM medical_record LIKE 'status';
-- Cập nhật ENUM cho medical_record.status (giống appointment.status)
ALTER TABLE medical_record 
-- Sửa cấu trúc bảng medical_record
-- Thêm 'PENDING' vào ENUM hiện có (không cần modify toàn bộ)
ALTER TABLE medical_record 
MODIFY COLUMN status ENUM(
    'IN_PROGRESS',
    'WAITING_RESULT', 
    'DONE',
    'CANCELLED',
    'PENDING'
) NOT NULL DEFAULT 'IN_PROGRESS';
-- 4. HỒ SƠ BỆNH ÁN
INSERT INTO medical_record (record_id, patient_id, appointment_id, main_doctor_id, diagnosis, treatment, note, status, created_at, updated_at) VALUES
(10000, @patient_id, 1000, 1, 'Theo dõi sức khỏe định kỳ', 'Thực hiện xét nghiệm theo gói', 'Bệnh nhân hợp tác, lấy mẫu thành công', 'DONE', NOW(), NOW()),
(10001, @patient_id, 1001, 1, 'Chờ kết quả xét nghiệm', 'Đã lấy mẫu, chờ kết quả', 'Đang xử lý', 'WAITING_RESULT', NOW(), NOW()),
(10002, @patient_id, 1002, 2, 'Khám tổng quát', NULL, 'Chưa thực hiện', 'PENDING', NOW(), NOW());

-- 5. CHỈ SỐ SINH TỒN
INSERT INTO medical_record_vital (record_id, weight, blood_pressure, pulse, recorded_by) VALUES
(10000, 65.5, '120/80', 72, 1),
(10001, 66.0, '118/78', 75, 1);

-- 6. DỊCH VỤ CHỈ ĐỊNH (Service Order)
INSERT INTO service_order (order_id, record_id, service_id, ordered_by, status, created_at) VALUES
(10000, 10000, 1, 1, 'DONE', NOW()),
(10001, 10000, 2, 1, 'DONE', NOW()),
(10002, 10001, 3, 1, 'ORDERED', NOW());

-- 7. KẾT QUẢ DỊCH VỤ
INSERT INTO service_result (result_id, order_id, result_data, conclusion, entered_by, entered_at) VALUES
(10000, 10000, 'Công thức máu: RBC 4.8, WBC 7.2, PLT 280\nĐường huyết: 5.2 mmol/L\nMỡ máu: Cholesterol 4.5, Triglycerid 1.2', 'Các chỉ số trong giới hạn bình thường', 7, NOW()),
(10001, 10001, 'Chức năng gan: AST 28, ALT 25, GGT 30\nChức năng thận: Urea 4.5, Creatinin 0.8', 'Chức năng gan thận bình thường', 7, NOW());

-- 8. ĐƠN THUỐC
-- Tạo bảng medicine nếu chưa có
CREATE TABLE IF NOT EXISTS medicine (
    medicine_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    active_element VARCHAR(255),
    packing_standard VARCHAR(100),
    base_unit VARCHAR(50),
    sell_price DECIMAL(10,2),
    usage_note VARCHAR(255),
    is_deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Thêm thuốc mẫu (nếu chưa có)
INSERT IGNORE INTO medicine (medicine_id, name, active_element, packing_standard, base_unit, sell_price, usage_note, is_deleted, created_at) VALUES
(1, 'Paracetamol 500mg', 'Paracetamol', 'Hộp 10 vỉ x 10 viên', 'Viên', 50000, 'Uống sau ăn, ngày 2-3 lần', 0, NOW()),
(2, 'Amoxicillin 500mg', 'Amoxicillin', 'Hộp 10 vỉ x 10 viên', 'Viên', 80000, 'Uống trước ăn 30 phút', 0, NOW()),
(3, 'Vitamin C 500mg', 'Ascorbic acid', 'Chai 100 viên', 'Viên', 45000, 'Ngày 1 viên sau ăn', 0, NOW());

-- Đơn thuốc
INSERT INTO prescription (prescription_id, record_id, created_at) VALUES
(10000, 10000, NOW());

INSERT INTO prescription_item (prescription_id, medicine_id, unit, quantity, dosage, price) VALUES
(10000, 1, 'Viên', 10, 'Ngày 2 lần, mỗi lần 1 viên', 50000),
(10000, 3, 'Viên', 30, 'Ngày 1 viên', 45000);

-- 9. TÁI KHÁM (dùng @patient_id)
INSERT INTO follow_up (follow_up_id, record_id, patient_id, doctor_id, scheduled_datetime, note, status, created_at) VALUES
(10000, 10000, @patient_id, 1, DATE_ADD(NOW(), INTERVAL 30 DAY), 'Tái khám sau 1 tháng để kiểm tra lại', 'PENDING', NOW());

-- 10. PHẢN HỒI
INSERT INTO feedback (feedback_id, record_id, rating, comment, created_at) VALUES
(10000, 10000, 5, 'Bác sĩ khám rất kỹ, nhân viên nhiệt tình. Kết quả nhanh chóng.', NOW());

-- 11. THÔNG BÁO
INSERT INTO notification (notification_id, account_id, type, content, sent_at) VALUES
(10000, 15, 'SYSTEM', 'Kết quả xét nghiệm của bạn đã có. Vui lòng đăng nhập để xem chi tiết.', NOW()),
(10001, 15, 'SYSTEM', CONCAT('Bạn có lịch tái khám vào ngày ', DATE_FORMAT(DATE_ADD(NOW(), INTERVAL 30 DAY), '%d/%m/%Y'), '. Vui lòng xác nhận lịch hẹn.'), NOW());

-- 12. CHAT SESSION (dùng @patient_id)
INSERT INTO chat_session (session_id, patient_id, started_at) VALUES
(10000, @patient_id, NOW());

INSERT INTO chat_message (message_id, session_id, sender_type, message_content, created_at) VALUES
(10000, 10000, 'USER', 'Chào bác sĩ, tôi muốn hỏi về kết quả xét nghiệm', NOW()),
(10001, 10000, 'BOT', 'Chào bạn, kết quả xét nghiệm của bạn bình thường. Các chỉ số đều trong ngưỡng cho phép.', NOW()),
(10002, 10000, 'USER', 'Cảm ơn bác sĩ, tôi có cần kiêng gì không?', NOW()),
(10003, 10000, 'BOT', 'Bạn nên duy trì chế độ ăn uống lành mạnh, tập thể dục đều đặn và tái khám đúng lịch.', NOW());

-- 13. KIỂM TRA TỔNG HỢP
-- =====================================================
-- KIỂM TRA DỮ LIỆU PATIENT (account_id = 15)
-- =====================================================

USE clinic_system;

-- Lấy patient_id
SET @patient_id = (SELECT patient_id FROM patient WHERE account_id = 15 LIMIT 1);
SELECT @patient_id as patient_id;

-- 1. KIỂM TRA PATIENT
SELECT '✅ PATIENT' as `Type`, p.patient_id as `ID`, p.full_name as `Name`, a.email as `Email`
FROM patient p 
JOIN account a ON p.account_id = a.account_id 
WHERE p.account_id = 15;

-- 2. KIỂM TRA APPOINTMENTS
SELECT '✅ APPOINTMENTS' as `Type`, COUNT(*) as `Total` 
FROM appointment 
WHERE patient_id = @patient_id;

-- 3. KIỂM TRA MEDICAL RECORDS
SELECT '✅ MEDICAL RECORDS' as `Type`, COUNT(*) as `Total` 
FROM medical_record 
WHERE patient_id = @patient_id;

-- 4. KIỂM TRA SERVICE RESULTS
SELECT '✅ SERVICE RESULTS' as `Type`, COUNT(*) as `Total`
FROM service_result sr 
JOIN service_order so ON sr.order_id = so.order_id
JOIN medical_record mr ON so.record_id = mr.record_id
WHERE mr.patient_id = @patient_id;

-- 5. KIỂM TRA PRESCRIPTIONS
SELECT '✅ PRESCRIPTIONS' as `Type`, COUNT(*) as `Total`
FROM prescription p
JOIN medical_record mr ON p.record_id = mr.record_id
WHERE mr.patient_id = @patient_id;

-- 6. KIỂM TRA FEEDBACK
SELECT '✅ FEEDBACK' as `Type`, COUNT(*) as `Total`
FROM feedback f
JOIN medical_record mr ON f.record_id = mr.record_id
WHERE mr.patient_id = @patient_id;

-- 7. KIỂM TRA FOLLOW UP
SELECT '✅ FOLLOW UP' as `Type`, COUNT(*) as `Total`
FROM follow_up 
WHERE patient_id = @patient_id;

-- 8. KIỂM TRA CHAT SESSION
SELECT '✅ CHAT SESSION' as `Type`, COUNT(*) as `Total`
FROM chat_session 
WHERE patient_id = @patient_id;