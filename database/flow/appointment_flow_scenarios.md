# Phân tích Luồng Đặt Lịch & Các Tình Huống Thực Tế (Edge Cases)

Tài liệu này được phân tích dựa trên file `appointment_flow.docx`, tập trung giải quyết các tình huống thực tế (edge cases) xảy ra tại phòng khám, đặc biệt là cách quản lý hàng đợi và thời gian của bác sĩ.

---

## 1. Vòng lặp Xét Nghiệm & Quản lý thời gian trống của Bác sĩ (Giải đáp thắc mắc)

**Tình huống:** Bệnh nhân Số 1 đến khám thông thường (đã đặt slot 30 phút). Bác sĩ khám lâm sàng mất 15 phút, sau đó có chỉ định đi xét nghiệm/chụp X-Quang. Bệnh nhân sang phòng Lab và phải chờ 30 phút mới có kết quả. 
**Câu hỏi:** Trong 30 phút chờ đợi đó, bác sĩ chính sẽ làm gì? Khi bệnh nhân có kết quả quay lại thì xử lý ra sao?

**Cách hệ thống & phòng khám xử lý (Luồng Re-exam):**

1. **Giải phóng phòng khám:** Ngay khi bác sĩ tạo chỉ định cận lâm sàng (`service_order`), trạng thái bệnh án của Bệnh nhân 1 chuyển sang `WAITING_RESULT`. Bệnh nhân 1 rời phòng khám để đi lấy mẫu. Bác sĩ **không bị khóa** với Bệnh nhân 1.
2. **Tận dụng thời gian trống:** Bác sĩ lập tức gọi **Bệnh nhân Số 2, Số 3** (những người đang có trạng thái `CHECKED_IN` trong hàng đợi) vào khám. Thời gian 30 phút chờ kết quả của Bệnh nhân 1 được lấp đầy hoàn toàn bằng các ca khám tiếp theo.
3. **Khi có kết quả (Chèn xen kẽ):** 30 phút sau, Bệnh nhân 1 cầm tờ kết quả quay lại gõ cửa phòng bác sĩ.
   - Bệnh nhân 1 **không phải ra quầy bốc số lại từ đầu**.
   - Hệ thống có luồng **Đọc kết quả (Re-exam)**. Tên của Bệnh nhân 1 sẽ được chèn **xen kẽ ưu tiên** vào danh sách chờ hiện tại trên màn hình của bác sĩ.
   - *Ví dụ:* Bác sĩ vừa khám xong Số 3, hệ thống báo đã có kết quả của Số 1 → Bác sĩ gọi Số 1 vào đọc kết quả, chẩn đoán cuối cùng và kê đơn (mất khoảng 5-10 phút). Xong xuôi Bệnh nhân 1 ra về (`COMPLETED`), bác sĩ mới gọi tiếp Số 4 vào khám.

*(Đã triển khai: Nút "Cận lâm sàng" và "Đã có kết quả" trên Admin-web, API `sendToLab` và `returnFromLab` ở Backend).*

---

## 2. Các Tình Huống Tại Quầy Lễ Tân (The Check-in Filter)

Trạng thái `CHECKED_IN` là tấm vé để tên bệnh nhân xuất hiện trên màn hình phòng khám của bác sĩ. Lễ tân đóng vai trò là màng lọc:

*   **Bệnh nhân Online đến ĐÚNG GIỜ:** Được giữ quyền ưu tiên, xếp lên đầu hàng đợi ở khung giờ đó.
*   **Bệnh nhân Online đến QUÁ SỚM (VD: Lịch 10:00 nhưng 08:30 đã tới):** Vẫn được `CHECKED_IN`, nhưng hệ thống ghim ở khu vực chờ, không cho "leo đầu" những người có lịch 08:30 hoặc 09:00. *(Ngoại lệ: Nếu phòng khám đang vắng, bác sĩ có thể gọi vào luôn).*
*   **Bệnh nhân Online đến TRỄ (Dưới 20 phút):** Vẫn được `CHECKED_IN`, nhưng **bị tước quyền ưu tiên giờ vàng**. Lễ tân sẽ đẩy họ xuống xếp hàng cùng nhóm Walk-in hiện tại.
*   **Bệnh nhân Online đến SIÊU TRỄ (> 20 phút) / BOM LỊCH:** Job chạy ngầm tự động quét lịch `PENDING` sang `CANCELLED`. Nếu bệnh nhân lết tới, lễ tân phải tạo một lịch `WALK_IN` mới toanh → xếp hàng lại từ số 0. Nếu không đến, bị đánh dấu 1 lần spam.
*   **Khách Cấp cứu / Người già (VIP):** Lễ tân tạo lịch Walk-in và tick vào ô Ưu tiên (Priority). Hệ thống ép STT = 0, lập tức pop-up lên đầu danh sách của bác sĩ. *(Đã triển khai: Checkbox `isPriority` khi tạo Walk-in và Check-in).*
*   **Đặt nhầm chuyên khoa / Nhầm bác sĩ:** Bệnh nhân đau dạ dày nhưng book nhầm Răng Hàm Mặt. Lễ tân **không bắt hủy lịch book lại**. Lễ tân dùng tính năng **Transfer (Chuyển bác sĩ)** trên hệ thống. Trạng thái giữ nguyên `CHECKED_IN`, nhưng dữ liệu được đẩy sang hàng đợi của bác sĩ Tiêu Hóa đang rảnh. *(Đã triển khai: Nút "Chuyển BS" và API `transferDoctor`).*

---

## 3. Các Tình Huống Kẹt Hàng Đợi Tại Phòng Khám (Queue Dynamics)

Bác sĩ không quan tâm ai book Online hay Walk-in, chỉ nhìn vào một danh sách duy nhất do hệ thống tự động sắp xếp:
*(Khách Ưu tiên > Khách Online đúng giờ > Khách Walk-in / Online đến trễ)*

*   **Hiệu ứng Domino (Bác sĩ khám lố giờ):** Ca trước tốn 45p thay vì 30p, đẩy lùi các ca sau. Hệ thống vẫn bình yên vì luồng chạy dựa trên "Hàng đợi thực tế đã Check-in" chứ không khóa chết vào `timeStart`. Bệnh nhân đã `CHECKED_IN` thì cứ chờ bác sĩ gọi.
*   **Gọi tên 3 lần không thấy mặt:** Bác sĩ gọi Số 5 nhưng Số 5 đi vệ sinh. Bác sĩ bấm **BỎ QUA (SKIPPED)**, hệ thống gọi Số 6 lên khám. Lát sau Số 5 chạy về, lễ tân đổi trạng thái từ `SKIPPED` về lại `CHECKED_IN`, hệ thống tự động nhét Số 5 vào vị trí ngay sau ca hiện tại để không làm kẹt hàng. *(Đã triển khai: Nút "Bỏ qua" cho Bác sĩ và "Trả về hàng đợi" cho Lễ tân).*
*   **Cần xét nghiệm nhưng không đủ điều kiện:** Bệnh nhân vào phòng, bác sĩ yêu cầu nội soi/thử máu nhưng bệnh nhân lỡ ăn sáng. Không thể làm xét nghiệm ngay. Bác sĩ lưu tạm bệnh án sơ bộ, dùng chức năng **Tạo Lịch Tái Khám (Follow-up)** hẹn 08:00 sáng mai quay lại nhịn ăn. Ca khám hiện tại kết thúc (`COMPLETED`), giải phóng slot.

---

## 4. Biến Cố Từ Phía Phòng Khám (Force Majeure)

*   **Bác sĩ báo ốm đột xuất:** Sáng 7h bác sĩ báo nghỉ, nhưng đã có 10 ca book Online.
*   Admin duyệt đơn nghỉ phép (`LeaveRequest`). Hệ thống quét thấy đụng độ lịch và cảnh báo Lễ tân. *(Đã triển khai: Hiển thị cờ `isDoctorBusy` và cảnh báo "⚠️ Bận" trên danh sách lịch hẹn).*
*   Lễ tân có 2 lựa chọn:
    1.  **Chuyển Bác sĩ (Ưu tiên):** Chuyển 10 ca này sang ca của Bác sĩ Y (có cùng chuyên khoa và đang rảnh). *(Hệ thống book online đã có dòng thông báo disclaimer về việc linh hoạt sắp xếp bác sĩ cùng chuyên khoa).*
    2.  **Hủy hàng loạt:** Hệ thống đổi 10 ca `PENDING` sang `CANCELLED`, gửi email/SMS xin lỗi khách hàng.

---

## 5. Các Ràng Buộc (Business Rules) Quan Trọng Khác

*   **Bạo lực thời gian:** Đòi đặt lịch khám vào lúc... 2 tiếng nữa → Chặn: "Chỉ được đặt trước tối thiểu 24 giờ".
*   **Khám 2 bác sĩ cùng lúc:** Vừa đặt Bác sĩ A lúc 09:00, lại đặt Bác sĩ B lúc 09:15 cùng ngày → Chặn ngay lập tức do trùng khung giờ của chính user đó.
*   **Spam bùng lịch:** Tháng trước đã bị đánh dấu `CANCELLED` (do bom lịch) 3 lần → Khóa tính năng book Online của user này, yêu cầu đến trực tiếp quầy. (Chỉ cho phép hủy lịch trước 3 tiếng).