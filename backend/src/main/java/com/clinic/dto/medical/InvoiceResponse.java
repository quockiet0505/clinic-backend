package com.clinic.dto.medical;

import com.clinic.common.enums.InvoiceStatus;
import com.clinic.common.enums.PaymentMethod;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InvoiceResponse {
    private Integer invoiceId;
    private Integer recordId;
    private Integer patientId;
    private String patientName;
    private String patientPhone;
    private BigDecimal totalPrice;
    private PaymentMethod paymentMethod;
    private InvoiceStatus status;
    private List<InvoiceItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
