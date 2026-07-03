package com.clinic.dto.ai;

import lombok.Data;
import java.util.List;

@Data
public class ChatRequestDTO {
    private String message;
    private String sessionId; // for keeping track, though frontend sends history
    private List<MessageHistoryDTO> history;
}
