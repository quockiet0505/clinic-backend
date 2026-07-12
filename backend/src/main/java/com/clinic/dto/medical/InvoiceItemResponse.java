package com.clinic.dto.medical;

import com.clinic.common.enums.InvoiceItemType;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class InvoiceItemResponse {
    private Integer itemId;
    private InvoiceItemType itemType;
    private Integer referenceId;
    private String description;
    private BigDecimal priceAtTime;
}
