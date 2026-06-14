package com.clinic.service.medical;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.clinic.dto.medical.DoctorServicePriceRequest;
import com.clinic.dto.medical.DoctorServicePriceResponse;
import com.clinic.entity.medical.DoctorServicePrice;
import com.clinic.entity.medical.Service;
import com.clinic.entity.staff.Staff;
import com.clinic.mapper.medical.DoctorServicePriceMapper;
import com.clinic.repository.medical.DoctorServicePriceRepository;
import com.clinic.repository.medical.ServiceRepository;
import com.clinic.repository.staff.StaffRepository;

import lombok.RequiredArgsConstructor;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class DoctorServicePriceService {
    private final DoctorServicePriceRepository priceRepository;
    private final StaffRepository staffRepository;
    private final ServiceRepository serviceRepository;
    private final DoctorServicePriceMapper priceMapper;

    @Transactional
    public DoctorServicePriceResponse createOrUpdate(
            DoctorServicePriceRequest request
    ) {

        Staff staff = staffRepository
                .findById(request.getStaffId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Service service = serviceRepository
                .findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        DoctorServicePrice priceConfig =
                priceRepository
                        .findByStaff_StaffIdAndService_ServiceId(
                                staff.getStaffId(),
                                service.getServiceId()
                        )
                        .orElse(new DoctorServicePrice());

        priceConfig.setStaff(staff);
        priceConfig.setService(service);

        priceConfig.setOriginalPrice(
                request.getOriginalPrice()
        );

        priceConfig.setDiscountPrice(
                request.getDiscountPrice()
        );

        return priceMapper.toResponse(
                priceRepository.save(priceConfig)
        );
    }

    @Transactional(readOnly = true)
    public List<DoctorServicePriceResponse> getAll() {
        return priceRepository.findAll().stream()
                .map(priceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Integer id) {
        if (!priceRepository.existsById(id)) {
            throw new RuntimeException("Price configuration not found");
        }
        priceRepository.deleteById(id);
    }
}