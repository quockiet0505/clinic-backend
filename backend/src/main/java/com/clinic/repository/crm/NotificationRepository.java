package com.clinic.repository.crm;

import com.clinic.entity.crm.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer>, JpaSpecificationExecutor<Notification> {
    List<Notification> findByAccount_AccountIdOrderBySentAtDesc(Integer accountId);
}