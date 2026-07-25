package com.clinic.e2e.group9_ai_moderation;

import com.clinic.e2e.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Nhóm 9: Đánh giá và AI kiểm duyệt bình luận (18 Test Cases)")
public class AiModerationIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("TC 9.1: Đánh giá có từ khóa độc hại bị AI từ chối")
    public void testModeration_RejectToxicComment() {
        io.restassured.RestAssured.given()
            .contentType(io.restassured.http.ContentType.JSON)
            .body("{ \"appointmentId\": 1, \"rating\": 1, \"comment\": \"Dịch vụ quá tệ, đồ ngu\" }")
        .when()
            .post("/api/v1/feedback")
        .then()
            .statusCode(200)
            .body("success", org.hamcrest.Matchers.equalTo(false))
            .body("message", org.hamcrest.Matchers.containsString("vi phạm tiêu chuẩn cộng đồng"));
    }

    @Test
    @DisplayName("TC 9.2: Cơ chế dự phòng khi AI Server gặp lỗi")
    public void testAiFallbackMechanism_WhenServerDown() {
        // TODO: Test xem backend có lưu bình luận thành PENDING khi gọi sang AI Python bị lỗi không
    }
}
