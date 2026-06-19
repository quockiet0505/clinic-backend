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
