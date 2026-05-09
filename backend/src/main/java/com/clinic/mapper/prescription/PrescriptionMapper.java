package com.clinic.mapper.prescription;

import java.math.BigDecimal;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import com.clinic.dto.prescription.PrescriptionItemResponse;
import com.clinic.dto.prescription.PrescriptionResponse;
import com.clinic.entity.prescription.Prescription;
import com.clinic.entity.prescription.PrescriptionItem;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PrescriptionMapper {

    @Mapping(source = "medicalRecord.recordId", target = "recordId")
    PrescriptionResponse toResponse(Prescription prescription);

    @Mapping(source = "medicine.medicineId", target = "medicineId")
    @Mapping(source = "medicine.name", target = "medicineName")
    @Mapping(source = "medicine.unit", target = "unit")
    @Mapping(target = "subTotal", expression = "java(calculateSubTotal(item))")
    PrescriptionItemResponse toItemResponse(PrescriptionItem item);

    // Custom logic to calculate the subtotal for each medicine line
    @Named("calculateSubTotal")
    default BigDecimal calculateSubTotal(PrescriptionItem item) {
        if (item.getPrice() != null && item.getQuantity() != null) {
            return item.getPrice().multiply(new BigDecimal(item.getQuantity()));
        }
        return BigDecimal.ZERO;
    }
}