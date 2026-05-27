-- =====================================================================
-- INSERT MOCK DATA FOR FRONTEND TESTING
-- (Chạy sau khi đã chạy schema4.sql và data.sql cơ bản)
-- =====================================================================
USE clinic_system;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. ACCOUNT (tạo tài khoản cho bệnh nhân mẫu)
TRUNCATE TABLE account;
INSERT INTO account (account_id, email, password, is_active) VALUES
(1, 'patient1@example.com', '$2a$10$dummyHashForDemo', 1),   -- mật khẩu: 123456 (mã hóa thật nếu cần)
(2, 'patient2@example.com', '$2a$10$dummyHashForDemo', 1),
(3, 'patient3@example.com', '$2a$10$dummyHashForDemo', 1);

-- 2. PATIENT (tương ứng account)
TRUNCATE TABLE patient;
INSERT INTO patient (patient_id, account_id, full_name, gender, date_of_birth, phone, address, avatar_url) VALUES
(1, 1, 'Nguyễn Văn An', 'MALE', '1990-05-15', '0901234561', '123 Đường Lê Lợi, Quận 1, TP.HCM', '/avatars/avatar-default.png'),
(2, 2, 'Trần Thị Bình', 'FEMALE', '1985-09-22', '0901234562', '456 Đường Nguyễn Huệ, Quận 2, TP.HCM', '/avatars/avatar-default.png'),
(3, 3, 'Lê Văn Cường', 'MALE', '1995-12-10', '0901234563', '789 Đường Võ Thị Sáu, Quận 3, TP.HCM', '/avatars/avatar-default.png');

-- 3. APPOINTMENT (lịch hẹn)
TRUNCATE TABLE appointment;
INSERT INTO appointment (appointment_id, patient_id, main_doctor_id, appointment_date, time_start, time_end, appointment_type, status, created_by) VALUES
(101, 1, 1, '2025-05-20', '09:00:00', '09:30:00', 'ONLINE', 'COMPLETED', 'PATIENT'),
(102, 1, 2, '2025-05-25', '10:00:00', '10:30:00', 'ONLINE', 'PENDING', 'PATIENT'),
(103, 2, 3, '2025-05-18', '14:00:00', '14:30:00', 'WALK_IN', 'COMPLETED', 'STAFF'),
(104, 3, 1, '2025-05-22', '08:30:00', '09:00:00', 'ONLINE', 'IN_PROGRESS', 'PATIENT');

-- 4. MEDICAL_RECORD (hồ sơ bệnh án)
TRUNCATE TABLE medical_record;
INSERT INTO medical_record (record_id, patient_id, appointment_id, main_doctor_id, diagnosis, treatment, status) VALUES
(1001, 1, 101, 1, 'Cảm cúm thông thường', 'Nghỉ ngơi, uống nhiều nước, paracetamol 500mg x 3 ngày', 'DONE'),
(1002, 2, 103, 3, 'Viêm họng cấp do liên cầu khuẩn', 'Amoxicillin 500mg x 7 ngày, súc họng nước muối', 'DONE'),
(1003, 3, 104, 1, 'Đau đầu căng thẳng', 'Giảm stress, massage, thuốc giảm đau khi cần', 'IN_PROGRESS');

-- 5. PRESCRIPTION (đơn thuốc)
TRUNCATE TABLE prescription;
INSERT INTO prescription (prescription_id, record_id) VALUES
(5001, 1001),
(5002, 1002);

-- 6. MEDICINE (thuốc mẫu)
TRUNCATE TABLE medicine;
INSERT INTO medicine (medicine_id, name, active_element, packing_standard, base_unit, sell_price, usage_note) VALUES
(701, 'Paracetamol 500mg', 'Paracetamol', 'Hộp 10 vỉ x 10 viên', 'Viên', 50000, 'Uống sau ăn, mỗi lần 1 viên, ngày 2-3 lần'),
(702, 'Amoxicillin 500mg', 'Amoxicillin', 'Hộp 10 vỉ x 10 viên', 'Viên', 80000, 'Uống trước ăn 1 giờ, ngày 2 lần, mỗi lần 1 viên'),
(703, 'Ibuprofen 200mg', 'Ibuprofen', 'Lọ 100 viên', 'Viên', 60000, 'Uống khi đau, mỗi lần 1 viên, không quá 3 viên/ngày');

-- 7. PRESCRIPTION_ITEM
TRUNCATE TABLE prescription_item;
INSERT INTO prescription_item (prescription_id, medicine_id, unit, quantity, dosage, price) VALUES
(5001, 701, 'Viên', 6, 'Mỗi lần 1 viên, ngày 2 lần sau ăn', 5000),
(5002, 702, 'Viên', 14, 'Ngày 2 lần, mỗi lần 1 viên', 8000);

-- 8. FOLLOW_UP (tái khám)
TRUNCATE TABLE follow_up;
INSERT INTO follow_up (follow_up_id, record_id, patient_id, doctor_id, scheduled_datetime, note, status) VALUES
(2001, 1001, 1, 1, '2025-06-03 09:00:00', 'Tái khám sau 2 tuần', 'PENDING'),
(2002, 1002, 2, 3, '2025-06-01 14:30:00', 'Tái khám kiểm tra lại', 'CONFIRMED');

-- 9. NOTIFICATION (thông báo)
TRUNCATE TABLE notification;
INSERT INTO notification (notification_id, account_id, type, content) VALUES
(3001, 1, 'SYSTEM', 'Chào mừng bạn đến với ClinicPro!'),
(3002, 1, 'EMAIL', 'Bạn có lịch hẹn khám vào ngày 25/05/2025.'),
(3003, 2, 'SYSTEM', 'Bác sĩ đã gửi đơn thuốc cho bạn.');

-- 10. FEEDBACK (phản hồi)
TRUNCATE TABLE feedback;
INSERT INTO feedback (feedback_id, record_id, rating, comment) VALUES
(4001, 1001, 5, 'Rất hài lòng với dịch vụ và bác sĩ.'),
(4002, 1002, 4, 'Hài lòng, nhưng chờ hơi lâu.');

-- 11. CHAT_SESSION
TRUNCATE TABLE chat_session;
INSERT INTO chat_session (session_id, patient_id) VALUES
(5001, 1),
(5002, 2);

-- 12. CHAT_MESSAGE
TRUNCATE TABLE chat_message;
INSERT INTO chat_message (message_id, session_id, sender_type, message_content) VALUES
(6001, 5001, 'USER', 'Tôi bị đau đầu, nên làm gì?'),
(6002, 5001, 'BOT', 'Bạn nên nghỉ ngơi, uống nhiều nước và có thể dùng paracetamol. Nếu đau nhiều hãy đến khám.'),
(6003, 5002, 'USER', 'Chi phí khám chuyên khoa tim mạch bao nhiêu?'),
(6004, 5002, 'BOT', 'Phí khám từ 150.000đ đến 650.000đ tùy bác sĩ. Bạn có thể đặt lịch qua ứng dụng.');

SET FOREIGN_KEY_CHECKS = 1;