package com.clinic.repository.crm;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.entity.crm.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    // Fetch notifications for a specific user, ordered by newest first
    List<Notification> findByAccount_AccountIdOrderBySentAtDesc(Integer accountId);
}