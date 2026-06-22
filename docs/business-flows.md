# Luồng nghiệp vụ phòng khám (Business Flows)

Tài liệu mô tả **thiết kế mục tiêu** và **thực trạng code/DB** (`clinic_system.sql`, `AppointmentService`, patient-web).  
Cập nhật mỗi khi thay đổi logic đặt lịch, schema, hoặc tích hợp AI.

**Liên quan:** [database.md](database.md) | [architecture-flow.md](architecture-flow.md) | AI knowledge: `clinic-ai-chat/knowledge/booking_guide.md`

---

## Changelog

| Ngày | Thay đổi |
|------|----------|
| 2026-06-22 | **Triển khai P0/P1 đặt lịch:** `booking_mode`, `expertise_id`, `suggested_expertise_id`, `is_ai_suggested`; UNIQUE slot; API slots theo khoa/dịch vụ; AI book + JWT |
| 2026-06-22 | Khởi tạo: phân tích Luồng 1–6, 4 mode đặt lịch, gap As-Is vs To-Be, đề xuất schema |

---

## 1. Tổng quan các luồng

| Luồng | Tên | Bảng chính | Trạng thái triển khai |
|-------|-----|------------|------------------------|
| **1** | Đặt lịch khám (Online / Walk-in) | `appointment`, `staff_schedule`, `leave_request` | **Một phần** — core có, thiếu vài rule |
| **2** | Tiếp nhận & khám bệnh | `medical_record`, `medical_record_vital` | **Một phần** — schema + entity có |
| **3** | Cận lâm sàng / Lab | `service_order`, `service_result` | **Một phần** — chỉ định trong ca khám, chưa đặt lịch Lab độc lập |
| **4** | Kho dược | `prescription`, `prescription_item`, `medicine` | **Một phần** — chưa có `inventory_transaction` |
| **5** | Thanh toán | — | **Chưa** — chưa có `bill`, `bill_item`, `refund_log` |
| **6** | CRM / AI Chat | `follow_up`, `notification`, `doctor_review`, `chat_session` | **Một phần** — chat DB có, AI service dùng RAM |

---

## 2. Luồng 1 — Đặt lịch khám bệnh

### 2.1. Bốn cách vào form đặt lịch (FINAL — thiết kế mục tiêu)

| # | Entry | Query / context | Lock gì | Ghi chú |
|---|--------|-----------------|---------|---------|
| **1** | Từ bác sĩ | `doctorId` | Lock `doctor` + `expertise`; service optional | patient-web: `?doctorId=` |
| **2** | Từ chuyên khoa | `expertiseId` | Lock `expertise`; chọn doctor optional | `?expertiseId=` |
| **3** | Từ dịch vụ | `serviceId` + `mode=service` | Lock `service`; doctor/expertise optional | Xét nghiệm, MRI, CT — không cần chọn khoa trước |
| **4** | Direct booking | Không doctor / expertise / service | Chỉ `date`, `time`, `description` | AI gợi ý chuyên khoa → staff/auto gán bác sĩ |

**Thực trạng frontend (`BookingForm.tsx`):**

- Mode 1–3: **đã có** qua URL params (`BookAppointment.tsx`).
- Mode 4 (DIRECT): backend hỗ trợ `BookingMode.DIRECT` + auto-assign bác sĩ; frontend **chưa** có entry riêng (AI có thể book qua tool).
- Query AI: `?suggestedExpertiseId=&isAiSuggested=1` — lưu gợi ý khoa tách khỏi khoa bệnh nhân chọn.
- Mô tả triệu chứng: gửi vào `note` — **frontend bắt buộc** (thiết kế ghi optional).

### 2.2. Luồng A — Khám bệnh có bác sĩ (thiết kế chi tiết)

| Bước | Bệnh nhân | Hệ thống | Ghi chú |
|------|-----------|----------|---------|
| 1 | Chọn loại: Khám bệnh | Hiển thị form | Web: doctor/expertise mode |
| 2 | Nhập mô tả tình trạng (optional) | Lưu `appointment.note` | AI RAG + `symptom_guide` gợi ý khoa |
| 3 | Chọn chuyên khoa (bắt buộc nếu không chọn BS) | Lọc bác sĩ theo `staff.expertise_id` | API: `GET /staffs/filter?expertiseId=` |
| 4 | Chọn bác sĩ (optional) | Có BS → slot theo BS; không → slot cả khoa / auto-assign | ✅ `GET /appointments/slots?expertiseId=` aggregate nhiều BS |
| 5 | Chọn ngày + giờ | Slot 30 phút từ `staff_schedule` trừ lịch đã đặt | `GET /appointments/slots?doctorId=&expertiseId=&serviceId=&date=` |
| 6 | Xác nhận | `POST /appointments` → `status=PENDING`, `type=ONLINE` | |

**Khám trọn gói (package):** nhiều khoa / nhiều slot trong một lần đặt → **chưa triển khai** (cần `appointment_service` hoặc nhiều `appointment` liên kết).

### 2.3. Luồng B — Đặt dịch vụ xét nghiệm / chụp chiếu

| Bước | Bệnh nhân | Hệ thống |
|------|-----------|----------|
| 1 | Chọn loại: Dịch vụ | Lọc `service.service_type IN (LAB_TEST, IMAGING)` |
| 2 | Chọn dịch vụ | Hiển thị giá, mô tả từ `service` |
| 3 | Chọn ngày + giờ | Slot phòng Lab/Imaging — **không cần bác sĩ** |
| 4 | Ghi chú (optional) | `appointment.note` |
| 5 | Xác nhận | `main_doctor_id=NULL`, `service_id` set, `type=ONLINE` hoặc `WALK_IN` |

**Thực trạng:**

- DB: `main_doctor_id` **nullable**, `service_id` **có** — hỗ trợ schema.
- `service_type`: `EXAM` | `LAB_TEST` | `IMAGING` — **đã có**.
- Frontend mode `service`: **đã có** — slot qua `serviceId`; LAB/IMAGING không bắt buộc `main_doctor_id`.
- `service_order` hiện gắn **medical_record** (chỉ định trong ca khám), **khác** appointment đặt Lab trực tiếp.

### 2.4. Quy tắc kinh doanh (Business Rules)

| Rule | Thiết kế | Thực trạng code |
|------|----------|-----------------|
| Slot 30 phút | Mặc định 30 phút/ca | ✅ `AppointmentService.getAvailableSlots()` |
| Online ≥ 24h trước giờ khám | Bắt buộc | ✅ `validateAppointmentLogic` khi `type=ONLINE` |
| Walk-in | Lễ tân tạo, check-in ngay | ⚠️ Field `queue_number`, `checkin_time` có; logic queue **chưa đầy đủ** trong `create()` |
| Staff schedule | BS phải có ca WORKING trong `staff_schedule` | ✅ Kiểm tra schedule theo ngày |
| Nghỉ phép | Không trùng `leave_request` APPROVED | ❌ **Chưa** validate trong `AppointmentService` |
| Chống trùng slot | UNIQUE `(main_doctor_id, appointment_date, time_start, is_deleted)` | ✅ DB + validate app; hủy → `is_deleted=1` |
| Hủy lịch | Trước 3 tiếng | ✅ `cancelByPatient()` |
| Hủy muộn / spam | Khóa đặt online sau N lần | ⚠️ Có đếm `[SPAM]` trong cancel_reason |

### 2.5. Chi tiết luồng con

#### 1.1 — Bệnh nhân đặt Online

```
Patient → POST /appointments (ONLINE, created_by=PATIENT)
       → validate 24h + schedule + conflict
       → status = PENDING
       → (target) notification Email/SMS
       → Lễ tân PATCH status → CONFIRMED
```

**Thực trạng:** Tạo PENDING ✅ | Notification ❌ | CONFIRMED manual qua admin ⚠️

#### 1.2 — Walk-in tại quầy

```
Staff → POST /appointments (WALK_IN, created_by=STAFF)
     → checkin_time = NOW(), status = CHECKED_IN
     → queue_number = thứ tự trong ngày theo bác sĩ
```

**Thực trạng:** API cho phép STAFF tạo ✅ | Tự set CHECKED_IN + queue **chưa** trong service

#### 1.3 — Online đến phòng khám

```
Staff tìm appointment → CONFIRMED/PENDING → CHECKED_IN + queue_number
```

**Thực trạng:** `updateStatus` ✅ | Queue auto ⚠️

---

## 3. Luồng 2 — Tiếp nhận & khám bệnh

| Bước | Vai trò | Bảng | Trạng thái |
|------|---------|------|------------|
| Đo sinh hiệu | Điều dưỡng | `medical_record` IN_PROGRESS + `medical_record_vital` | Schema ✅ |
| Bác sĩ khám | Bác sĩ | `diagnosis`, `treatment` trên `medical_record` | Schema ✅ |
| Chỉ định CLS | Bác sĩ | `service_order` ORDERED → record WAITING_RESULT | ✅ (LAB/IMAGING only) |
| Kê đơn | Bác sĩ | `prescription`, `prescription_item` | Schema ✅ |
| Kết thúc | Hệ thống | record DONE, appointment COMPLETED, checkout_time | Status enum ✅, auto bill ❌ |

**Liên kết appointment ↔ medical_record:** `medical_record.appointment_id` — có.

---

## 4. Luồng 3 — Cận lâm sàng (Lab)

Chu trình: `service_order` (ORDERED) → lấy mẫu → `service_result` → order DONE → notify bác sĩ.

Ngoại lệ: order REJECTED + `rejection_reason`.

**Lưu ý:** Đây là Lab **trong ca khám** (có `record_id`). Khác với **Luồng 1B** (bệnh nhân tự đặt lịch xét nghiệm).

---

## 5. Luồng 4 — Kho dược

Thiết kế: prescription PENDING → dược sĩ duyệt → trừ `medicine` → `inventory_transaction` DISPENSE.

**Thực trạng:** `prescription` / `prescription_item` / `medicine` có; **`inventory_transaction` chưa có** trong `clinic_system.sql`.

---

## 6. Luồng 5 — Thanh toán

Thiết kế: `bill_item` (CONSULTATION, SERVICE, MEDICINE) → `bill` UNPAID → PAID / REFUNDED.

**Thực trạng:** **Chưa có bảng bill** — chỉ có `consultation_fee`, `service_fee` trên `medical_record`.

---

## 7. Luồng 6 — CRM & AI Chatbot

| Tính năng | Thiết kế | Thực trạng |
|-----------|----------|------------|
| Tái khám | `follow_up` + notification | Bảng ✅, job nhắc ⚠️ |
| Đánh giá | `doctor_review` sau khám | ✅ (frontend mobile/web) |
| AI Chat audit | `chat_session`, `chat_message` | Bảng backend ✅; **clinic-ai-chat dùng session RAM**, chưa ghi DB |
| Gợi ý chuyên khoa từ mô tả | AI + `suggested_expertise_id` | ✅ RAG + `suggest_expertise_tool`; cột DB + API persist |

---

## 8. Schema — As-Is vs đề xuất bổ sung

### 8.1. Bảng `appointment` (hiện tại — `clinic_system.sql` bảng **12**)

```sql
-- Thứ tự FK: bảng 10 SERVICE → 11 DOCTOR_SERVICE_PRICE → 12 APPOINTMENT
appointment_id, patient_id,
main_doctor_id NULL, service_id NULL,
expertise_id NULL,           -- chuyên khoa bệnh nhân chọn
suggested_expertise_id NULL, -- chuyên khoa AI gợi ý từ note/chat
booking_mode ENUM('DOCTOR','EXPERTISE','SERVICE','DIRECT') NOT NULL DEFAULT 'DOCTOR',
is_ai_suggested BOOLEAN NOT NULL DEFAULT FALSE,
appointment_date, time_start, time_end,
appointment_type ENUM('ONLINE','WALK_IN'),
status ENUM(...),
created_by ENUM('PATIENT','STAFF'),
checkin_time, checkout_time, queue_number,
cancelled_by, cancel_reason, note,
is_deleted TINYINT DEFAULT 0
```

**Index:** `idx_unique_slot (main_doctor_id, appointment_date, time_start, is_deleted)` — hủy lịch set `is_deleted=1` để giải phóng slot.

**Chưa có:** `appointment_service` (gói khám nhiều dịch vụ) — roadmap P2.

### 8.2. Migration cho DB cũ

Nếu đã chạy `clinic_system.sql` phiên bản cũ (chưa có cột mới), chạy:

`database/migrations/add_appointment_booking_fields.sql`

**Lưu ý:** Script khởi tạo mới `database/clinic_system.sql` đã gộp đủ cột + UNIQUE — không cần migration.

### 8.3. `AppointmentRequest` / `AppointmentResponse` (DTO)

Đã có và persist: `mainDoctorId`, `expertiseId`, `suggestedExpertiseId`, `serviceId`, `bookingMode`, `isAiSuggested`, `note`.

---

## 9. Tích hợp AI Chat (`clinic-ai-chat`)

| Hành vi AI | Nguồn | Tool / RAG |
|------------|-------|------------|
| FAQ quy trình đặt lịch | `knowledge/booking_guide.md` | RAG |
| Gợi ý chuyên khoa từ triệu chứng | `knowledge/symptom_guide.md` | RAG (không chẩn đoán) |
| Danh sách bác sĩ / chuyên khoa | Backend live | `get_doctors_tool`, `get_specialties_tool` |
| Giờ trống | Backend live | `get_available_slots_tool` (doctor / expertise / service) |
| Tạo lịch thay bệnh nhân | JWT patient (`access_token` từ web/mobile) | ✅ `book_appointment_tool` → `POST /appointments` |
| Lưu gợi ý AI vào appointment | `suggested_expertise_id`, `is_ai_suggested` | ✅ persist qua API |

**Direct booking qua chat (mode 4):** Flow mục tiêu — bệnh nhân chỉ mô tả + chọn ngày giờ → AI gợi ý khoa → tạo appointment PENDING với `suggested_expertise_id` → lễ tân/ hệ thống gán bác sĩ.

---

## 10. Backlog ưu tiên (từ gap analysis)

1. **P0** — Validate `leave_request` APPROVED khi đặt lịch / sinh slot  
2. ~~**P0** — API slot theo `expertiseId` / Lab không cần `doctorId`~~ ✅  
3. ~~**P1** — Persist `expertise_id` + `suggested_expertise_id` trên `appointment`~~ ✅  
4. **P1** — Walk-in: auto `CHECKED_IN`, `queue_number` trong `AppointmentService`  
5. ~~**P1** — UNIQUE constraint chống trùng slot~~ ✅  
6. **P2** — `appointment_service` + đặt gói khám  
7. **P2** — Frontend entry DIRECT booking (không qua AI)  
8. **P3** — Luồng 5 billing tables + Luồng 4 inventory  
9. **P3** — AI chat ghi audit vào `chat_session` / `chat_message`  

---

## 11. API đặt lịch (tham chiếu nhanh)

| Method | Path | Role |
|--------|------|------|
| POST | `/api/v1/appointments` | PATIENT, STAFF, ADMIN |
| GET | `/api/v1/appointments/slots?doctorId=&expertiseId=&serviceId=&date=` | Public |
| PATCH | `/api/v1/appointments/{id}/cancel?reason=` | PATIENT |
| PATCH | `/api/v1/appointments/{id}/status?status=` | STAFF |
| GET | `/api/v1/appointments/my` | PATIENT |

Payload tạo lịch (patient-web):

```json
{
  "appointmentDate": "2026-06-25",
  "timeStart": "09:00:00",
  "timeEnd": "09:30:00",
  "mainDoctorId": 12,
  "expertiseId": 3,
  "suggestedExpertiseId": 5,
  "serviceId": null,
  "bookingMode": "EXPERTISE",
  "isAiSuggested": true,
  "appointmentType": "ONLINE",
  "createdBy": "PATIENT",
  "note": "Đau đầu 3 ngày, hoa mắt"
}
```

**`bookingMode`:** `DOCTOR` | `EXPERTISE` | `SERVICE` | `DIRECT`
