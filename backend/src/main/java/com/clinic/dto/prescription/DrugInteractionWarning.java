package com.clinic.dto.prescription;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrugInteractionWarning {
    private String medicine1;
    private String medicine2;
    private String mechanism;
    private String consequence;
    private String management;
}
