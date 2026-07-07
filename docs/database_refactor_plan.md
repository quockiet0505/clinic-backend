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

> [!IMPORTANT]
> Bạn vui lòng đọc kỹ Kế hoạch này. Nếu bạn đồng ý 100%, hãy phản hồi "Triển khai đi" để tôi bắt đầu viết Script can thiệp thẳng vào file SQL và Code Java Backend.
