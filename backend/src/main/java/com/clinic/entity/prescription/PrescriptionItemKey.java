package com.clinic.entity.prescription;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Composite Key class for prescription_item table
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionItemKey implements Serializable {
    
    @Column(name = "prescription_id")
    private Integer prescriptionId;

    @Column(name = "medicine_id")
    private Integer medicineId;

    // Must override equals and hashCode for Composite Keys in Hibernate
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrescriptionItemKey that = (PrescriptionItemKey) o;
        return Objects.equals(prescriptionId, that.prescriptionId) &&
               Objects.equals(medicineId, that.medicineId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prescriptionId, medicineId);
    }
}