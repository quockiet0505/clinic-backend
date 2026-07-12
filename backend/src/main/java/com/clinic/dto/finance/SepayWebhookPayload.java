package com.clinic.dto.finance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SepayWebhookPayload {
    private String gateway;
    private String transactionDate;
    private String accountNumber;
    private String subAccount;
    private String content;
    private String transferType;
    private Double transferAmount;
    private Double accumulated;
    private Long id;
    private String referenceCode;
    private String description;
}
