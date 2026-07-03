package com.clinic.dto.ai;

import lombok.Data;
import java.util.List;

@Data
public class AnalyzeRequestDTO {
    private String message;
    private List<MessageHistoryDTO> history;
}
