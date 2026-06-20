-- Migration: thêm chỉ số sinh tồn tự khai báo vào patient_vital_profile
-- Chạy nếu DB đã tạo từ phiên bản cũ (chưa có weight, blood_pressure, pulse)

ALTER TABLE patient_vital_profile
  ADD COLUMN IF NOT EXISTS weight DECIMAL(5,2) NULL AFTER height,
  ADD COLUMN IF NOT EXISTS blood_pressure VARCHAR(20) NULL AFTER weight,
  ADD COLUMN IF NOT EXISTS pulse INT NULL AFTER blood_pressure;
