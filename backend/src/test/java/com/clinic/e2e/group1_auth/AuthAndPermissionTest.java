package com.clinic.e2e.group1_auth;

import com.clinic.e2e.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.restassured.http.ContentType;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.containsString;

@DisplayName("Nhóm 1: Đăng ký, đăng nhập và phân quyền (14 Test Cases)")
public class AuthAndPermissionTest extends BaseIntegrationTest {

    @Test
    @DisplayName("TC 1.1: Đăng ký bệnh nhân thành công với thông tin đầy đủ")
    public void testPatientRegistration_Success() {
        String requestBody = """
                {
                    "fullName": "Nguyen Van A",
                    "email": "nguyenvana_test1@gmail.com",
                    "password": "password123",
                    "phone": "0901234567",
                    "gender": "MALE",
                    "dateOfBirth": "1990-01-01",
                    "address": "123 Test Street, HCM"
                }
                """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/v1/auth/patient/register")
        .then()
            .statusCode(200) // Hệ thống dùng ApiResponse nên code chuẩn là 200 (kèm success=true)
            .body("success", equalTo(true))
            .body("message", equalTo("Patient registered successfully"))
            .body("data.email", equalTo("nguyenvana_test1@gmail.com"));
    }

    @Test
    @DisplayName("TC 1.2: Đăng ký thất bại khi thiếu họ và tên")
    public void testPatientRegistration_Fail_MissingName() {
        String requestBody = """
                {
                    "email": "nguyenvana_test2@gmail.com",
                    "password": "password123",
                    "phone": "0901234567",
                    "gender": "MALE",
                    "dateOfBirth": "1990-01-01",
                    "address": "123 Test Street, HCM"
                }
                """; // Thiếu fullName

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/v1/auth/patient/register")
        .then()
            .statusCode(400) // Lỗi Validation
            .body("success", equalTo(false))
            .body("message", containsString("Validation"));
    }

    @Test
    @DisplayName("TC 1.3: Đăng nhập thất bại khi tài khoản không tồn tại")
    public void testPatientLogin_Fail_AccountNotFound() {
        String requestBody = """
                {
                    "email": "not_exist_user@gmail.com",
                    "password": "password123"
                }
                """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/v1/auth/patient/login")
        .then()
            // Tùy theo cách ném lỗi của hệ thống, có thể là 401 hoặc 404
            .statusCode(org.hamcrest.Matchers.anyOf(equalTo(401), equalTo(404), equalTo(400))) 
            .body("success", equalTo(false));
    }
}
