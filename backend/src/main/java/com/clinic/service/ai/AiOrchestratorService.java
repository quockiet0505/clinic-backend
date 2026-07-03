package com.clinic.service.ai;

import com.clinic.dto.ai.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiOrchestratorService {

    @Value("${application.ai-server-url}")
    private String aiServerUrl;

    private final com.clinic.repository.staff.StaffRepository staffRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder().build();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final java.util.Map<String, java.util.List<MessageHistoryDTO>> sessionHistories = new java.util.concurrent.ConcurrentHashMap<>();

    public SseEmitter handleChatStream(ChatRequestDTO request) {
        SseEmitter emitter = new SseEmitter(600000L); // 10 minutes timeout

        executorService.execute(() -> {
            try {
                String sessionId = request.getSessionId() != null ? request.getSessionId() : "default";
                java.util.List<MessageHistoryDTO> history = sessionHistories.computeIfAbsent(sessionId, k -> new java.util.ArrayList<>());

                // 1. Analyze Query
                AnalyzeRequestDTO analyzeRequest = new AnalyzeRequestDTO();
                analyzeRequest.setMessage(request.getMessage());
                analyzeRequest.setHistory(new java.util.ArrayList<>(history));

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<AnalyzeRequestDTO> entity = new HttpEntity<>(analyzeRequest, headers);

                log.info("Sending /analyze to AI Server");
                ResponseEntity<AnalyzeResponseDTO> analyzeResponse = restTemplate.postForEntity(
                        aiServerUrl + "/api/v1/analyze",
                        entity,
                        AnalyzeResponseDTO.class
                );

                AnalyzeResponseDTO analysis = analyzeResponse.getBody();
                if (analysis == null) {
                    throw new RuntimeException("AI Analyze Response is null");
                }

                log.info("Received Intent: {}, Params: {}", analysis.getIntent(), analysis.getParameters());

                // 2. Fetch Knowledge Context based on Intent
                String knowledgeContext = fetchKnowledgeContext(analysis);

                // 3. Generate Stream Response
                GenerateRequestDTO generateRequest = new GenerateRequestDTO();
                generateRequest.setMessage(request.getMessage()); // Or rewrittenQuery
                generateRequest.setHistory(new java.util.ArrayList<>(history));
                generateRequest.setIntent(analysis.getIntent());
                generateRequest.setRewrittenQuery(analysis.getRewrittenQuery());
                generateRequest.setKnowledgeContext(knowledgeContext);

                // Add user message to history
                history.add(new MessageHistoryDTO("user", request.getMessage()));

                String requestBody = objectMapper.writeValueAsString(generateRequest);

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(aiServerUrl + "/api/v1/generate"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                log.info("Starting /generate stream");
                httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                        .thenAccept(response -> {
                            Stream<String> lines = response.body();
                            StringBuilder fullResponse = new StringBuilder();
                            lines.forEach(line -> {
                                try {
                                    if (line.startsWith("data: ")) {
                                        String chunk = line.substring(6);
                                        if ("[ERROR]".equals(chunk.trim()) || chunk.contains("[ERROR]")) {
                                            emitter.send(SseEmitter.event().data(chunk));
                                        } else {
                                            fullResponse.append(chunk);
                                            emitter.send(SseEmitter.event().data(chunk));
                                        }
                                    }
                                } catch (Exception e) {
                                    log.error("Error sending SSE chunk", e);
                                    emitter.completeWithError(e);
                                }
                            });
                            // Add AI response to history
                            history.add(new MessageHistoryDTO("assistant", fullResponse.toString()));
                            emitter.complete();
                        })
                        .exceptionally(ex -> {
                            log.error("Error in HTTP Client stream", ex);
                            emitter.completeWithError(ex);
                            return null;
                        });

            } catch (Exception e) {
                log.error("Error in AI Orchestration", e);
                try {
                    emitter.send(SseEmitter.event().data("[ERROR] Lỗi hệ thống: " + e.getMessage()));
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            }
        });

        return emitter;
    }

    public java.util.Map<String, String> handleChatSend(ChatRequestDTO request) {
        String sessionId = request.getSessionId() != null ? request.getSessionId() : "default";
        java.util.List<MessageHistoryDTO> history = sessionHistories.computeIfAbsent(sessionId, k -> new java.util.ArrayList<>());

        try {
            // 1. Analyze Query
            AnalyzeRequestDTO analyzeRequest = new AnalyzeRequestDTO();
            analyzeRequest.setMessage(request.getMessage());
            analyzeRequest.setHistory(new java.util.ArrayList<>(history));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<AnalyzeRequestDTO> entity = new HttpEntity<>(analyzeRequest, headers);

            ResponseEntity<AnalyzeResponseDTO> analyzeResponse = restTemplate.postForEntity(
                    aiServerUrl + "/api/v1/analyze",
                    entity,
                    AnalyzeResponseDTO.class
            );

            AnalyzeResponseDTO analysis = analyzeResponse.getBody();
            if (analysis == null) throw new RuntimeException("AI Analyze Response is null");

            // 2. Fetch Knowledge Context based on Intent
            String knowledgeContext = fetchKnowledgeContext(analysis);

            // 3. Generate Request
            GenerateRequestDTO generateRequest = new GenerateRequestDTO();
            generateRequest.setMessage(request.getMessage());
            generateRequest.setHistory(new java.util.ArrayList<>(history));
            generateRequest.setIntent(analysis.getIntent());
            generateRequest.setRewrittenQuery(analysis.getRewrittenQuery());
            generateRequest.setKnowledgeContext(knowledgeContext);

            history.add(new MessageHistoryDTO("user", request.getMessage()));

            String requestBody = objectMapper.writeValueAsString(generateRequest);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(aiServerUrl + "/api/v1/generate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Send sync
            HttpResponse<Stream<String>> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofLines());
            StringBuilder fullResponse = new StringBuilder();
            response.body().forEach(line -> {
                if (line.startsWith("data: ")) {
                    String chunk = line.substring(6);
                    if (!"[ERROR]".equals(chunk.trim()) && !chunk.contains("[ERROR]")) {
                        fullResponse.append(chunk);
                    }
                }
            });

            String finalReply = fullResponse.toString().replace("\\n", "\n");
            history.add(new MessageHistoryDTO("assistant", finalReply));

            return java.util.Map.of("reply", finalReply, "session_id", sessionId);

        } catch (Exception e) {
            log.error("Error in AI Orchestration (Send)", e);
            return java.util.Map.of("reply", "Lỗi hệ thống: " + e.getMessage(), "session_id", sessionId);
        }
    }

    private String fetchKnowledgeContext(AnalyzeResponseDTO analysis) {
        String intent = analysis.getIntent();
        
        if ("BOOKING".equals(intent)) {
            return "HỆ THỐNG BÁO: Hiện tại tính năng lấy lịch trực tiếp từ DB đang nâng cấp. CHỈ THỊ CHO AI: Vui lòng yêu cầu người dùng truy cập mục Đặt lịch trên web/app để xem giờ trống chính xác.";
        } else if ("DOCTOR_INFO".equals(intent)) {
            // Lấy danh sách bác sĩ thật từ Database
            java.util.List<com.clinic.entity.staff.Staff> doctors = staffRepository.findByStaffTypeAndIsDeleted(com.clinic.common.enums.StaffType.DOCTOR, 0);
            StringBuilder sb = new StringBuilder("DANH SÁCH BÁC SĨ TẠI PHÒNG KHÁM:\n");
            for (com.clinic.entity.staff.Staff doc : doctors) {
                sb.append("- ").append(doc.getFullName());
                if (doc.getExpertise() != null) {
                    sb.append(" (Chuyên khoa: ").append(doc.getExpertise().getExpertiseName()).append(")");
                }
                if (doc.getExperience() != null) {
                    sb.append(" - Kinh nghiệm: ").append(doc.getExperience());
                }
                sb.append("\n");
            }
            return sb.toString();
        } else if ("CLINIC_INFO".equals(intent)) {
            return "Phòng khám ClinicPro. Địa chỉ: 123 Nguyễn Văn Linh, Đà Nẵng. Điện thoại: 0123456789. Giờ làm việc: 8:00 đến 17:00, T2-T7.";
        }
        return "";
    }
}
