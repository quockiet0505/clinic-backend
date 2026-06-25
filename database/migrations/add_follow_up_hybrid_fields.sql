-- Migration: Hybrid (C) — liên kết follow_up ↔ appointment + audit nhắc tái khám
-- Chạy trên database đã có clinic_system.sql (bảng follow_up cũ)
-- Tham chiếu: database/flow/consultation_clinical_workflow_analysis.md §5.5.1

ALTER TABLE follow_up
    ADD COLUMN appointment_id INT NULL AFTER doctor_id,
    ADD COLUMN confirmed_at DATETIME NULL AFTER status,
    ADD COLUMN reminder_sent_at DATETIME NULL AFTER confirmed_at,
    ADD COLUMN cancel_reason VARCHAR(255) NULL AFTER note;

ALTER TABLE follow_up
    ADD CONSTRAINT fk_followup_appointment
        FOREIGN KEY (appointment_id) REFERENCES appointment(appointment_id);

CREATE INDEX idx_followup_appointment ON follow_up(appointment_id);
CREATE INDEX idx_followup_status_datetime ON follow_up(status, scheduled_datetime);
