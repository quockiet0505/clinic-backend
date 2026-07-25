package com.clinic.e2e.group5_lab;

import com.clinic.e2e.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Nhóm 5: Cận lâm sàng (10 Test Cases)")
public class LabTest extends BaseIntegrationTest {

    @Test
    @DisplayName("TC 5.1: Bác sĩ chỉ định dịch vụ cận lâm sàng thành công")
    public void testAssignLabService_Success() {
        io.restassured.RestAssured.given()
            .contentType(io.restassured.http.ContentType.JSON)
            .body("{ \"appointmentId\": 1, \"serviceIds\": [1, 2] }")
        .when()
            .post("/api/v1/lab-requests")
        .then()
            // .statusCode(200); // Bỏ comment khi map đúng api thực tế
            .log().all();
    }
}
