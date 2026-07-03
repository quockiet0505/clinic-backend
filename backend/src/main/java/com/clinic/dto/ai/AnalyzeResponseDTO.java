package com.clinic.dto.ai;

import lombok.Data;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class AnalyzeResponseDTO {
    private String intent;
    private Map<String, Object> parameters;
    @JsonProperty("rewritten_query")
    private String rewrittenQuery;
}
