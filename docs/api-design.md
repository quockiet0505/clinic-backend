# 🔌 Tiêu chuẩn Thiết kế API (API Design Guidelines)

Hệ thống Backend cung cấp các RESTful API. Mọi API mới viết thêm phải tuân thủ chuẩn này để Mobile và Frontend dễ tích hợp.

## 1. Định dạng Base URL & Versioning
- Format: `http://{domain}/api/v1/{resource}`
- Tất cả API trả về JSON. Header bắt buộc có `Content-Type: application/json`.

## 2. HTTP Methods & Status Codes
Sử dụng chính xác phương thức HTTP để mô tả hành động:
- `GET`: Đọc tài nguyên (200 OK).
- `POST`: Tạo mới (201 Created).
- `PUT`: Cập nhật toàn bộ tài nguyên (200 OK).
- `PATCH`: Cập nhật một phần tài nguyên (200 OK).
- `DELETE`: Xóa tài nguyên (204 No Content).

## 3. Chuẩn hóa Định dạng Response (API Response Wrapper)
Mọi API (kể cả lỗi) đều phải được bọc trong một đối tượng chung:

**Thành công:**
```json
{
  "code": 200,
  "status": "SUCCESS",
  "message": "Lấy danh sách thành công",
  "data": [ ... ]
}
```

**Thất bại:**
```json
{
  "code": 400,
  "status": "BAD_REQUEST",
  "message": "Số điện thoại không hợp lệ",
  "errors": [
    {"field": "phone", "message": "Phone must be 10 digits"}
  ]
}
```

## 4. Phân trang (Pagination)
Sử dụng `page` (từ 0) và `size` (mặc định 20).
Response phân trang phải chứa metadata:
```json
"data": {
  "content": [ ... ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 150,
  "totalPages": 8
}
```

## 5. API Đặt lịch (Appointment)

| Method | Path | Auth | Mô tả |
|--------|------|------|--------|
| POST | `/api/v1/appointments` | PATIENT / STAFF | Tạo lịch; body có `bookingMode`, `isAiSuggested`, `suggestedExpertiseId` |
| GET | `/api/v1/appointments/slots` | Public | `?date=` + một trong `doctorId`, `expertiseId`, `serviceId` |
| GET | `/api/v1/appointments/my` | PATIENT | Lịch của tôi |
| PATCH | `/api/v1/appointments/{id}/cancel` | PATIENT | Hủy (≥3h trước); set `is_deleted=1` |
| PATCH | `/api/v1/appointments/{id}/status` | STAFF | Cập nhật trạng thái |

**Payload tạo lịch (rút gọn):**

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
  "note": "Triệu chứng..."
}
```

Chi tiết luồng: [business-flows.md](business-flows.md)
