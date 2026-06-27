-- Chạy script này trên Database hiện tại để cập nhật theo kiến trúc mới (v2)

-- 1. Bảng `service`: Sửa ENUM service_type và thêm cột estimated_duration
-- Bước 1a: Migrate data cũ IMAGING → X_RAY trước khi đổi ENUM
UPDATE service SET service_type = 'X_RAY' WHERE service_type = 'IMAGING';

-- Bước 1b: Đổi ENUM (thêm X_RAY, ULTRASOUND; bỏ IMAGING)
ALTER TABLE service
MODIFY COLUMN service_type ENUM('EXAM','LAB_TEST','X_RAY','ULTRASOUND','CT_SCAN','MRI','ENDOSCOPY','OTHER') NOT NULL DEFAULT 'LAB_TEST',
ADD COLUMN IF NOT EXISTS estimated_duration INT DEFAULT 15 AFTER service_type;

-- 2. Bảng `appointment`: Ràng buộc booking_mode không cho phép DIRECT (chỉ DOCTOR, EXPERTISE, SERVICE)
ALTER TABLE appointment
MODIFY COLUMN booking_mode ENUM('DOCTOR', 'EXPERTISE', 'SERVICE') DEFAULT 'DOCTOR';

-- 3. Bảng `service_order`: Thêm các trường ghi chú của bác sĩ và tên dịch vụ ngoài luồng
ALTER TABLE service_order
ADD COLUMN custom_service_name VARCHAR(255) NULL AFTER service_id,
ADD COLUMN doctor_note TEXT NULL AFTER custom_service_name;

-- 4. Bảng `service_result`: Đổi attachment_url thành attachment_urls (kiểu JSON) để chứa nhiều ảnh
ALTER TABLE service_result
CHANGE COLUMN attachment_url attachment_urls JSON NULL;
