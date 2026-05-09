package com.clinic.repository.service;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.entity.service.Service;

public interface ServiceRepository extends JpaRepository<Service, Integer> {
    List<Service> findByIsDeleted(Integer isDeleted);
}