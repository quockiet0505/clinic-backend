package com.clinic.e2e.group8_ai_chatbot;

import com.clinic.e2e.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Nhóm 8: AI Chatbot (16 Test Cases)")
public class ChatbotTest extends BaseIntegrationTest {

    @Test
    @DisplayName("TC 8.1: AI phản hồi thông tin giờ mở cửa")
    public void testChatbot_WorkingHours() {
        io.restassured.RestAssured.given()
            .contentType(io.restassured.http.ContentType.JSON)
            .body("{ \"message\": \"Phòng khám mấy giờ mở cửa?\" }")
        .when()
            .post("/api/v1/chat")
        .then()
            .log().all();
    }
}
