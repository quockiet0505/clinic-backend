# Cơ sở dữ liệu (Database)

## Khởi tạo

- **Tên database mặc định**: `clinic_system`
- **File script khởi tạo**: Nằm trong thư mục `database/clinic_system.sql` (và các file update đi kèm như `update_doctors.sql`).
- **Cách chạy**: Mở file SQL này trong MySQL Workbench, DBeaver hoặc phpMyAdmin và chạy toàn bộ script để tạo bảng và nạp dữ liệu mẫu (seed data).

## Các bảng chính trong hệ thống

- `staff`: Lưu thông tin nhân viên, bác sĩ, quản trị viên. Cột `staff_type` phân biệt vai trò.
- `patient`: Lưu thông tin bệnh nhân.
- `appointment`: Lưu lịch hẹn khám bệnh của bệnh nhân với bác sĩ.
- `service_catalog` / `service`: Lưu danh mục các dịch vụ khám, xét nghiệm, chẩn đoán hình ảnh.
- `service_order`: Lưu các chỉ định dịch vụ của bác sĩ dành cho bệnh nhân trong quá trình khám.
- `service_result`: Lưu kết quả thực hiện dịch vụ (ví dụ: kết quả xét nghiệm, file đính kèm).
- `medical_record`: Hồ sơ bệnh án của bệnh nhân.
- `patient_vital_profile`: Hồ sơ sức khoẻ bệnh nhân (chiều cao, cân nặng, huyết áp, mạch, nhóm máu, dị ứng, tiền sử bệnh). Bệnh nhân cập nhật qua API profile.
- `feedback` / `doctor_review`: Đánh giá phòng khám và bác sĩ từ bệnh nhân.

## Migration bổ sung

Nếu database đã tạo trước khi có luồng đặt lịch mới / AI gợi ý chuyên khoa, chạy:

```sql
-- database/migrations/add_appointment_booking_fields.sql
```

**Bảng/cột cần update (P0):**

| Đối tượng | Thay đổi |
|-----------|----------|
| `appointment` | Thêm `expertise_id`, `suggested_expertise_id`, `booking_mode`, `is_ai_suggested`, `is_deleted` |
| `appointment` | UNIQUE `idx_unique_slot` (doctor + date + time + is_deleted) |
| Thứ tự bảng | **10** SERVICE → **11** DOCTOR_SERVICE_PRICE → **12** APPOINTMENT (FK hợp lệ) |
| `appointment_service` | **Bảng mới** — nhiều dịch vụ trong một lịch (gói khám) |

**Chưa cần ngay (P2/P3 — roadmap):** `bill`, `bill_item`, `refund_log`, `inventory_transaction`, `lab_room_schedule`.

Chi tiết luồng: [business-flows.md](business-flows.md)

Nếu database đã tạo trước khi có các cột vital mới, chạy:

```sql
-- database/migrations/add_patient_vital_profile_metrics.sql
ALTER TABLE patient_vital_profile
  ADD COLUMN IF NOT EXISTS weight DECIMAL(5,2) NULL AFTER height,
  ADD COLUMN IF NOT EXISTS blood_pressure VARCHAR(20) NULL AFTER weight,
  ADD COLUMN IF NOT EXISTS pulse INT NULL AFTER blood_pressure;
```

