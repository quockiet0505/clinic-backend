-- Migration: Bổ sung schema đặt lịch (Luồng 1 + tích hợp AI)
-- Chạy trên database clinic_system SAU khi đã có clinic_system.sql
-- Tham chiếu: docs/business-flows.md

-- ============================================================
-- P0 — Bắt buộc cho luồng đặt lịch mới + AI gợi ý chuyên khoa
-- ============================================================

-- 1) Bảng appointment — thêm chuyên khoa & mode đặt lịch
ALTER TABLE appointment
  ADD COLUMN expertise_id INT NULL
    COMMENT 'Chuyên khoa bệnh nhân chọn (khi chưa/chưa cố định bác sĩ)'
    AFTER service_id,
  ADD COLUMN suggested_expertise_id INT NULL
    COMMENT 'Chuyên khoa AI gợi ý từ appointment.note / chat'
    AFTER expertise_id,
  ADD COLUMN booking_mode ENUM('DOCTOR', 'EXPERTISE', 'SERVICE', 'DIRECT') NOT NULL DEFAULT 'DOCTOR'
    COMMENT 'Cách bệnh nhân vào form: từ BS / khoa / dịch vụ / mô tả triệu chứng'
    AFTER suggested_expertise_id,
  ADD COLUMN is_ai_suggested BOOLEAN NOT NULL DEFAULT FALSE
    COMMENT 'Lịch được AI gợi ý/đặt hộ'
    AFTER booking_mode;

ALTER TABLE appointment
  ADD CONSTRAINT fk_appt_expertise
    FOREIGN KEY (expertise_id) REFERENCES expertise(expertise_id),
  ADD CONSTRAINT fk_appt_suggested_expertise
    FOREIGN KEY (suggested_expertise_id) REFERENCES expertise(expertise_id);

-- Index tra cứu theo chuyên khoa
CREATE INDEX idx_appt_expertise ON appointment (expertise_id);
CREATE INDEX idx_appt_booking_mode ON appointment (booking_mode);

-- 2) Nhiều dịch vụ trong một lịch (gói khám / trọn gói)
CREATE TABLE IF NOT EXISTS appointment_service (
  appointment_id INT NOT NULL,
  service_id INT NOT NULL,
  sequence_order INT NOT NULL DEFAULT 1 COMMENT 'Thứ tự thực hiện trong gói',
  PRIMARY KEY (appointment_id, service_id),
  FOREIGN KEY (appointment_id) REFERENCES appointment(appointment_id) ON DELETE CASCADE,
  FOREIGN KEY (service_id) REFERENCES service(service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3) Chống trùng slot bác sĩ (khuyến nghị — chỉ áp khi main_doctor_id NOT NULL)
-- Lưu ý: MySQL cho phép nhiều NULL trong UNIQUE; slot không BS (Lab) không bị ảnh hưởng.
ALTER TABLE appointment
  ADD UNIQUE KEY uq_appt_doctor_slot (main_doctor_id, appointment_date, time_start, is_deleted);

-- ============================================================
-- P1 — Tuỳ chọn: trạng thái chờ AI gợi ý chuyên khoa
-- Chỉ chạy nếu team quyết định dùng enum riêng (thay vì PENDING + suggested_expertise_id)
-- ============================================================
-- ALTER TABLE appointment
--   MODIFY COLUMN status ENUM(
--     'PENDING',
--     'PENDING_SPECIALTY',
--     'CONFIRMED',
--     'CHECKED_IN',
--     'IN_PROGRESS',
--     'WAITING_RESULT',
--     'COMPLETED',
--     'SKIPPED',
--     'CANCELLED',
--     'NO_SHOW'
--   ) DEFAULT 'PENDING';

-- ============================================================
-- P2 — Tương lai (CHƯA triển khai code) — ghi để roadmap
-- ============================================================
-- bill, bill_item, refund_log          → Luồng 5 Thanh toán
-- inventory_transaction                → Luồng 4 Kho dược
-- lab_room_schedule                    → Slot xét nghiệm không cần bác sĩ
-- doctor_review.appointment_id         → Đánh giá gắn đúng lịch khám (nếu cần)
