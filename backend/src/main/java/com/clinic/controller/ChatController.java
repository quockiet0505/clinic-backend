package com.clinic.controller;

import com.clinic.dto.ai.ChatRequestDTO;
import com.clinic.service.ai.AiOrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final AiOrchestratorService aiOrchestratorService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody ChatRequestDTO request) {
        return aiOrchestratorService.handleChatStream(request);
    }

    @PostMapping(value = "/send", produces = MediaType.APPLICATION_JSON_VALUE)
    public java.util.Map<String, String> sendChat(@RequestBody ChatRequestDTO request) {
        return aiOrchestratorService.handleChatSend(request);
    }
}
