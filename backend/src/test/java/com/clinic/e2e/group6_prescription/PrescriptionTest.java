package com.clinic.e2e.group6_prescription;

import com.clinic.e2e.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Nhóm 6: Kê đơn thuốc (12 Test Cases)")
public class PrescriptionTest extends BaseIntegrationTest {

    @Test
    @DisplayName("TC 6.1: Bác sĩ kê đơn thuốc thành công")
    public void testCreatePrescription_Success() {
        io.restassured.RestAssured.given()
            .contentType(io.restassured.http.ContentType.JSON)
            .body("{ \"appointmentId\": 1, \"medicines\": [{\"id\": 1, \"quantity\": 10}] }")
        .when()
            .post("/api/v1/prescriptions")
        .then()
            .log().all();
    }
}
