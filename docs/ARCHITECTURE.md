# Kiến trúc Hệ thống Backend (Spring Boot)

## 1. Cấu trúc Dự án
Dự án được xây dựng theo mô hình **Layered Architecture (N-Tier)**:
- `Controller`: Xử lý HTTP Request/Response, Validate DTO.
- `Service`: Chứa toàn bộ Business Logic (Nghiệp vụ).
- `Repository`: Kế thừa `JpaRepository` để giao tiếp với MySQL.
- `Entity`: Các class map trực tiếp với các bảng trong Database.
- `DTO`: Data Transfer Object dùng để nhận và trả dữ liệu.

## 2. Bảo mật (Security)
- Sử dụng **Spring Security** kết hợp **JWT (JSON Web Token)**.
- Mọi API (ngoại trừ Auth và Public API) đều phải đi qua `JwtAuthenticationFilter`.

## 3. Database
- Hệ quản trị: **MySQL 8.0+**
- Sử dụng **Spring Data JPA** (Hibernate) để ORM.
- Các File SQL thuần được đặt tại thư mục `database/` ngoài cùng.

## 4. Tích hợp AI (AI Chat)
- Các tool AI của Python Bot sẽ tương tác với Backend qua các REST API (ví dụ `/api/v1/appointments` để tự động đặt lịch).
- Có sự đồng bộ chặt chẽ về Entity `Appointment`, truyền cờ `isAiSuggested`.

*Các file tài liệu lẻ tẻ trước đây đã được xóa bỏ, vui lòng tham khảo file này cho cấu trúc tổng thể và file `business-flows.md` để xem nghiệp vụ chi tiết.*

## 5. Kế hoạch Phát triển Hạ tầng tương lai (Roadmap)
- **Tự động hóa hạ tầng (IaC):** Chuyển đổi việc cấu hình máy ảo (VM), mạng nội bộ (VPC), Luật tường lửa (Firewall), và Load Balancer thủ công trên GCP Console sang quản lý hoàn toàn bằng mã nguồn bằng cách sử dụng **SST (Infrastructure as Code - IaC)** (tham khảo cấu trúc tại dự án `cloud-engineer/infra`). Việc này giúp dễ dàng nhân bản và đồng bộ hạ tầng cho các môi trường khác nhau như Staging, UAT, và Production.
- **Tự động sao lưu Database (Auto Backup):** Tích hợp công cụ sao lưu dữ liệu MySQL định kỳ và đẩy lên Google Cloud Storage (GCS) tương tự như giải pháp sử dụng WAL-G cho Postgres để đảm bảo an toàn dữ liệu khi có sự cố hệ thống vật lý.
