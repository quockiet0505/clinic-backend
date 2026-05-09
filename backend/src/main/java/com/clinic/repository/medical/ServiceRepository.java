package com.clinic.repository.medical;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.entity.medical.Service;

public interface ServiceRepository extends JpaRepository<Service, Integer> {
    List<Service> findByIsDeleted(Integer isDeleted);
}