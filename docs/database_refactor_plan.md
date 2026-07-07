# Kế Hoạch Chuẩn Hóa Database (Kế Toán - Thanh Toán & Tương Tác Thuốc)

## 1. Mục tiêu
- Xóa bỏ sự lộn xộn, dư thừa trong cách lưu trữ cấu hình giá dịch vụ và giá khám Bác sĩ.
- Đảm bảo "Bảo toàn giá trị lịch sử" (Đóng băng giá) cho cả tiền khám và tiền dịch vụ để Biên lai không bị nhảy số khi thay đổi giá trong tương lai.
- Tích hợp thêm bảng `drug_interaction` (Tương tác thuốc) theo yêu cầu mới.

## 2. Phân tách rõ ràng nơi lưu trữ
- **Bảng `medical_record` (Bệnh án):** Sẽ chịu trách nhiệm lưu Trạng thái thanh toán của **Tiền Khám Bác sĩ**.
- **Bảng `service_order` (Chỉ định dịch vụ):** Sẽ chịu trách nhiệm lưu Trạng thái thanh toán của **Tiền Dịch vụ Cận lâm sàng** (Siêu âm, xét nghiệm...).

---

## 3. Các thay đổi chi tiết trên Database

### 3.1. Nhóm Bảng Cấu Hình Giá (Danh Mục)
Mục đích: Chỉ lưu 2 cột `original_price` (Giá gốc) và `discount_amount` (Tiền giảm). Giá cuối cùng Backend tự tính.

#### Bảng `doctor_service_price` (Cấu hình giá khám của Bác sĩ)
- [DELETE] Cột `price` (Xóa bỏ vì dư thừa).
- [MODIFY] Đổi tên cột `discount_price` thành `discount_amount`.

#### Bảng `service` (Danh mục dịch vụ cận lâm sàng)
- [MODIFY] Đổi tên cột `discount_price` thành `discount_amount`.

---

### 3.2. Nhóm Bảng Lịch Sử (Đóng băng giá)
Mục đích: Lưu lại toàn bộ 3 con số (Gốc, Giảm, Thực thu) tại thời điểm khám.

#### Bảng `medical_record` (Lưu Tiền Khám Bác sĩ)
Thêm mới 3 cột để chốt sổ giá khám:
- [NEW] `consultation_original_fee` DECIMAL(10,2) (Tiền khám gốc)
- [NEW] `consultation_discount` DECIMAL(10,2) (Tiền khám được giảm)
- [NEW] `consultation_final_fee` DECIMAL(10,2) (Tiền khám thực thu)

#### Bảng `service_order` (Lưu Tiền Dịch vụ)
- [NEW] `service_original_fee` DECIMAL(10,2) (Giá dịch vụ gốc)
- [NEW] `service_discount` DECIMAL(10,2) (Tiền được giảm)
- [NEW] `service_final_fee` DECIMAL(10,2) (Tiền dịch vụ thực thu)

---

### 3.3. Tích hợp Bảng Tương Tác Thuốc (Drug Interaction)
Thêm mới bảng `drug_interaction` để phục vụ tính năng cảnh báo khi kê đơn.
- [NEW] Bảng `drug_interaction` gồm các cột: `interaction_id`, `active_element_1`, `active_element_2`, `mechanism`, `consequence`, `management`, `created_at`, `updated_at`.

---

## 4. Tác động tới Code Java Backend
- Cập nhật lại các Entity: `DoctorServicePrice`, `Service`, `MedicalRecord`, `ServiceOrder`.
- Xóa bỏ/sửa lại các field dư thừa (như `consultationFee` hiện tại đang bị mapping sai kiểu).

---

## 5. Phân tích và Tích hợp Tương Tác Thuốc (Drug Interaction)

**Chiến lược xử lý dữ liệu và thuật toán mới:**

1. **Thuật toán xử lý Thuốc nhiều thành phần (Multiple Active Elements):**
   - **Thực tế:** Thuốc trên thị trường thường có dạng bào chế kết hợp (Ví dụ: *Augmentin* chứa *Amoxicillin, Acid clavulanic*).
   - **Lưu trữ & Phân tách:** Cột `active_element` trong bảng `medicine` sẽ lưu các thành phần cách nhau bằng **dấu phẩy (`,`)**.
   - **Thuật toán (Checking Logic):** 
     - Khi kê đơn, Backend sẽ lấy chuỗi `active_element` và gọi hàm `split(",")`.
     - **Sanitize (Làm sạch):** Backend sẽ loại bỏ toàn bộ khoảng trắng thừa (`trim()`), chuẩn hóa chữ thường (lowercase) và loại bỏ các ký tự đặc biệt không hợp lệ để tránh lỗi sai khác do người dùng nhập thừa dấu cách (ví dụ: `" Amoxicillin ,  Acid clavulanic "` sẽ được biến thành mảng chuẩn `["amoxicillin", "acid clavulanic"]`).
     - Cuối cùng, hệ thống sẽ đem mảng này đi quét vào bảng `drug_interaction`. Đảm bảo độ chính xác tuyệt đối.

2. **Kế hoạch Tách 3 File Seed (Ưu tiên Dữ liệu Thật):**
   Thay vì nhồi chung vào 1 file, tôi sẽ thiết kế 3 file Seed riêng biệt để hệ thống hóa dữ liệu, sử dụng **hoàn toàn dữ liệu thuốc thực tế (Real Data)**:

   - **File 1: `seed_1_drug_interactions.sql`**
     - Chức năng: Nạp toàn bộ các luật tương tác thuốc từ file JSON vào bảng `drug_interaction`.
     - *Đây là bộ não (Rule Engine) của hệ thống cảnh báo.*

   - **File 2: `seed_2_medicines_safe.sql` (50 Thuốc Bình Thường / An Toàn)**
     - Chức năng: Nạp 50 loại thuốc phổ biến không gây tương tác nghiêm trọng (nhóm Vitamin, khoáng chất, thuốc bổ, giảm đau cơ bản).
     - *Dữ liệu thật:* Ví dụ: **Panadol Extra** (`active_element: Paracetamol, Caffeine`), **Vitamin C 500mg** (`active_element: Acid ascorbic`), **Oresol** (`active_element: Glucose, Natri clorid, Kali clorid`)...
     - *Mục đích:* Dùng để đối chứng. Khi Bác sĩ kê các thuốc này, hệ thống phải chạy mượt mà, không cảnh báo sai (False Positive).

   - **File 3: `seed_3_medicines_interacting.sql` (50 Thuốc Có Tương Tác)**
     - Chức năng: Nạp 50 loại thuốc chuyên khoa, kháng sinh, kháng viêm... được lấy **trực tiếp từ các cặp tương tác** trong file JSON gốc.
     - *Dữ liệu thật:* Ví dụ: Cặp tương tác *(Aceclofenac - Ketorolac)*. Ta sẽ có thuốc **Airtal 100mg** (`active_element: Aceclofenac`) và **Tora 30mg** (`active_element: Ketorolac`). Hoặc các thuốc kết hợp phức tạp.
     - *Mục đích:* Dùng để test tính năng cảnh báo. Khi Bác sĩ vô tình kê đơn 2 thuốc này, màn hình phải lập tức bật thông báo đỏ chót theo đúng `consequence` và `management`.

**Kế hoạch tích hợp (Đã chốt):**
1. **Cập nhật Schema:** Sửa lỗi cú pháp bảng `medicine` và thêm `CREATE TABLE drug_interaction` vào DB.
2. **Tạo dữ liệu Mẫu & Nạp DB:** Chạy script tạo 3 file Seed, import vào database.
3. **Backend Entities:** Tạo entity `DrugInteraction.java`.
4. **Logic đa thành phần:** Viết thuật toán `split(",")` + `trim()`/sanitize tại `PrescriptionService`.
5. **Export DB:** Sau khi chạy seed xong, dùng script Python để **dump database ra lại file `clinic_system.sql`**, đồng thời tự động đánh số thứ tự (STT) đàng hoàng cho từng table (VD: `-- 1. Table structure for table account`) đúng y như định dạng ban đầu của file.

## CẬP NHẬT KẾT QUẢ TRIỂN KHAI (Hoàn thành Tính Năng Tương Tác Thuốc)

Tính năng cảnh báo tương tác thuốc đã được triển khai hoàn tất 100% từ Database đến Backend theo đúng Kế hoạch trên:

1. **Cơ sở dữ liệu & Cấu trúc:**
   - File `clinic_system.sql` đã được chuẩn hóa thành bản Schema Only: được đánh STT từng bảng, có comment giải thích rõ ràng từng bảng (`-- CHỨC NĂNG BẢNG xxx`) và từng trường dữ liệu phức tạp. Lỗi lặp bảng `drug_interaction` đã được xóa bỏ.
   - Thư mục `database/backup` chứa file `clinic_system_full.sql` (bản dump đầy đủ dữ liệu chuẩn UTF-8 Tiếng Việt bằng `mysqldump`).
   - Giữ nguyên 3 file Seed (`seed_1_drug_interactions.sql`, `seed_2_medicines_safe.sql`, `seed_3_medicines_interacting.sql`) để dùng về sau.

2. **Cập nhật Backend Java (`PrescriptionService` & `PrescriptionController`):**
   - Đã tích hợp hàm quét chéo dữ liệu thuốc đa thành phần (`checkInteractions()`). Thuật toán dùng `.split("[,+]")`, kết hợp `.trim()` và `.toLowerCase()` để làm sạch, chặn lỗi gõ sai chữ hoa/chữ thường.
   - **Chặn lưu toa thuốc:** Đã chặn ngay ở hàm `create()` của `PrescriptionService`. Khi bác sĩ ấn lưu đơn, nếu danh sách thuốc có tương tác, Backend sẽ ném ra `RuntimeException` cảnh báo màu đỏ với chi tiết Cơ chế, Hậu quả, và Hướng dẫn khắc phục.
   - **API Kiểm tra Độc lập:** Đã tạo thêm chức năng chuyên dụng ở `PrescriptionController` (`POST /api/v1/prescriptions/check-interactions`). Bác sĩ chỉ cần truyền vào danh sách ID của các loại thuốc, API sẽ trả về `An toàn` hoặc danh sách Cảnh báo chi tiết. Tính năng này giúp Bác sĩ có thể bấm nút kiểm tra nháp ngay trong lúc nhập thuốc, không cần phải đợi ấn Lưu mới biết lỗi.
