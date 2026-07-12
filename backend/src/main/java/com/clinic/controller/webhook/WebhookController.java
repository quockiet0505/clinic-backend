package com.clinic.controller.webhook;

import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.finance.SepayWebhookPayload;
import com.clinic.service.medical.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.clinic.util.ResponseUtil;

import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final InvoiceService invoiceService;
    private final ObjectMapper objectMapper;
    
    @Value("${sepay.webhook-secret}")
    private String sepayWebhookSecret;

    @PostMapping("/sepay")
    public ResponseEntity<ApiResponse<Object>> handleSepayWebhook(
            @RequestHeader(value = "X-SePay-Signature", defaultValue = "") String signature,
            @RequestHeader(value = "X-SePay-Timestamp", defaultValue = "") String timestamp,
            @RequestBody String rawBody
    ) {
        log.info("--- INCOMING SEPAY WEBHOOK ---");
        
        try {
            // Calculate HMAC-SHA256
            String dataToSign = timestamp + "." + rawBody;
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(sepayWebhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            String hash = HexFormat.of().formatHex(sha256Hmac.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8)));
            String expectedSignature = "sha256=" + hash;

            // Verify signature
            if (!expectedSignature.equals(signature)) {
                log.warn("Invalid HMAC signature! Expected: {}, Got: {}", expectedSignature, signature);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        ApiResponse.<Object>builder()
                                .success(false)
                                .message("Invalid signature")
                                .build()
                );
            }

            // Parse payload
            SepayWebhookPayload payload = objectMapper.readValue(rawBody, SepayWebhookPayload.class);
            log.info("Payload: {}", payload);
            
            // Process
            invoiceService.processSepayWebhook(payload);
            
        } catch (Exception e) {
            log.error("Error processing Sepay webhook: {}", e.getMessage(), e);
            // Even if processing fails, returning 200 OK or 400 is fine, but for signature/parse errors we catch them.
            return ResponseEntity.badRequest().body(
                    ApiResponse.<Object>builder().success(false).message("Error processing webhook").build()
            );
        }
        
        return ResponseUtil.success("Webhook received successfully", null);
    }
}
