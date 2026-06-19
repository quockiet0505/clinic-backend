package com.clinic.repository.base;

import com.clinic.entity.base.QuickAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuickActionRepository extends JpaRepository<QuickAction, Integer> {
    List<QuickAction> findByIsActiveTrueOrderByDisplayOrderAsc();
}