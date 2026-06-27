# Thư mục `database/flow` — Tài liệu luồng nghiệp vụ

| File | Nội dung |
|------|----------|
| [`appointment_flow_scenarios.md`](appointment_flow_scenarios.md) | **Phân tích tình huống thực tế (Edge cases)** từ `appointment_flow.docx` (Quản lý thời gian trống, Lễ tân, Hàng đợi, Biến cố) |
| [`consultation_clinical_workflow_analysis.md`](consultation_clinical_workflow_analysis.md) | **Luồng khám + chỉ định CLS trong ConsultationWorkspace**, máy trạng thái nút (Khám → Chờ KQ → Đọc KQ), xem kết quả không trùng Lab, tái khám & thông báo |
| [`demo_accounts.md`](demo_accounts.md) | **Tài khoản demo** `bn1@gmail.com` … `bn7@gmail.com` (mật khẩu `12345678`) + script [`seed_clinical_flow_demo.sql`](seed_clinical_flow_demo.sql) |
| [`clinical_flows_analysis.md`](clinical_flows_analysis.md) | **Phân tích sâu** 6 luồng từ `flow.docx`: timeline, edge case, slot 30p vs CLS *(chưa tạo file)* |
| [`appointment_booking_flows.md`](appointment_booking_flows.md) | Luồng đặt lịch bản cũ (3–4 mode) — tham khảo lịch sử |
| [`booking_two_flows.md`](booking_two_flows.md) | **Luồng đặt lịch hiện tại:** 2 mode (BS / XN–X-Quang), check slot, ưu tiên, patient-web + mobile + admin |
| [`database_analysis.md`](database_analysis.md) | Tổng quan schema DB theo nhóm |
| [`../flow.docx`](../flow.docx) | Tài liệu gốc Word |

**Schema:** [`../clinic_system.sql`](../clinic_system.sql)
