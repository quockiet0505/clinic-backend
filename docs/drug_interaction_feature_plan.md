# Kế hoạch Triển khai Chức năng Tương tác thuốc (Frontend)

## Mục tiêu
Nâng cấp giao diện kê đơn thuốc (PrescriptionBuilder) của Bác sĩ trên Admin Web để hỗ trợ tìm kiếm thuốc từ danh mục, cho phép nhập thuốc ngoài, và tích hợp cảnh báo tương tác thuốc thời gian thực.

## 1. Cải tiến Giao diện Nhập Thuốc (PrescriptionBuilder.tsx)
- **Thay thế Input thường thành Searchable Dropdown (Combobox/Autocomplete):**
  - Khi Bác sĩ gõ tên thuốc, hệ thống sẽ gọi API lấy danh sách thuốc tương ứng từ hệ thống (`GET /api/v1/medicines?search=...`) và hiển thị dropdown.
  - Khi chọn thuốc từ hệ thống, `PrescriptionItem` sẽ lưu lại `medicineId` và `medicineName`.
- **Hỗ trợ Thuốc ngoài hệ thống:**
  - Nếu Bác sĩ gõ tên thuốc không có trong danh sách, dropdown sẽ có tuỳ chọn "Thêm thuốc ngoài: [Tên thuốc]".
  - Khi đó, `medicineId` sẽ bằng `null` (hoặc undefined) và chỉ lưu `medicineName` dạng text.

## 2. Trang "Tra cứu Tương tác thuốc" riêng biệt (DrugInteractionChecker.tsx)
- Tạo một trang Frontend (Page) hoàn toàn mới chuyên biệt cho chức năng tra cứu tương tác thuốc (ví dụ URL: `/pharmacy/interaction-checker` hoặc `/medical/interaction-checker`).
- Giao diện tra cứu không nằm dạng Modal (Dialog) nhỏ nữa mà sẽ chiếm một trang độc lập, thoải mái không gian để người dùng thao tác.
- **Tính năng chính của trang:**
  - Cung cấp ô Searchable Dropdown (cho phép chọn nhiều loại thuốc cùng lúc - ít nhất 2 loại).
  - Nút bấm **"Kiểm tra Tương tác"** để gọi API `POST /api/v1/prescriptions/check-interactions`.
- **Trạng thái kết quả:**
  - **An toàn (Safe):** Hiển thị thông báo màu xanh ngọc báo hiệu các thuốc vừa chọn an toàn.
  - **Có tương tác (Warning/Danger):** Hiển thị kết quả tra cứu chi tiết bằng giao diện Accordion (thu gọn/mở rộng) bao gồm Cơ chế, Hậu quả, và Cách xử lý.

## 3. Tích hợp chặn lúc Lưu Đơn Thuốc (Submit)
- Khi Bác sĩ bấm nút "Lưu Đơn Thuốc" trong lúc khám bệnh, Backend sẽ tự động kiểm tra tương tác.
- Nếu Backend trả về lỗi (do API Create có tích hợp sẵn chặn tương tác), Frontend phải bắt lỗi `RuntimeException` và hiển thị Modal hoặc Toast cảnh báo đỏ để Bác sĩ cân nhắc sửa đổi.
- Có thể thiết kế thêm checkbox "Bỏ qua cảnh báo và tiếp tục lưu" nếu Bác sĩ chủ động quyết định (Yêu cầu thêm tính năng `override_warning` ở Backend sau này nếu cần thiết).

## 4. Các API cần sử dụng
- `GET /api/v1/medicines?search={query}&size=10`: Lấy danh sách thuốc.
- `POST /api/v1/prescriptions/check-interactions`: API kiểm tra độc lập danh sách `[medicineId1, medicineId2, ...]`.
- `POST /api/v1/prescriptions`: Gửi data lưu, bao gồm mảng `{ medicineId, dosage, quantity }`. (Thuốc ngoài có thể cần cập nhật Backend để chấp nhận `medicineId = null` và truyền thêm `medicineName`).
