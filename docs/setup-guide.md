# Hướng dẫn cài đặt Backend

## Yêu cầu môi trường
- Java 17+
- Maven 3.8+
- MySQL 8.0+

## Các bước chạy dự án

1. **Cấu hình Database**:
   - Mở file `backend/src/main/resources/application.properties`.
   - Cập nhật `spring.datasource.username` và `spring.datasource.password` cho phù hợp với MySQL local của bạn.
   - Đảm bảo database `clinic_system` đã được tạo (xem file `database.md`).

2. **Build và Chạy**:
   - Mở terminal tại thư mục `backend`.
   - Chạy lệnh để tải thư viện và build:
     ```bash
     mvn clean install
     ```
   - Chạy lệnh để khởi động server:
     ```bash
     mvn spring-boot:run
     ```

3. **Kiểm tra**:
   - Server mặc định chạy tại: `http://localhost:8080`
   - Các API bắt đầu với tiền tố `/api/v1/`
