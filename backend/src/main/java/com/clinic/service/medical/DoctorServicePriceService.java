package com.clinic.service.medical;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.dto.common.PageResponse;
import com.clinic.dto.medical.DoctorServicePriceFilterRequest;
import com.clinic.dto.medical.DoctorServicePriceRequest;
import com.clinic.dto.medical.DoctorServicePriceResponse;
import com.clinic.entity.medical.DoctorServicePrice;
import com.clinic.entity.staff.Staff;
import com.clinic.mapper.medical.DoctorServicePriceMapper;
import com.clinic.repository.medical.DoctorServicePriceRepository;
import com.clinic.repository.staff.StaffRepository;
import com.clinic.specification.medical.DoctorServicePriceSpecification;
import com.clinic.util.FilterUtils;

import lombok.RequiredArgsConstructor;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class DoctorServicePriceService {
    private final DoctorServicePriceRepository priceRepository;
    private final StaffRepository staffRepository;
    private final DoctorServicePriceMapper priceMapper;

    @Transactional(readOnly = true)
    public PageResponse<DoctorServicePriceResponse> getAll(DoctorServicePriceFilterRequest filter) {
        Specification<DoctorServicePrice> spec = DoctorServicePriceSpecification.filterBy(filter);
        Pageable pageable = buildPageable(filter);
        Page<DoctorServicePrice> page = priceRepository.findAll(spec, pageable);
        return FilterUtils.buildPageResponse(page.map(priceMapper::toResponse));
    }

    private Pageable buildPageable(DoctorServicePriceFilterRequest filter) {
        String sortBy = filter.getSortBy() != null ? filter.getSortBy() : "staff.fullName";
        if ("doctorName".equals(sortBy)) {
            sortBy = "staff.fullName";
        } else if ("serviceName".equals(sortBy) || "price".equals(sortBy)) {
            sortBy = "originalPrice";
        }
        String sortDir = filter.getSortDir() != null ? filter.getSortDir() : "ASC";
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? filter.getSize() : 20;
        return PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
    }

    @Transactional
    public DoctorServicePriceResponse createOrUpdate(
            DoctorServicePriceRequest request
    ) {
        Staff staff = staffRepository
                .findById(request.getStaffId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        DoctorServicePrice priceConfig = priceRepository
                .findByStaff_StaffId(staff.getStaffId())
                .orElse(new DoctorServicePrice());

        priceConfig.setStaff(staff);
        priceConfig.setOriginalPrice(request.getOriginalPrice());
        priceConfig.setDiscountAmount(request.getDiscountPrice());

        return priceMapper.toResponse(priceRepository.save(priceConfig));
    }

    @Transactional(readOnly = true)
    public List<DoctorServicePriceResponse> getAllLegacy() {
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
