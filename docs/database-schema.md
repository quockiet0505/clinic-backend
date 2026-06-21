# 🗄️ Thiết kế Cơ sở dữ liệu (Database Schema)

Dưới đây là đặc tả hệ thống cơ sở dữ liệu (RDBMS) sử dụng trong dự án.

## 1. Quy ước chung
- Tên bảng: Số nhiều, viết thường phân cách bằng dấu gạch dưới (VD: `users`, `medical_records`).
- Khóa chính (Primary Key): Trường `id`, kiểu `BIGINT` (Auto Increment hoặc UUID).
- Trường Audit: Mọi bảng bắt buộc phải có: `created_at`, `updated_at`, `created_by`, `updated_by`.
- Xóa mềm (Soft Delete): Bảng quan trọng dùng trường `is_deleted` thay vì xóa vật lý (`DELETE`).

## 2. Core Entities (Các bảng lõi)

### Bảng `users`
Lưu trữ thông tin xác thực và tài khoản.
- `id` (PK)
- `email` (Unique)
- `password_hash`
- `role` (ADMIN, DOCTOR, PATIENT)
- `is_active`

### Bảng `appointments`
Thông tin cuộc hẹn giữa bệnh nhân và bác sĩ.
- `id` (PK)
- `patient_id` (FK -> users)
- `doctor_id` (FK -> users)
- `appointment_date` (DATETIME)
- `status` (PENDING, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW)
- `reason` (TEXT)

### Bảng `medical_records`
Lưu trữ kết quả khám bệnh.
- `id` (PK)
- `appointment_id` (FK -> appointments)
- `diagnosis` (TEXT) - Chẩn đoán
- `prescription` (JSON) - Đơn thuốc
- `notes` (TEXT)

## 3. Indexes & Constraints
- Đánh `INDEX` trên các trường thường xuyên tìm kiếm như `email`, `appointment_date`, `status`.
- Đảm bảo Foreign Keys (FK) được mapping đúng `ON DELETE RESTRICT` để tránh mất dữ liệu liên đới.
