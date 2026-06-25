-- Seed demo data: luồng khám + CLS + tái khám + thông báo
-- Mật khẩu tất cả tài khoản = 12345678 (copy hash từ kiet@gmail.com)
-- Chạy: mysql -u root -p clinic_system < database/flow/seed_clinical_flow_demo.sql

USE clinic_system;

DROP PROCEDURE IF EXISTS seed_clinical_flow_demo;

DELIMITER //

CREATE PROCEDURE seed_clinical_flow_demo()
BEGIN
    DECLARE v_pwd VARCHAR(255);
    DECLARE v_role_patient INT;
    DECLARE v_role_doctor INT;
    DECLARE v_role_staff INT;
    DECLARE v_expertise INT;
    DECLARE v_lab_service INT;
    DECLARE v_lab_price DECIMAL(10,2);
    DECLARE v_doc_acc INT;
    DECLARE v_doc_staff INT;
    DECLARE v_lab_acc INT;
    DECLARE v_lab_staff INT;
    DECLARE v_letan_acc INT;
    DECLARE v_letan_staff INT;
    DECLARE v_today DATE;

    IF EXISTS (SELECT 1 FROM account WHERE email = 'bn1@gmail.com') THEN
        SELECT 'SKIP: bn1@gmail.com already exists' AS result;
    ELSE
        SET v_today = CURDATE();
        SET v_pwd = (SELECT password FROM account WHERE email = 'kiet@gmail.com' LIMIT 1);
        SET v_role_patient = (SELECT role_id FROM role WHERE role_code = 'PATIENT' LIMIT 1);
        SET v_role_doctor = (SELECT role_id FROM role WHERE role_code = 'DOCTOR' LIMIT 1);
        SET v_role_staff = (SELECT role_id FROM role WHERE role_code = 'STAFF' LIMIT 1);
        SET v_expertise = (SELECT expertise_id FROM expertise ORDER BY expertise_id LIMIT 1);
        SET v_lab_service = (SELECT service_id FROM service WHERE service_type = 'LAB_TEST' AND is_deleted = 0 ORDER BY service_id LIMIT 1);
        SET v_lab_price = (SELECT COALESCE(discount_price, original_price) FROM service WHERE service_id = v_lab_service);

        IF v_pwd IS NULL OR v_role_patient IS NULL OR v_role_doctor IS NULL THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Missing base data: kiet@gmail.com account or roles';
        END IF;

        -- ===== STAFF: bacsi@gmail.com =====
        INSERT INTO account (email, password, is_active) VALUES ('bacsi@gmail.com', v_pwd, 1);
        SET v_doc_acc = LAST_INSERT_ID();
        INSERT INTO account_role (account_id, role_id) VALUES (v_doc_acc, v_role_doctor);
        INSERT INTO staff (account_id, expertise_id, full_name, gender, phone, staff_type, is_deleted)
        VALUES (v_doc_acc, v_expertise, 'BS. Nguyễn Minh Kiet', 'Nam', '0900000001', 'DOCTOR', 0);
        SET v_doc_staff = LAST_INSERT_ID();

        -- ===== STAFF: lab@gmail.com =====
        INSERT INTO account (email, password, is_active) VALUES ('lab@gmail.com', v_pwd, 1);
        SET v_lab_acc = LAST_INSERT_ID();
        INSERT INTO account_role (account_id, role_id) VALUES (v_lab_acc, v_role_staff);
        INSERT INTO staff (account_id, full_name, gender, phone, staff_type, is_deleted)
        VALUES (v_lab_acc, 'KTV. Trần Lab', 'Nam', '0900000002', 'LAB_TECH', 0);
        SET v_lab_staff = LAST_INSERT_ID();

        -- ===== STAFF: letan@gmail.com =====
        INSERT INTO account (email, password, is_active) VALUES ('letan@gmail.com', v_pwd, 1);
        SET v_letan_acc = LAST_INSERT_ID();
        INSERT INTO account_role (account_id, role_id) VALUES (v_letan_acc, v_role_staff);
        INSERT INTO staff (account_id, full_name, gender, phone, staff_type, is_deleted)
        VALUES (v_letan_acc, 'Lễ tân Mai', 'Nữ', '0900000003', 'STAFF', 0);
        SET v_letan_staff = LAST_INSERT_ID();

        -- ===== Helper: create patient =====
        -- BN1 — CHECKED_IN queue 1
        INSERT INTO account (email, password, is_active) VALUES ('bn1@gmail.com', v_pwd, 1);
        SET @acc = LAST_INSERT_ID();
        INSERT INTO account_role VALUES (@acc, v_role_patient);
        INSERT INTO patient (account_id, full_name, gender, date_of_birth, phone, address, is_deleted)
        VALUES (@acc, 'BN1 - Chờ gọi khám', 'Nam', '1990-01-15', '0901000001', 'TP.HCM - Demo', 0);
        SET @pat = LAST_INSERT_ID();
        INSERT INTO appointment (patient_id, main_doctor_id, expertise_id, appointment_date, time_start, time_end,
            appointment_type, status, created_by, booking_mode, checkin_time, queue_number, is_deleted)
        VALUES (@pat, v_doc_staff, v_expertise, v_today, '08:00:00', '08:30:00', 'WALK_IN', 'CHECKED_IN', 'STAFF', 'DOCTOR',
            CONCAT(v_today, ' 07:45:00'), 1, 0);
        SET @apt = LAST_INSERT_ID();
        INSERT INTO medical_record (patient_id, appointment_id, main_doctor_id, diagnosis, treatment, note, status, vitals_taken)
        VALUES (@pat, @apt, v_doc_staff, 'Chưa khám', NULL, NULL, 'IN_PROGRESS', 1);
        SET @rec = LAST_INSERT_ID();
        INSERT INTO notification (account_id, type, content, sent_at)
        VALUES (@acc, 'SYSTEM', 'Lịch khám hôm nay lúc 08:00. Bạn đã check-in, vui lòng chờ gọi tên.', NOW());

        -- BN2 — IN_PROGRESS + ORDERED
        INSERT INTO account (email, password, is_active) VALUES ('bn2@gmail.com', v_pwd, 1);
        SET @acc = LAST_INSERT_ID();
        INSERT INTO account_role VALUES (@acc, v_role_patient);
        INSERT INTO patient (account_id, full_name, gender, date_of_birth, phone, address, is_deleted)
        VALUES (@acc, 'BN2 - Đang khám có CLS', 'Nam', '1990-01-15', '0901000002', 'TP.HCM - Demo', 0);
        SET @pat = LAST_INSERT_ID();
        INSERT INTO appointment (patient_id, main_doctor_id, expertise_id, appointment_date, time_start, time_end,
            appointment_type, status, created_by, booking_mode, checkin_time, is_deleted)
        VALUES (@pat, v_doc_staff, v_expertise, v_today, '08:30:00', '09:00:00', 'WALK_IN', 'IN_PROGRESS', 'STAFF', 'DOCTOR',
            CONCAT(v_today, ' 08:20:00'), 0);
        SET @apt = LAST_INSERT_ID();
        INSERT INTO medical_record (patient_id, appointment_id, main_doctor_id, diagnosis, treatment, note, status, vitals_taken)
        VALUES (@pat, @apt, v_doc_staff, 'Viêm họng nghi ngờ', 'Theo dõi', NULL, 'IN_PROGRESS', 1);
        SET @rec = LAST_INSERT_ID();
        INSERT INTO service_order (record_id, service_id, ordered_by, price_at_time, status)
        VALUES (@rec, v_lab_service, v_doc_staff, v_lab_price, 'ORDERED');
        INSERT INTO notification (account_id, type, content, sent_at)
        VALUES (@acc, 'SYSTEM', CONCAT('Đến lượt khám của bạn. Vui lòng vào phòng khám gặp Bác sĩ BS. Nguyễn Minh Kiet.'), NOW());

        -- BN3 — WAITING_RESULT + ORDERED
        INSERT INTO account (email, password, is_active) VALUES ('bn3@gmail.com', v_pwd, 1);
        SET @acc = LAST_INSERT_ID();
        INSERT INTO account_role VALUES (@acc, v_role_patient);
        INSERT INTO patient (account_id, full_name, gender, date_of_birth, phone, address, is_deleted)
        VALUES (@acc, 'BN3 - Chờ kết quả CLS', 'Nam', '1990-01-15', '0901000003', 'TP.HCM - Demo', 0);
        SET @pat = LAST_INSERT_ID();
        INSERT INTO appointment (patient_id, main_doctor_id, expertise_id, appointment_date, time_start, time_end,
            appointment_type, status, created_by, booking_mode, checkin_time, is_deleted)
        VALUES (@pat, v_doc_staff, v_expertise, v_today, '09:00:00', '09:30:00', 'WALK_IN', 'WAITING_RESULT', 'STAFF', 'DOCTOR',
            CONCAT(v_today, ' 08:50:00'), 0);
        SET @apt = LAST_INSERT_ID();
        INSERT INTO medical_record (patient_id, appointment_id, main_doctor_id, diagnosis, treatment, note, status, vitals_taken)
        VALUES (@pat, @apt, v_doc_staff, 'Nghi ngờ viêm phổi', 'Chờ XN', NULL, 'WAITING_RESULT', 1);
        SET @rec = LAST_INSERT_ID();
        INSERT INTO service_order (record_id, service_id, ordered_by, price_at_time, status)
        VALUES (@rec, v_lab_service, v_doc_staff, v_lab_price, 'ORDERED');
        INSERT INTO notification (account_id, type, content, sent_at)
        VALUES (@acc, 'SYSTEM', 'Bác sĩ đã chỉ định cận lâm sàng. Vui lòng di chuyển đến khu vực xét nghiệm/chụp chiếu.', NOW());

        -- BN4 — CHECKED_IN queue 0 + KQ DONE
        INSERT INTO account (email, password, is_active) VALUES ('bn4@gmail.com', v_pwd, 1);
        SET @acc = LAST_INSERT_ID();
        INSERT INTO account_role VALUES (@acc, v_role_patient);
        INSERT INTO patient (account_id, full_name, gender, date_of_birth, phone, address, is_deleted)
        VALUES (@acc, 'BN4 - Đọc kết quả', 'Nam', '1990-01-15', '0901000004', 'TP.HCM - Demo', 0);
        SET @pat = LAST_INSERT_ID();
        INSERT INTO appointment (patient_id, main_doctor_id, expertise_id, appointment_date, time_start, time_end,
            appointment_type, status, created_by, booking_mode, checkin_time, queue_number, is_deleted)
        VALUES (@pat, v_doc_staff, v_expertise, v_today, '09:30:00', '10:00:00', 'WALK_IN', 'CHECKED_IN', 'STAFF', 'DOCTOR',
            CONCAT(v_today, ' 09:15:00'), 0, 0);
        SET @apt = LAST_INSERT_ID();
        INSERT INTO medical_record (patient_id, appointment_id, main_doctor_id, diagnosis, treatment, note, status, vitals_taken)
        VALUES (@pat, @apt, v_doc_staff, 'Theo dõi sau XN', NULL, NULL, 'WAITING_RESULT', 1);
        SET @rec = LAST_INSERT_ID();
        INSERT INTO service_order (record_id, service_id, ordered_by, price_at_time, status)
        VALUES (@rec, v_lab_service, v_doc_staff, v_lab_price, 'DONE');
        SET @ord = LAST_INSERT_ID();
        INSERT INTO service_result (order_id, result_data, conclusion, entered_by, entered_at, created_at)
        VALUES (@ord, 'WBC: 12.000', 'Viêm nhiễm nhẹ', v_lab_staff, NOW(), NOW());
        INSERT INTO notification (account_id, type, content, sent_at)
        VALUES (@acc, 'SYSTEM', 'Đã có kết quả xét nghiệm. Bạn đã được xếp ưu tiên vào gặp bác sĩ để đọc kết quả.', NOW());

        -- BN5 — COMPLETED + follow_up PENDING
        INSERT INTO account (email, password, is_active) VALUES ('bn5@gmail.com', v_pwd, 1);
        SET @acc = LAST_INSERT_ID();
        INSERT INTO account_role VALUES (@acc, v_role_patient);
        INSERT INTO patient (account_id, full_name, gender, date_of_birth, phone, address, is_deleted)
        VALUES (@acc, 'BN5 - Đã khám + tái khám', 'Nam', '1990-01-15', '0901000005', 'TP.HCM - Demo', 0);
        SET @pat = LAST_INSERT_ID();
        INSERT INTO appointment (patient_id, main_doctor_id, expertise_id, appointment_date, time_start, time_end,
            appointment_type, status, created_by, booking_mode, checkin_time, checkout_time, is_deleted)
        VALUES (@pat, v_doc_staff, v_expertise, DATE_SUB(v_today, INTERVAL 1 DAY), '10:00:00', '10:30:00',
            'WALK_IN', 'COMPLETED', 'STAFF', 'DOCTOR',
            CONCAT(DATE_SUB(v_today, INTERVAL 1 DAY), ' 09:45:00'),
            CONCAT(DATE_SUB(v_today, INTERVAL 1 DAY), ' 10:30:00'), 0);
        SET @apt = LAST_INSERT_ID();
        INSERT INTO medical_record (patient_id, appointment_id, main_doctor_id, diagnosis, treatment, note, status, vitals_taken)
        VALUES (@pat, @apt, v_doc_staff, 'Viêm đường hô hấp', 'Thuốc + nghỉ ngơi', 'Tái khám 7 ngày', 'DONE', 1);
        SET @rec = LAST_INSERT_ID();
        INSERT INTO follow_up (record_id, patient_id, doctor_id, scheduled_datetime, note, status)
        VALUES (@rec, @pat, v_doc_staff, DATE_ADD(v_today, INTERVAL 7 DAY) + INTERVAL 9 HOUR, 'Tái khám đánh giá đáp ứng điều trị', 'PENDING');
        INSERT INTO notification (account_id, type, content, sent_at)
        VALUES (@acc, 'SYSTEM', CONCAT('Bác sĩ BS. Nguyễn Minh Kiet hẹn bạn tái khám vào ',
            DATE_FORMAT(DATE_ADD(v_today, INTERVAL 7 DAY) + INTERVAL 9 HOUR, '%d/%m/%Y %H:%i'),
            '. Ghi chú: Tái khám đáp ứng điều trị. Vui lòng xác nhận trên ứng dụng.'), NOW());

        -- BN6 — follow_up ngày mai (test job D-1)
        INSERT INTO account (email, password, is_active) VALUES ('bn6@gmail.com', v_pwd, 1);
        SET @acc = LAST_INSERT_ID();
        INSERT INTO account_role VALUES (@acc, v_role_patient);
        INSERT INTO patient (account_id, full_name, gender, date_of_birth, phone, address, is_deleted)
        VALUES (@acc, 'BN6 - Nhắc tái khám mai', 'Nam', '1990-01-15', '0901000006', 'TP.HCM - Demo', 0);
        SET @pat = LAST_INSERT_ID();
        INSERT INTO appointment (patient_id, main_doctor_id, expertise_id, appointment_date, time_start, time_end,
            appointment_type, status, created_by, booking_mode, is_deleted)
        VALUES (@pat, v_doc_staff, v_expertise, DATE_SUB(v_today, INTERVAL 3 DAY), '11:00:00', '11:30:00',
            'WALK_IN', 'COMPLETED', 'STAFF', 'DOCTOR', 0);
        SET @apt = LAST_INSERT_ID();
        INSERT INTO medical_record (patient_id, appointment_id, main_doctor_id, diagnosis, treatment, note, status, vitals_taken)
        VALUES (@pat, @apt, v_doc_staff, 'Hen suyễn', 'Dùng thuốc dự phòng', NULL, 'DONE', 1);
        SET @rec = LAST_INSERT_ID();
        INSERT INTO follow_up (record_id, patient_id, doctor_id, scheduled_datetime, note, status)
        VALUES (@rec, @pat, v_doc_staff, DATE_ADD(v_today, INTERVAL 1 DAY) + INTERVAL 10 HOUR, 'Tái khám kiểm tra phổi', 'PENDING');

        -- BN7 — CONFIRMED follow-up + nhiều thông báo
        INSERT INTO account (email, password, is_active) VALUES ('bn7@gmail.com', v_pwd, 1);
        SET @acc = LAST_INSERT_ID();
        INSERT INTO account_role VALUES (@acc, v_role_patient);
        INSERT INTO patient (account_id, full_name, gender, date_of_birth, phone, address, is_deleted)
        VALUES (@acc, 'BN7 - Đã xác nhận tái khám', 'Nam', '1990-01-15', '0901000007', 'TP.HCM - Demo', 0);
        SET @pat = LAST_INSERT_ID();
        INSERT INTO appointment (patient_id, main_doctor_id, expertise_id, appointment_date, time_start, time_end,
            appointment_type, status, created_by, booking_mode, is_deleted)
        VALUES (@pat, v_doc_staff, v_expertise, DATE_SUB(v_today, INTERVAL 5 DAY), '14:00:00', '14:30:00',
            'WALK_IN', 'COMPLETED', 'STAFF', 'DOCTOR', 0);
        SET @apt = LAST_INSERT_ID();
        INSERT INTO medical_record (patient_id, appointment_id, main_doctor_id, diagnosis, treatment, note, status, vitals_taken)
        VALUES (@pat, @apt, v_doc_staff, 'Dị ứng da', 'Tránh allergen', NULL, 'DONE', 1);
        SET @rec = LAST_INSERT_ID();
        INSERT INTO follow_up (record_id, patient_id, doctor_id, scheduled_datetime, note, status, confirmed_at)
        VALUES (@rec, @pat, v_doc_staff, DATE_ADD(v_today, INTERVAL 14 DAY) + INTERVAL 15 HOUR,
            'Tái khám da liễu', 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 1 DAY));
        INSERT INTO notification (account_id, type, content, sent_at) VALUES
            (@acc, 'SYSTEM', 'Lịch hẹn khám của bạn đã được xác nhận.', NOW()),
            (@acc, 'SYSTEM', 'Kết quả xét nghiệm máu của bạn đã sẵn sàng. Vui lòng xem trong Hồ sơ y tế.', NOW());

        SELECT 'OK: Demo data seeded successfully' AS result;
        SELECT email, '12345678' AS password_hint FROM account
        WHERE email IN ('bacsi@gmail.com','letan@gmail.com','lab@gmail.com',
            'bn1@gmail.com','bn2@gmail.com','bn3@gmail.com','bn4@gmail.com',
            'bn5@gmail.com','bn6@gmail.com','bn7@gmail.com')
        ORDER BY email;
    END IF;
END //

DELIMITER ;

CALL seed_clinical_flow_demo();
DROP PROCEDURE IF EXISTS seed_clinical_flow_demo;
