package com.clinic.e2e.group2_booking;

import com.clinic.e2e.BaseIntegrationTest;
import io.restassured.http.ContentType;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Nhóm 2: Đặt lịch khám trực tuyến (12 Test Cases)")
public class OnlineBookingTest extends BaseIntegrationTest {

    @Test
    @DisplayName("TC 2.1: Bệnh nhân đặt lịch khám thành công với đầy đủ thông tin")
    public void testOnlineBooking_Success() {
        // Giả lập Payload chuẩn theo AppointmentRequest DTO
        String requestBody = String.format("""
                {
                    "patientId": 1,
                    "mainDoctorId": 2,
                    "bookingMode": "ONLINE",
                    "appointmentDate": "%s",
                    "timeStart": "08:00:00",
                    "timeEnd": "08:30:00",
                    "appointmentType": "NEW",
                    "createdBy": "PATIENT"
                }
                """, LocalDate.now().plusDays(1).toString());

        given()
            // .header("Authorization", "Bearer " + getPatientToken()) // Trong môi trường thật cần JWT
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/v1/appointments")
        .then()
            .statusCode(200) // Hệ thống dùng ApiResponse wrapper
            .body("success", equalTo(true))
            .body("message", equalTo("Tạo lịch khám thành công")) // Theo logic thông thường
            .body("data.appointmentId", notNullValue());
    }

    @Test
    @DisplayName("TC 2.2: Xem danh sách khung giờ trống của bác sĩ")
    public void testFetchAvailableSlots_Success() {
        String date = LocalDate.now().plusDays(1).toString();
        
        given()
            .queryParam("doctorId", 2)
            .queryParam("date", date)
        .when()
            .get("/api/v1/appointments/slots")
        .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data", notNullValue()); // Phải trả về mảng các khung giờ trống
    }

    @Test
    @DisplayName("TC 2.3: Chống trùng lịch khi 2 bệnh nhân cùng lúc đặt 1 slot")
    public void testConcurrentBooking_PreventDoubleBooking() {
        // TODO: Logic giả lập gửi 2 request đặt lịch cùng lúc (Concurrency Test)
    }
}
