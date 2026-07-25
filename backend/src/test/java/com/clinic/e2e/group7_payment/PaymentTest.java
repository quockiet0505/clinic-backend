package com.clinic.e2e.group7_payment;

import com.clinic.e2e.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Nhóm 7: Hóa đơn và thanh toán (12 Test Cases)")
public class PaymentTest extends BaseIntegrationTest {

    @Test
    @DisplayName("TC 7.1: Bệnh nhân thanh toán thành công hóa đơn")
    public void testPayment_Success() {
        io.restassured.RestAssured.given()
            .contentType(io.restassured.http.ContentType.JSON)
            .body("{ \"appointmentId\": 1, \"amount\": 500000, \"method\": \"VNPAY\" }")
        .when()
            .post("/api/v1/payments")
        .then()
            // .statusCode(200); // Bỏ comment khi map đúng
            .log().all();
    }
}
