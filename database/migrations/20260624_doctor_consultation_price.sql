-- Migration: bỏ hẳn service_id khỏi doctor_service_price (phí khám chỉ theo bác sĩ)
-- Chạy trên DB đã tồn tại. Nên backup trước khi chạy.

-- 1) Gộp trùng staff_id (giữ bản id lớn nhất) nếu có nhiều dòng cũ
DELETE t1 FROM doctor_service_price t1
INNER JOIN doctor_service_price t2
  ON t1.staff_id = t2.staff_id AND t1.id < t2.id;

-- 2) Xóa FK tới service (tên constraint có thể khác — kiểm tra SHOW CREATE TABLE doctor_service_price)
SET @fk_name := (
  SELECT CONSTRAINT_NAME
  FROM information_schema.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'doctor_service_price'
    AND COLUMN_NAME = 'service_id'
    AND REFERENCED_TABLE_NAME IS NOT NULL
  LIMIT 1
);
SET @drop_fk := IF(
  @fk_name IS NOT NULL,
  CONCAT('ALTER TABLE doctor_service_price DROP FOREIGN KEY `', @fk_name, '`'),
  'SELECT 1'
);
PREPARE stmt FROM @drop_fk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3) Xóa cột service_id
ALTER TABLE doctor_service_price DROP COLUMN service_id;

-- 4) Đảm bảo unique 1 bác sĩ = 1 mức phí
ALTER TABLE doctor_service_price
  ADD UNIQUE KEY uk_doctor_price_staff (staff_id);
