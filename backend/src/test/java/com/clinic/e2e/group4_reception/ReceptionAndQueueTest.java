package com.clinic.e2e.group4_reception;

import com.clinic.e2e.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Nhóm 4: Tiếp nhận, khám bệnh và quản lý hàng đợi (12 Test Cases)")
public class ReceptionAndQueueTest extends BaseIntegrationTest {

    @Test
    @DisplayName("TC 4.1: Lễ tân gọi bệnh nhân vào phòng khám (Check-in)")
    public void testReceptionCallPatient_Success() {
        int appointmentId = 1;
        
        io.restassured.RestAssured.given()
            .contentType(io.restassured.http.ContentType.JSON)
        .when()
            .patch("/api/v1/appointments/" + appointmentId + "/queue/call")
        .then()
            .statusCode(200)
            .body("success", org.hamcrest.Matchers.equalTo(true));
    }
}
