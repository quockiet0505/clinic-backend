package com.clinic.repository.service;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.entity.service.ServiceOrder;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Integer> {
    List<ServiceOrder> findByMedicalRecord_RecordId(Integer recordId);
}