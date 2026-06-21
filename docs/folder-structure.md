# 📂 Cấu trúc Thư mục (Folder Structure)

Dự án áp dụng mô hình Layered Architecture kết hợp với Feature-Based Packaging để rạch ròi trách nhiệm của từng thành phần.

```text
src/main/java/com/clinic/
├── ClinicApplication.java      # Lớp chạy ứng dụng (Entry point)
│
├── config/                     # Chứa các file cấu hình hệ thống
│   ├── SecurityConfig.java     # Cấu hình Spring Security
│   ├── SwaggerConfig.java      # Cấu hình OpenAPI documentation
│   └── WebMvcConfig.java       # CORS & Interceptors
│
├── core/                       # Lớp lõi dùng chung (Cross-cutting concerns)
│   ├── exceptions/             # GlobalExceptionHandler, CustomException
│   ├── responses/              # ApiResponse, ErrorResponse (chuẩn hóa đầu ra)
│   └── utils/                  # Hàm tiện ích (DateUtils, StringUtils)
│
├── features/                   # Chứa các Module nghiệp vụ chính
│   ├── auth/                   # Xử lý Đăng nhập, Đăng ký, Quên mật khẩu
│   ├── users/                  # Quản lý tài khoản (Admin, Bác sĩ, Bệnh nhân)
│   ├── appointments/           # Logic Đặt lịch, Hủy lịch
│   └── medical_records/        # Quản lý hồ sơ bệnh án
│
└── infrastructure/             # Cầu nối với các hệ thống bên ngoài
    ├── email/                  # Tích hợp SendGrid / SMTP
    ├── payment/                # Tích hợp VNPay, Momo
    └── storage/                # Tích hợp AWS S3, Cloudinary
```

## Chi tiết trong 1 Feature (Ví dụ: `appointments`)
Bên trong thư mục `appointments/`, cấu trúc sẽ chia theo Layer:
```text
appointments/
├── dto/                    # Request/Response DTO (BookingRequest, AppointmentResponse)
├── entity/                 # JPA Entity (Appointment.java)
├── repository/             # JpaRepository interface
├── service/                # Business logic (AppointmentService interface & Impl)
└── controller/             # REST API Controller
```
