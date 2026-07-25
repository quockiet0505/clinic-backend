package com.clinic.e2e;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * Lớp cơ sở (Base Class) cho tất cả các E2E Test của Backend.
 * - Khởi tạo Spring Boot context với cổng ngẫu nhiên.
 * - Cấu hình RestAssured trỏ tới cổng đó.
 * - Kế thừa lớp này giúp các class con không phải lặp lại code cấu hình.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // Sử dụng file cấu hình application-test.properties (cần tạo nếu sử dụng db riêng)
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    @BeforeEach
    public void setUpBase() {
        RestAssured.port = port;
        // Có thể cấu hình thêm các setup mặc định tại đây (VD: lấy token admin chuẩn bị sẵn)
    }
}
