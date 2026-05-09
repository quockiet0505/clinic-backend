package com.clinic.repository.service;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.entity.service.ServiceResult;

public interface ServiceResultRepository extends JpaRepository<ServiceResult, Integer> {
    Optional<ServiceResult> findByServiceOrder_OrderId(Integer orderId);
}