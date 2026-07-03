package com.clinic.dto.ai;

import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class GenerateRequestDTO {
    private String message;
    private List<MessageHistoryDTO> history;
    private String intent;
    @JsonProperty("rewritten_query")
    private String rewrittenQuery;
    @JsonProperty("knowledge_context")
    private String knowledgeContext;
}
