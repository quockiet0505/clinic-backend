package com.clinic.e2e.group3_reschedule;

import com.clinic.e2e.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Nhóm 3: Dời và hủy lịch khám (12 Test Cases)")
public class RescheduleAndCancelTest extends BaseIntegrationTest {

    @Test
    @DisplayName("TC 3.1: Bệnh nhân hủy lịch khám thành công")
    public void testCancelBooking_Success() {
        // Giả lập ID lịch khám
        int appointmentId = 1;
        
        io.restassured.RestAssured.given()
            .contentType(io.restassured.http.ContentType.JSON)
            .body("{ \"reason\": \"Bận việc đột xuất\" }")
        .when()
            .patch("/api/v1/appointments/" + appointmentId + "/cancel")
        .then()
            .statusCode(200)
            .body("success", org.hamcrest.Matchers.equalTo(true));
    }
}
