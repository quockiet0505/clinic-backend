# 📦 Hướng dẫn Triển khai (Deployment)

Tài liệu này hướng dẫn cách đóng gói ứng dụng Backend và triển khai lên môi trường Production (VPS / AWS / Google Cloud).

## 1. Đóng gói ứng dụng (Build JAR)
Sử dụng Maven để build file chạy:
```bash
mvn clean package -DskipTests
```
File đầu ra sẽ nằm ở `target/clinic-backend-1.0.0.jar`.

## 2. Triển khai bằng Docker (Khuyến nghị)
Sử dụng Docker giúp đóng gói môi trường chuẩn xác nhất, tránh lỗi "works on my machine".

### Dockerfile
```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/clinic-backend-*.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
```

### Lệnh Deploy
```bash
docker build -t clinic-backend:latest .
docker run -d -p 8080:8080 --name clinic_api --env-file .env clinic-backend:latest
```

## 3. Cấu hình Nginx (Reverse Proxy)
Trên Server Production, nên dùng Nginx chặn trước Docker để cấu hình SSL/HTTPS:
```nginx
server {
    listen 80;
    server_name api.clinic.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```
