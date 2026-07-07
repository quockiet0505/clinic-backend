package com.clinic.mapper.medical;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.clinic.dto.medical.DoctorServicePriceResponse;
import com.clinic.entity.medical.DoctorServicePrice;

@Mapper(componentModel = "spring")
public interface DoctorServicePriceMapper {

    @Mapping(target = "staffId", source = "staff.staffId")
    @Mapping(target = "doctorName", source = "staff.fullName")
    @Mapping(target = "imageUrl", source = "staff.imageUrl")
    @Mapping(target = "originalPrice", source = "originalPrice")
    @Mapping(target = "discountAmount", source = "discountAmount")
    @Mapping(
        target = "finalPrice",
        expression =
            "java(entity.getDiscountAmount() != null && entity.getDiscountAmount().compareTo(java.math.BigDecimal.ZERO) > 0 ? entity.getDiscountAmount() : entity.getOriginalPrice())"
    )
    DoctorServicePriceResponse toResponse(DoctorServicePrice entity);
}
