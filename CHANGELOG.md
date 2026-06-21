# Changelog

## [2026-06-22] - UI/UX & Feedback Features Update

### Backend (`clinic-backend`)
- **Services**: Thêm trường `description` vào bảng `service` và các DTO (`ServiceRequest`, `ServiceResponse`).
- **Feedbacks**: Thêm `appointmentId` vào `DoctorFeedbackResponse` và cập nhật `DoctorFeedbackMapper` để hỗ trợ logic kiểm tra và sửa đánh giá trên Mobile.

### Frontend Mobile (`clinic-frontend/mobile-app`)
- **UI/UX**: 
  - Đồng bộ thiết kế Header sử dụng `GradientAppBar` cho toàn bộ các trang chính và trang con (Lịch hẹn, Hồ sơ y tế, Chat, Tất cả dịch vụ, Tất cả bác sĩ...).
  - Sửa lỗi tràn màn hình (RenderFlex overflowed) ở danh sách Dịch vụ nổi bật trên trang chủ.
  - Thêm nút xóa nhanh (Clear 'x') cho thanh tìm kiếm `ClinicSearchField`.
  - Cập nhật `GradientButton` hỗ trợ trạng thái loading.
- **Data & Logic**:
  - Đổi `viewCount` thành `patientCount` trong `DoctorModel` để hiển thị số lượng bệnh nhân đã khám thực tế.
  - Fix lỗi crash/không load được ảnh (`Invalid image data`) bằng cách đổi sang `ui-avatars.com` với định dạng PNG và xử lý URL localhost cho máy ảo.
  - Tối ưu trang Lịch hẹn (`AppointmentScreen`): Tự động cập nhật ảnh bác sĩ ngay khi dữ liệu tải xong bằng `Consumer2`.
- **Feedback Feature**:
  - Trang Chi tiết lịch hẹn: Tự động kiểm tra trạng thái đánh giá, đổi nút thành "Xem đánh giá" nếu đã đánh giá.
  - Trang Lịch sử đánh giá: Thêm chức năng "Sửa đánh giá" (cho phép sửa trong vòng 24h kể từ lúc tạo).
  - Trang Đánh giá phòng khám: Chặn đánh giá nếu bệnh nhân chưa có lịch hẹn nào ở trạng thái "Đã hoàn thành" (COMPLETED).

### Frontend Web (`clinic-frontend/patient-web`)
- Hiển thị mô tả chi tiết (`description`) của dịch vụ trên giao diện.
- Fix lỗi build TypeScript (`Invalid value for '--ignoreDeprecations'`) trong `tsconfig.app.json`.