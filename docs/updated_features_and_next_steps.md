# Tài liệu Tổng hợp Cập nhật Hệ thống & Hướng đi Tiếp theo

Tài liệu này tổng hợp toàn bộ các phần việc đã chỉnh sửa thuộc Pha 1, Pha 2, Pha 3 và phác thảo chi tiết kế hoạch thực hiện Pha 4 (AI & Đánh giá) kế tiếp, cùng những thông tin người dùng cần chuẩn bị.

---

## 1. Danh sách các nội dung đã chỉnh sửa (Pha 1, 2 & 3)

### Pha 1: Hóa đơn & Thanh toán QR (Đóng băng giá)
- **Tự động sinh Hóa đơn**: Ngay khi ca khám chuyển sang trạng thái hoàn thành (`MedicalRecord.status = DONE`), hệ thống tự động sinh hóa đơn đóng băng giá lâm sàng (chỉ gồm phí khám và các dịch vụ chỉ định từ bệnh án).
- **Thanh toán VietQR động**:
  - Giao diện **Admin Web** (Lễ tân) hiển thị mã QR động chứa số tiền thanh toán thực tế và nội dung chuyển khoản bắt buộc dạng `BILL{id}`.
  - Giao diện **Patient Web** và **Mobile App** (Flutter) cho phép bệnh nhân xem danh sách hóa đơn, quét mã VietQR để chuyển tiền, sau đó gửi yêu cầu xác nhận *"Tôi đã chuyển khoản thành công"*.
  - Hóa đơn tự động chuyển sang trạng thái chờ đối soát (`PENDING_VERIFY`). Lễ tân có quyền phê duyệt (`PAID`) hoặc từ chối (`UNPAID`) trên trang quản trị Admin.

### Pha 2: Khóa tài khoản & Tách vai trò Lễ tân/Điều dưỡng (Bỏ STAFF)
- **Loại bỏ role STAFF**: Phân rã hoàn toàn vai trò `STAFF` chung thành hai vai trò nghiệp vụ rõ rệt là Lễ tân (`RECEPTIONIST`) và Điều dưỡng (`NURSE`).
- **Phân quyền ngầm (Implicit Role Mapping)**:
  - Cấu hình backend Spring Security tự động map vai trò `RECEPTIONIST` và `NURSE` có thêm authority `ROLE_STAFF` để các API Controller cũ được bảo vệ bằng `@PreAuthorize("hasRole('STAFF')")` hoạt động trơn tru.
  - Cấu hình frontend Admin Web (`RoleGuard.tsx` và `authApi.ts`) ánh xạ ngầm tương tự để Tiếp tân/Điều dưỡng truy cập được các route STAFF cũ mà không bị chặn (Access Denied).
- **Khóa tài khoản đăng nhập**: Thêm API/UI bật/tắt trạng thái hoạt động (`isActive = 0 / 1`) của tất cả tài khoản bệnh nhân và nhân viên.
- **Phạt spam hủy hẹn**: Bệnh nhân hủy hẹn muộn $\ge 3$ lần sẽ bị tự động khóa quyền đặt lịch khám trực tuyến. Lễ tân có nút mở khóa đặt lịch cho bệnh nhân (Đổi nhãn `[SPAM]` thành `[UNLOCKED]`).
- **Di chuyển dữ liệu cũ**: Đồng bộ hóa dữ liệu cũ trong bảng `staff` bằng cách cập nhật tất cả bản ghi có loại nhân viên cũ là `'STAFF'` sang `'RECEPTIONIST'` (Lễ tân) trực tiếp trên CSDL và chuyển đổi định nghĩa cột enum `staff_type` tương ứng, giúp ứng dụng khởi chạy thành công không bị lỗi phân tích enum (`IllegalArgumentException`).
- **Nợ kỹ thuật (Chưa hoàn thiện)**: Hiện tại Backend và Admin Web mới chỉ đổi tên `STAFF` thành `RECEPTIONIST` và `NURSE` (kế thừa lại 100% quyền hạn của STAFF cũ). Chưa phân tách ranh giới chức năng rõ ràng giữa 2 role này (Ví dụ: Lễ tân không được xem kho thuốc, Y tá không được xem doanh thu). Cần thực hiện phân quyền chi tiết cho Sidebar và Controller trong tương lai.

### Pha 3: Dữ liệu 100 Thuốc thật
- **Nguồn dữ liệu tham khảo**: Cổng thông tin Ngân hàng Dữ liệu Ngành Dược (Drugbank Vietnam - https://drugbank.vn) và Hệ thống Cơ sở dữ liệu Dược học Quốc tế DrugBank Online (https://go.drugbank.com).
- **Loại bỏ quản lý giá**: Xóa sạch toàn bộ các cột `sell_price`, `consultation_fee` và `service_fee` khỏi bảng `medicine` trong CSDL MySQL và các file SQL seed.
- **Nạp 100 thuốc thật**:
  - `seed_2_medicines_safe.sql`: 50 loại thuốc thật, an toàn (Paracetamol, Claritin, Nexium, Smecta, v.v.).
  - `seed_3_medicines_interacting.sql`: 50 loại thuốc thật có hoạt chất dễ gây tương tác chéo (Brista & Toradol, Viagra & Nitromint, Zocor & Klacid, v.v.) khớp chuẩn 100% với cấu trúc tương tác trong `seed_1_drug_interactions.sql`.
- **Đồng bộ hóa & Backup**: Import trực tiếp vào CSDL thực tế và xuất tệp backup chuẩn `mysqldump` tại [clinic_system_full.sql](file:///d:/Information%20Technology/LV_CNTT/core_code/clinic-backend/database/backup/clinic_system_full.sql).

### Pha 3.5: Tích hợp Webhook Sepay (Thanh toán tự động)
- **Triển khai WebhookController**: Mở endpoint `/api/v1/webhooks/sepay` để nhận giao dịch từ ngân hàng.
- **Phân tích lỗi khi test (2026-07-12)**:
  1. Lỗi `403 Forbidden` ban đầu: Do người dùng cấu hình URL trên Sepay bị thiếu đường dẫn endpoint (chỉ trỏ vào root `/` thay vì `/api/v1/webhooks/sepay`).
  2. Lỗi `401 Unauthorized` (Invalid API Key): Sepay gửi API Key trong một custom header khác (không phải `Authorization`). Đã fix bằng cách quét toàn bộ `HttpServletRequest` headers. Đặc biệt, **nút Test Webhook trên Web Sepay không gửi kèm API Key**, nên hệ thống báo 401 nếu bật tính năng bảo vệ.
  3. Lỗi không chốt được đơn: Khi dùng tính năng Test Webhook của Sepay, nội dung mặc định của họ sinh ra là `Thanh toan don hang DH...`. Hệ thống Backend dùng Regex `BILL(\d+)` nên không tìm thấy mã hóa đơn. Phải chỉnh bằng tay nội dung thành `BILL{id}` khi test mới thành công.

---

## 2. Tiến độ triển khai hiện tại (Pha 4: Huấn luyện AI & Đánh giá)

Trong pha này, hệ thống đang thực hiện quá trình huấn luyện và kiểm thử đa mô hình (Multi-model Training & Testing):

1. **Huấn luyện mô hình (Đang thực hiện)**:
   - Đã chọn 2 mô hình LLM tương tự hạng cân 7B để so sánh với `Qwen2.5-7B-Instruct`: `vilm/vinallama-7b-chat` và `SeaLLMs/SeaLLM-7B-v2.5` (nhằm tối ưu Tiếng Việt và tránh lỗi bản quyền Gated Repo của Llama-3).
   - Đã tạo các script huấn luyện độc lập (`modal_train_vinallama.py` và `modal_train_seallm.py`) sử dụng Modal.com với GPU H100.
   - Siêu tham số (Hyperparameters) và Random Seed (42) được giữ nguyên tuyệt đối giữa 3 mô hình để đảm bảo tính công bằng (Fairness).
2. **Xây dựng kịch bản Đánh giá Đa Chiều (Sắp thực hiện)**:
   - Thay vì chỉ dùng BLEU (so khớp từ vựng), hệ thống sẽ tích hợp **ROUGE**, **BERTScore (Semantic Similarity)** và **Tốc độ phản hồi (Tokens/s)**.
   - Tập Test gồm 100 câu độc lập được tách riêng từ một tập dữ liệu y tế khác để đảm bảo tính khách quan (tránh Data Leakage).
3. **Trang Dashboard Đánh giá trên Admin Web**:
   - Sẽ hiển thị bảng so sánh trực quan các chỉ số kỹ thuật và tốc độ của 3 mô hình.
4. **Bộ lọc Triệu chứng Khẩn cấp (Safety Guardrails)**:
   - Xây dựng bộ lọc từ khóa chặn trả lời và hiển thị cảnh báo đỏ khi phát hiện triệu chứng nguy kịch (đau ngực, đột quỵ...).

---

## 3. Những thông tin Bạn (Người dùng) cần cung cấp

Để phục vụ tốt nhất cho việc triển khai Pha 4, vui lòng chuẩn bị và cung cấp cho tôi các thông tin sau:

1. **Tài nguyên tính toán (GPU Server / Cloud API)**:
   - Bạn có máy chủ GPU cục bộ (ví dụ: RTX 3090/4090) hay tài khoản cloud nào để chạy fine-tuning không? (Ví dụ: Hugging Face, Modal.com, RunPod, Google Colab Pro).
   - Nếu bạn muốn chạy huấn luyện trực tiếp qua script Python của tôi trên server riêng, vui lòng cung cấp thông tin môi trường.
2. **Bộ câu hỏi/Dữ liệu mẫu (Dataset)**:
   - Bạn có tập dữ liệu câu hỏi - đáp y khoa tiếng Việt cụ thể nào của phòng khám muốn dùng để train và test không? (Nếu không, tôi sẽ lấy ngẫu nhiên 9,400 mẫu từ bộ dữ liệu y khoa tiếng Việt trên Hugging Face để train và trích ra 100 câu test).
3. **Danh sách triệu chứng khẩn cấp cần chặn**:
   - Hãy cho tôi biết các từ khóa hoặc triệu chứng cụ thể nào bạn muốn hệ thống phát hiện ngay để đưa ra cảnh báo khẩn cấp (ví dụ: "đau ngực", "nhồi máu", "tai biến", "khó thở", v.v.).
