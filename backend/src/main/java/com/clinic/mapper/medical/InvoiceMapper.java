package com.clinic.mapper.medical;

import com.clinic.dto.medical.InvoiceItemResponse;
import com.clinic.dto.medical.InvoiceResponse;
import com.clinic.entity.medical.Invoice;
import com.clinic.entity.medical.InvoiceItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InvoiceMapper {

    @Mapping(source = "patient.patientId", target = "patientId")
    @Mapping(source = "patient.fullName", target = "patientName")
    @Mapping(source = "patient.phone", target = "patientPhone")
    @Mapping(source = "medicalRecord.recordId", target = "recordId")
    InvoiceResponse toResponse(Invoice invoice);

    InvoiceItemResponse toItemResponse(InvoiceItem item);
}
