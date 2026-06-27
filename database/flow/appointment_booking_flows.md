# Luồng đặt lịch khám — Chi tiết đa nền tảng

Tài liệu mô tả **3 chế độ đặt lịch** (`booking_mode`), **field nào được `NULL`**, cách **backend xử lý** khi frontend gửi hoặc bỏ trống field, và **thực trạng triển khai** trên admin-web, patient-web, mobile-app.

**Liên quan:**

| Tài liệu | Nội dung |
|----------|----------|
| [`../clinic_system.sql`](../clinic_system.sql) | Schema bảng `appointment` |
| [`database_analysis.md`](database_analysis.md) | Tổng quan DB & luồng nghiệp vụ |
| [`clinical_flows_analysis.md`](clinical_flows_analysis.md) | 6 luồng nghiệp vụ + edge case (từ flow.docx) |
| [`../../docs/business-flows.md`](../../docs/business-flows.md) | Luồng nghiệp vụ toàn hệ thống |

**Cập nhật:** 2026-06-28 (Kiến trúc v2 - Tối ưu KTV & Hàng Đợi)

---

## 1. Bảng `appointment` — Cột nullable

```sql
-- Trích từ clinic_system.sql (bảng appointment)
main_doctor_id          INT NULL      -- NULL: chờ gán BS/KTV
service_id              INT NULL      -- NULL: khám bệnh thông thường (có chuyên khoa/Bác sĩ)
expertise_id            INT NULL      -- NULL: chỉ áp dụng khi đặt luồng SERVICE (xét nghiệm trực tiếp)
suggested_expertise_id  INT NULL      -- Chuyên khoa AI gợi ý (tham khảo)
booking_mode            ENUM('DOCTOR','EXPERTISE','SERVICE') DEFAULT 'DOCTOR'
is_ai_suggested         BOOLEAN DEFAULT FALSE
note                    TEXT NULL     -- Triệu chứng / lý do
time_start, time_end    TIME NULL     -- DTO bắt buộc khi tạo
```

**Index chống trùng slot:** `UNIQUE (main_doctor_id, appointment_date, time_start, is_deleted)`. 
*Lưu ý:* Khi đặt luồng `SERVICE`, hệ thống sẽ tự động gán một KTV (có role TECHNICIAN) vào cột `main_doctor_id` để áp dụng index chống trùng slot này.

---

## 2. API Backend

| Method | Endpoint | Vai trò |
|--------|----------|---------|
| `GET` | `/api/v1/appointments/slots?date=&doctorId=&expertiseId=&serviceId=` | Slot trống (chỉ **một** trong 3 param ID) |
| `POST` | `/api/v1/appointments` | Tạo lịch (`PATIENT` / `STAFF` / `ADMIN`) |
| `GET` | `/api/v1/appointments/my` | Lịch của bệnh nhân |
| `PATCH` | `/api/v1/appointments/{id}/cancel?reason=` | Hủy (trước 3h) |
| `PATCH` | `/api/v1/appointments/{id}/status` | Admin/staff đổi trạng thái |

**DTO request** (`AppointmentRequest.java`):

| Field | Bean Validation | Ghi chú |
|-------|-----------------|---------|
| `appointmentDate`, `timeStart`, `timeEnd` | `@NotNull` | Luôn bắt buộc |
| `appointmentType`, `createdBy` | `@NotNull` | `ONLINE` + `PATIENT` từ app bệnh nhân |
| `mainDoctorId`, `expertiseId`, `serviceId`, `bookingMode`, `note` | Không `@NotNull` | Tùy luồng sẽ bị ràng buộc trong Service |

**Suy luận mode** nếu client không gửi `bookingMode` (`AppointmentService.resolveBookingMode`):

```
có mainDoctorId  → DOCTOR
có serviceId     → SERVICE
có expertiseId   → EXPERTISE
thiếu tất cả     → Báo lỗi (Không cho phép DIRECT)
```

---

## 3. Backend — Xử lý theo `bookingMode`

Nguồn: `AppointmentService.create()` + `getAvailableSlots()`.

### 3.1. `DOCTOR` — Khám theo đích danh Bác sĩ

| Input | Bắt buộc? | Backend xử lý |
|-------|-----------|---------------|
| `mainDoctorId` | **Có** | `loadDoctor()` — thiếu → exception |
| `expertiseId` | Optional | Tự gán từ `staff.expertise_id` của BS |
| `serviceId` | **NULL** | Không set dịch vụ |

**Slot:** `GET /slots?doctorId=&date=`
**Validate:** Kiểm tra ca BS (weekend, lễ, nghỉ phép, trùng slot).

---

### 3.2. `EXPERTISE` — Khám theo chuyên khoa (Bệnh nhân không chọn BS)

| Input | Bắt buộc? | Backend xử lý |
|-------|-----------|---------------|
| `expertiseId` | **Có** | Thiếu → exception |
| `mainDoctorId` | **NULL** | `autoAssignDoctor(expertiseId)` — gán BS đang rảnh trong khung giờ đó |
| `serviceId` | **NULL** | |

**Slot:** `GET /slots?expertiseId=&date=` — gộp slot mọi BS thuộc khoa.

---

### 3.3. `SERVICE` — Đặt trực tiếp dịch vụ Cận Lâm Sàng

Chỉ áp dụng cho các service có `service_type` IN (`LAB_TEST`, `IMAGING`, `CT_SCAN`, `MRI`, `ENDOSCOPY`). (Luồng `EXAM` tạm ẩn).

| Input | Bắt buộc? | Backend xử lý |
|-------|-----------|---------------|
| `serviceId` | **Có** | Lấy ra `estimated_duration` của Service |
| `mainDoctorId` | **NULL** | `autoAssignTechnician()` — tìm KTV rảnh và gán vào |
| `expertiseId` | **NULL** | Không bắt buộc |

**Slot:** `GET /slots?serviceId=&date=`
Tính toán slot dựa trên thời gian thực hiện (`estimated_duration`) và tổng số lượng KTV (TECHNICIAN) đang trực. Đảm bảo KTV không bị dồn ứ bệnh nhân.

---

### 3.4. Chỉ định Cận Lâm Sàng (Trong quá trình Khám Bệnh)

Khác với đặt trực tiếp từ nhà (mục 3.3), khi bác sĩ đang khám (`IN_PROGRESS`) và kê chỉ định Cận Lâm Sàng:
- **KHÔNG xếp Slot thời gian.**
- Lệnh được đưa vào **Hàng đợi (FIFO Queue)** của phòng KTV. Bệnh nhân qua phòng KTV bốc số, chờ gọi tên thực hiện.
- Hỗ trợ loại dịch vụ `OTHER` (Bác sĩ phải điền tên dịch vụ tự do vào cột `custom_service_name`).

---

### 3.5. Validate chung (`validateAppointmentLogic`)

| Rule | Áp dụng |
|------|---------|
| Bệnh nhân không trùng giờ | Mọi mode |
| Không cho phép mode `DIRECT` | Bắt buộc chọn ít nhất Khoa, Bác sĩ, hoặc Dịch vụ |
| Spam hủy ≥ 3 lần `[SPAM]` | Khóa đặt online |
| Weekend / lễ / nghỉ / trùng slot | Áp dụng cho BS và cả KTV (Technician) |

---

### 3.6. Walk-in (admin tạo)

| Field | Giá trị |
|-------|---------|
| `appointmentType` | `WALK_IN` |
| `createdBy` | `STAFF` |
| Không kiểm tra 24h | |
| Sau save | `status = CHECKED_IN`, `checkin_time = now`, `queue_number` tăng dần |

---

## 4. Ma trận tổng hợp — Field NULL / bắt buộc

### 4.1. Theo loại khám (bệnh nhân online)

| Loại khám | `bookingMode` | Bắt buộc gửi | Được NULL | Slot query |
|-----------|---------------|---------------|-----------|------------|
| Chọn bác sĩ | `DOCTOR` | `mainDoctorId`, ngày, giờ | `serviceId` | `doctorId` |
| Chọn chuyên khoa | `EXPERTISE` | `expertiseId`, ngày, giờ | `mainDoctorId`, `serviceId` | `expertiseId` |
| Xét nghiệm / chụp | `SERVICE` + LAB/IMAGING | `serviceId`, ngày, giờ | **`mainDoctorId`, `expertiseId`** | `serviceId` |
| Gói khám EXAM | `SERVICE` + EXAM | `serviceId`, ngày, giờ | `mainDoctorId` (auto) | `serviceId` |
| AI / direct | `DIRECT` | ngày, giờ | BS, khoa, dịch vụ (tùy) | Không có¹ |

¹ Thực tế: chuyển sang EXPERTISE sau khi AI gợi ý khoa.

### 4.2. `suggestedExpertiseId` vs `expertiseId`

| Cột | Ai set | Mục đích |
|-----|--------|----------|
| `expertise_id` | Bệnh nhân / form / auto từ BS | Đặt lịch, slot, auto-assign |
| `suggested_expertise_id` | AI chat | Admin xem “AI gợi ý X, BN chọn Y” |

---

## 5. Admin-web

**Thư mục:** `clinic-frontend/admin-web/src/features/appointments/`

### 5.1. Tạo lịch Walk-in — `AppointmentFormDialog.tsx`

| Field UI | Required UI | Gửi API |
|----------|-------------|---------|
| `patientId` | Có | Number |
| `expertiseId` | Không (optional combobox) | null nếu trống |
| `serviceId` | Không | null nếu trống |
| `mainDoctorId` | Không | null nếu trống |
| `note` | Có | string |

**Validate form:** Phải có **ít nhất một** trong `mainDoctorId` \| `expertiseId` \| `serviceId`.

**Suy `bookingMode` trước POST:**

```
có mainDoctorId → DOCTOR
else có serviceId → SERVICE
else có expertiseId → EXPERTISE
else → DIRECT
```

**Payload cố định:** `appointmentType: WALK_IN`, `createdBy: STAFF`, ngày/giờ = hiện tại.

### 5.2. Hiển thị lịch — `AppointmentTable.tsx`

| Cột | Xử lý NULL |
|-----|------------|
| Loại đặt | `getBookingModeLabel(bookingMode)` |
| AI | Badge nếu `isAiSuggested` |
| Bác sĩ | `doctorName \|\| 'Chưa gán bác sĩ'` |
| Khoa | Chỉ hiện nếu `expertiseName` |
| AI gợi ý khoa | Hiện nếu `suggestedExpertiseName !== expertiseName` |
| Dịch vụ | Hiện nếu `serviceName` |

### 5.3. Thao tác staff

- **Check-in:** `PENDING` / `CONFIRMED` → `CHECKED_IN`
- **Hủy:** Mọi trạng thái trừ terminal
- **Transfer BS:** `PATCH /appointments/{id}/transfer?newDoctorId=` — gán BS khi lịch DIRECT/Lab chưa có BS

---

## 6. Patient-web

**Thư mục:** `clinic-frontend/patient-web/src/features/appointments/`

### 6.1. Entry — `BookAppointment.tsx` (URL params)

| URL | Mode | Lock UI |
|-----|------|---------|
| `?doctorId=` | `DOCTOR` | Bác sĩ + khoa (từ BS) |
| `?expertiseId=` | `EXPERTISE` | Chuyên khoa |
| `?serviceId=` hoặc `?mode=service` | `SERVICE` | Dịch vụ |
| `?suggestedExpertiseId=&isAiSuggested=1` | EXPERTISE + AI metadata | Gợi ý AI |

**Chưa có:** entry `?mode=direct` (form chỉ triệu chứng + ngày giờ).

### 6.2. Form — `BookingForm.tsx`

**Ẩn/hiện field theo `bookingMode`:**

| `bookingMode` | Hiện | Ẩn |
|---------------|------|-----|
| `DOCTOR` | Chuyên khoa (readonly/disabled), Bác sĩ, ngày/giờ | Dịch vụ |
| `EXPERTISE` | Chuyên khoa, Bác sĩ (optional: “Để hệ thống chọn”), ngày/giờ | Dịch vụ |
| `SERVICE` | Dịch vụ, ngày/giờ | Chuyên khoa, Bác sĩ |

**Slot** (`appointmentApi.getTimeSlots`):

```typescript
SERVICE  → { serviceId }
DOCTOR   → { doctorId }        // sau khi chọn BS
EXPERTISE → { expertiseId }    // hoặc doctorId nếu đã chọn BS
```

**Chọn slot EXPERTISE:** Nếu slot trả `doctorId` và form chưa có BS → gán `doctorId` từ slot.

**Validate UI (bắt buộc hơn backend):**

| Mode | UI bắt buộc |
|------|-------------|
| `DOCTOR` | `doctorId`, ngày, giờ, `description` (note) |
| `EXPERTISE` | `expertiseId`, ngày, giờ, note |
| `SERVICE` | `serviceId`, ngày, giờ, note |

**POST payload** (`appointmentApi.createAppointment`):

```json
{
  "appointmentDate", "timeStart", "timeEnd",
  "mainDoctorId": doctorId || null,
  "expertiseId": expertiseId || null,
  "serviceId": serviceId || null,
  "suggestedExpertiseId": suggestedExpertiseId || null,
  "bookingMode", "isAiSuggested",
  "appointmentType": "ONLINE",
  "createdBy": "PATIENT",
  "note": description
}
```

### 6.3. Lịch sử — `MyAppointments` / `AppointmentCard`

- Map `doctorName` → fallback `'Chưa xếp bác sĩ'` nếu null
- **Chưa map** `bookingMode`, `serviceType` — roadmap hiển thị “Xét nghiệm — không cần bác sĩ”

---

## 7. Mobile-app (Flutter)

**Thư mục:** `clinic-frontend/mobile-app/lib/`

### 7.1. State — `appointment_provider.dart`

| Method | `bookingMode` | Clear field |
|--------|---------------|-------------|
| `selectDoctor()` | `DOCTOR` | service, specialty |
| `selectService()` | `SERVICE` | doctor, specialty, expertiseId |
| `selectSpecialty()` | `EXPERTISE` | doctor, service |
| `applyAiSuggestion()` | `EXPERTISE` (nếu có khoa) | — |

**Gửi API** (`booking_service.confirmBooking`):

- `mainDoctorId`: từ `selectedDoctor` hoặc `selectedTimeSlot['doctorId']` (EXPERTISE)
- `expertiseId`: chỉ khi `bookingMode == EXPERTISE`
- `serviceId`: chỉ khi `bookingMode == SERVICE`
- `bookingMode`, `isAiSuggested`, `suggestedExpertiseId`

### 7.2. Entry màn hình

| Màn | Flow |
|-----|------|
| Home → BS / All Doctors / Doctor detail | `selectDoctor` → `SelectTimeScreen` |
| Home → Dịch vụ / All Services | `selectService` → `SelectTimeScreen` |
| Home → Chuyên khoa / All Specialties | `selectSpecialty` → `SelectTimeScreen` |
| Tab Lịch hẹn → “Đặt lịch” | Chỉ `SelectDoctorScreen` ⚠️ thiếu hub 3 loại |

### 7.3. `SelectTimeScreen` — Slot & NULL

**Info card:** Hiển thị BS **hoặc** dịch vụ **hoặc** chuyên khoa (đúng 1 trong 3).

**Slot hiện tại ⚠️:**

- Grid cố định 08:00–17:00, match với API
- Nếu `availableSlots.isEmpty` → mock mode → **mọi slot disabled**
- **Cần sửa:** render trực tiếp từ API; EXPERTISE hiện `doctorName` trên chip

**Note:** Bắt buộc nhập (UI) trước sang confirm.

### 7.4. `ConfirmBookingScreen`

| Mode | Hiển thị | Phí |
|------|----------|-----|
| DOCTOR | Card BS | `consultationFee` |
| SERVICE | Card dịch vụ | Giá dịch vụ |
| EXPERTISE | Card khoa | Hardcode 150.000 ⚠️ |

**Confirm:** Gửi đủ field; EXPERTISE lấy `doctorId` từ slot nếu chưa chọn BS.

### 7.5. Model & list — `appointment_model.dart`

**Có:** `mainDoctorId`, `serviceId`, `serviceName`, `doctorName`, `specialty`

**Thiếu:** `bookingMode`, `isAiSuggested`, `suggestedExpertiseId`, `serviceType`

**List card:** Luôn hiển thị avatar BS — với lịch Lab nên hiển thị tên dịch vụ, “Chưa gán bác sĩ”.

### 7.6. AI Chat — `chat_provider.dart`

- Chỉ chat text, **chưa** gọi `applyAiSuggestion()`
- **Chưa** gửi JWT cho AI book tool

---

## 8. AI Chat (`clinic-ai-chat`)

| Tool | Backend call |
|------|--------------|
| `get_available_slots_tool` | `GET /appointments/slots` (doctor / expertise / service) |
| `book_appointment_tool` | `POST /appointments` + JWT patient |

**Payload AI book:**

```json
{
  "bookingMode": "EXPERTISE | DOCTOR | SERVICE | DIRECT",
  "suggestedExpertiseId": ...,
  "isAiSuggested": true,
  "note": "triệu chứng từ chat"
}
```

**Patient-web deep link:** `/appointments/book?expertiseId=&suggestedExpertiseId=&isAiSuggested=1`

**Mobile:** Chưa có deep link tương đương.

---

## 9. Sơ đồ luồng end-to-end

```mermaid
flowchart TB
  subgraph clients [Clients]
    AW[admin-web Walk-in]
    PW[patient-web ONLINE]
    MA[mobile-app ONLINE]
    AI[clinic-ai-chat JWT]
  end

  subgraph api [Backend API]
    SLOTS[GET /appointments/slots]
    CREATE[POST /appointments]
    SVC[AppointmentService.create]
  end

  subgraph db [(appointment)]
  end

  AW -->|WALK_IN payload| CREATE
  PW --> SLOTS
  MA --> SLOTS
  AI --> CREATE
  PW --> CREATE
  MA --> CREATE
  SLOTS --> SVC
  CREATE --> SVC
  SVC -->|resolve mode + auto-assign + validate| db
```

---

## 10. Quy tắc UI nên tuân theo (checklist dev)

| # | Quy tắc |
|---|---------|
| 1 | `SERVICE` + LAB/IMAGING: **ẩn** chọn BS và chuyên khoa |
| 2 | `DOCTOR`: **ẩn** chọn dịch vụ; khoa readonly theo BS |
| 3 | `EXPERTISE`: BS optional; slot API trả `doctorId` → lưu vào payload |
| 4 | Không gửi `mainDoctorId: 0` — omit hoặc `null` |
| 5 | Luôn gửi `bookingMode` rõ ràng (tránh suy luận sai) |
| 6 | `note`: DB optional; product yêu cầu bắt buộc trên patient/mobile |
| 7 | Hiển thị list: `doctorName ?? 'Chưa gán bác sĩ'`; ưu tiên `serviceName` nếu `bookingMode=SERVICE` |

---

## 11. Gap & roadmap

| # | Gap | Nền tảng | Ưu tiên |
|---|-----|----------|---------|
| 1 | Slot grid cố định / empty → disabled | mobile | P0 |
| 2 | Hub đặt lịch 3 loại (tab Lịch hẹn) | mobile | P1 |
| 3 | Hiển thị `bookingMode` / Lab không BS | patient-web, mobile | P1 |
| 4 | DIRECT entry + slot strategy | patient-web, mobile, backend | P2 |
| 5 | AI chat → `applyAiSuggestion` + JWT | mobile | P2 |
| 6 | Phí EXPERTISE hardcode 150k | mobile confirm | P1 |
| 7 | `AppointmentModel` thiếu field mới | mobile | P0 |

---

## 12. Ví dụ payload thực tế

### 12.1. Xét nghiệm máu (SERVICE + LAB_TEST)

```json
POST /api/v1/appointments
{
  "serviceId": 12,
  "bookingMode": "SERVICE",
  "appointmentDate": "2026-06-26",
  "timeStart": "09:00:00",
  "timeEnd": "09:30:00",
  "appointmentType": "ONLINE",
  "createdBy": "PATIENT",
  "note": "Xét nghiệm tổng quát định kỳ"
}
```

**Kết quả DB:** `main_doctor_id = NULL`, `expertise_id = NULL`, `service_id = 12`.

### 12.2. Khám chuyên khoa — hệ thống chọn BS

```json
{
  "expertiseId": 3,
  "bookingMode": "EXPERTISE",
  "appointmentDate": "2026-06-26",
  "timeStart": "10:00:00",
  "timeEnd": "10:30:00",
  "appointmentType": "ONLINE",
  "createdBy": "PATIENT",
  "note": "Đau bụng âm ỉ 2 tuần"
}
```

**Backend:** `autoAssignDoctor(3)` → set `main_doctor_id`.

### 12.3. Khám bác sĩ cụ thể

```json
{
  "mainDoctorId": 5,
  "bookingMode": "DOCTOR",
  "appointmentDate": "2026-06-26",
  "timeStart": "14:00:00",
  "timeEnd": "14:30:00",
  "appointmentType": "ONLINE",
  "createdBy": "PATIENT",
  "note": "Tái khám huyết áp"
}
```

**Backend:** `expertise_id` tự lấy từ BS #5.

### 12.4. AI gợi ý khoa, BN chọn khoa khác

```json
{
  "expertiseId": 2,
  "suggestedExpertiseId": 7,
  "isAiSuggested": true,
  "bookingMode": "EXPERTISE",
  "appointmentDate": "2026-06-27",
  "timeStart": "08:30:00",
  "timeEnd": "09:00:00",
  "appointmentType": "ONLINE",
  "createdBy": "PATIENT",
  "note": "Đau ngực khi gắng sức — AI gợi ý Tim mạch"
}
```

**Admin thấy:** Khoa: Nội | AI gợi ý: Tim mạch.

---

*Tài liệu này là nguồn tham chiếu khi sửa patient-web, mobile-app hoặc `AppointmentService`. Cập nhật khi thay đổi schema, DTO, hoặc form đặt lịch.*
