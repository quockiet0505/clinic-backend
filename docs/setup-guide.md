# 🚀 Hướng dẫn Cài đặt & Khởi chạy (Setup Guide)

Tài liệu này hướng dẫn chi tiết các bước thiết lập môi trường Backend trên máy tính cá nhân.

## 1. Yêu cầu hệ thống (Prerequisites)
Để chạy dự án, máy tính của bạn phải cài đặt:
- **Java Development Kit (JDK) 17:** Khuyến nghị dùng Eclipse Temurin hoặc Amazon Corretto. Cài biến môi trường `JAVA_HOME`.
- **Maven 3.8.x trở lên:** Cài đặt và cấu hình `M2_HOME`.
- **Database:** MySQL 8.0 (chạy qua Docker hoặc cài trực tiếp).
- **IDE Khuyến nghị:** IntelliJ IDEA Ultimate / Community Edition.

## 2. Thiết lập Database
Tạo database mới trong MySQL:
```sql
CREATE DATABASE clinic_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 3. Cấu hình Môi trường (application.yml)
Tạo file `src/main/resources/application-local.yml` (hoặc sao chép từ `application.yml`):
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/clinic_db?useSSL=false&serverTimezone=UTC
    username: root
    password: password
  jpa:
    hibernate:
      ddl-auto: update # Chỉ dùng cho DEV
    show-sql: true
```

## 4. Khởi chạy
Mở Terminal tại thư mục `clinic-backend` và chạy:
```bash
# 1. Clean và tải thư viện
mvn clean install -DskipTests

# 2. Chạy ứng dụng
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## 5. Troubleshooting (Lỗi thường gặp)
- **Port 8080 already in use:** Một ứng dụng khác đang chiếm cổng 8080. Kill process đó hoặc đổi cổng trong `server.port`.
- **Access denied for user:** Kiểm tra lại username/password MySQL.
