# Phân tích Cấu trúc Database & Các luồng nghiệp vụ

Tài liệu này phân tích toàn bộ cấu trúc cơ sở dữ liệu `clinic_system` cũng như chi tiết các luồng dữ liệu chính trong hệ thống phòng khám, đặc biệt tập trung vào luồng đặt lịch khám.

---

## 1. Phân tích các bảng dữ liệu (Theo nhóm chức năng)

### Nhóm 1: Quản trị & Phân quyền
- **`role`**: Lưu các quyền trong hệ thống (ADMIN, DOCTOR, PATIENT, STAFF, LAB_TECH).
- **`account`**: Lưu thông tin đăng nhập, bảo mật (mật khẩu, email, trạng thái khóa tài khoản nếu đăng nhập sai nhiều lần).
- **`account_role`**: Bảng trung gian phân quyền cho tài khoản.

### Nhóm 2: Nhân sự & Ngày phép
- **`expertise`**: Danh mục chuyên khoa (Nội, Ngoại, Nhi, Mắt...).
- **`staff`**: Chứa thông tin của bác sĩ, nhân viên, kỹ thuật viên Lab. Map với `expertise_id` nếu là bác sĩ.
- **`staff_schedule`**: Lịch làm việc của nhân viên (Ngày, giờ bắt đầu, giờ kết thúc, trạng thái WORKING/OFF).
- **`leave_request`**: Quản lý đơn xin nghỉ phép của nhân viên. Gồm ngày bắt đầu, ngày kết thúc và trạng thái duyệt (APPROVED/PENDING/REJECTED). Dùng để chặn lịch hẹn vào ngày nghỉ.

### Nhóm 3: Bệnh nhân & Sức khỏe
- **`patient`**: Hồ sơ hành chính của bệnh nhân.
- **`patient_vital_profile`**: Hồ sơ sức khỏe tổng quát (Chiều cao, Cân nặng, Huyết áp, Nhóm máu, Dị ứng...). 

### Nhóm 4: Lịch hẹn & Đặt lịch
- **`appointment`**: Bảng lõi quản lý lịch hẹn. Quản lý trạng thái từ lúc đặt lịch (PENDING) -> Xác nhận (CONFIRMED) -> Đến phòng khám (CHECKED_IN) -> Đang khám (IN_PROGRESS) -> Hoàn thành (COMPLETED) hoặc Hủy (CANCELLED).

### Nhóm 5: Dịch vụ & Bảng giá
- **`service`**: Danh mục dịch vụ khám, xét nghiệm, chụp chiếu.
- **`doctor_service_price`**: Bảng giá riêng biệt của từng dịch vụ áp dụng cho từng bác sĩ (nếu bác sĩ có mức phí khác nhau).

### Nhóm 6: Khám bệnh & Cận lâm sàng
- **`medical_record`**: Hồ sơ bệnh án của một ca khám. Chứa chẩn đoán (diagnosis) và hướng điều trị (treatment).
- **`medical_record_vital`**: Sinh hiệu đo trực tiếp trong lần khám đó.
- **`service_order`**: Phiếu chỉ định xét nghiệm/chụp chiếu do bác sĩ yêu cầu trong ca khám.
- **`service_result`**: Kết quả xét nghiệm/chụp chiếu được KTV Lab cập nhật lại.

### Nhóm 7: Kho Dược & Đơn thuốc
- **`medicine`**: Danh mục thuốc.
- **`prescription`**: Đơn thuốc của ca khám.
- **`prescription_item`**: Các loại thuốc cụ thể và liều lượng được kê trong đơn.

### Nhóm 8: Chăm sóc & Thông báo
- **`follow_up`**: Lịch hẹn tái khám.
- **`notification`**: Hệ thống thông báo tự động (Email/Hệ thống).
- **`feedback` & `doctor_review`**: Phản hồi của bệnh nhân về dịch vụ và bác sĩ.

### Nhóm 9: Trợ lý AI (Chatbot)
- **`chat_session` & `chat_message`**: Lưu trữ lịch sử chat của bệnh nhân với hệ thống AI để làm cơ sở gợi ý chuyên khoa hoặc kiểm toán.

### Nhóm 10: Cấu hình hệ thống (Setting)
- **`quick_action`, `logo_setting`, `banner_setting`, `system_setting`**: Lưu các cấu hình động trên giao diện.
- **`contact_message`**: Tin nhắn liên hệ từ khách vãng lai gửi tới phòng khám.

---

## 2. Chi tiết các luồng dữ liệu (Data Flows)

### 2.1. Luồng đặt lịch khám (Appointment Flows)
Có rất nhiều biến thể khi đặt lịch, được định nghĩa thông qua cột `booking_mode` và `appointment_type`.

**A. Các trường hợp (Modes) đặt lịch:**
1. **DOCTOR (Chọn đích danh bác sĩ)**: 
   - Bệnh nhân chọn 1 bác sĩ cụ thể -> Cần kiểm tra lịch trống (`staff_schedule`) của bác sĩ đó.
   - Khoa khám sẽ tự động lấy theo chuyên khoa của bác sĩ.
2. **EXPERTISE (Chọn chuyên khoa)**: 
   - Bệnh nhân không biết chọn bác sĩ nào, chỉ chọn "Khoa Nội".
   - Hệ thống sẽ tự động tìm các bác sĩ thuộc khoa Nội đang rảnh và sinh slot trống. Khi đặt, hệ thống tự assign (gán) một bác sĩ phù hợp.
3. **SERVICE (Đặt lịch dịch vụ độc lập)**: 
   - Đặt lịch thẳng cho Lab (Xét nghiệm máu) hoặc Chụp chiếu.
   - Không cần bác sĩ khám, hệ thống sẽ gán vào slot của Kỹ thuật viên Lab (`LAB_TECH`).
4. **DIRECT (Đặt lịch qua AI / Không chọn gì)**: 
   - Bệnh nhân chỉ nói triệu chứng qua AI (VD: "Tôi bị đau đầu").
   - AI dựa vào knowledge base sinh ra `suggested_expertise_id` (Gợi ý chuyên khoa Thần Kinh).
   - Lịch hẹn được tạo ra với chế độ chờ gán bác sĩ, có kèm theo đánh dấu `is_ai_suggested = true`.

**B. Theo hình thức (Online vs Walk-in):**
- **ONLINE**: Bệnh nhân đặt trước. Phải đặt trước 24h. Hệ thống sẽ kiểm tra xem bệnh nhân có bị khóa tài khoản do spam (hủy lịch muộn >= 3 lần) hay không. Tạo xong trạng thái là `PENDING`.
- **WALK_IN**: Bệnh nhân tới trực tiếp quầy lễ tân. Lễ tân tạo lịch trên hệ thống. Không cần kiểm tra 24h, lịch tạo ra sẽ có `checkin_time` ngay lập tức và được xếp số thứ tự (`queue_number`) để vào khám.

**C. Rule sinh slot trống (Cập nhật mới nhất):**
- Loại bỏ các ngày Thứ 7, Chủ Nhật.
- Loại bỏ các ngày nghỉ lễ cố định (Tết Dương Lịch, 30/4, 1/5, 2/9...).
- Loại bỏ những ngày mà bác sĩ có đơn xin nghỉ phép (`leave_request`) được phê duyệt.
- Nếu ngày đó không có lịch tùy chỉnh trong `staff_schedule`, tự động sinh slot mặc định: **Sáng (07:30 - 11:30)** và **Chiều (13:30 - 17:00)**. (Lưu ý: 1 Slot = 30 phút).

### 2.2. Luồng Khám bệnh & Cận lâm sàng
1. **Check-in**: Lễ tân xác nhận bệnh nhân tới -> `appointment` chuyển thành `CHECKED_IN`.
2. **Khám lâm sàng**: Điều dưỡng đo sinh hiệu (`medical_record_vital`). Bác sĩ mở `medical_record` -> Trạng thái thành `IN_PROGRESS`.
3. **Chỉ định (Tùy chọn)**: Nếu cần xét nghiệm, Bác sĩ tạo `service_order`. Bệnh án chuyển sang `WAITING_RESULT`.
4. **Trả kết quả**: KTV Lab làm xét nghiệm, nhập kết quả vào `service_result`. 
5. **Kết luận**: Bác sĩ đọc kết quả, cập nhật chẩn đoán.

### 2.3. Luồng Kê đơn thuốc (Prescription)
1. Bác sĩ tạo phiếu `prescription` dựa trên bệnh án.
2. Thêm nhiều `prescription_item` vào đơn thuốc (Bao gồm thuốc gì, định lượng, cách dùng).
3. Đơn thuốc được chuyển qua nhà thuốc phòng khám (dược sĩ sẽ thấy danh sách).

### 2.4. Luồng Chăm sóc Khách hàng sau khám
1. **Tái khám**: Bác sĩ tạo phiếu `follow_up` hẹn ngày bệnh nhân quay lại. 
2. **Review**: Sau khi khám xong (`checkout_time`), hệ thống gửi yêu cầu đánh giá để bệnh nhân viết `doctor_review`.
3. Bệnh nhân có thể xem lại toàn bộ hồ sơ khám bệnh và đơn thuốc trên app/web của mình.
