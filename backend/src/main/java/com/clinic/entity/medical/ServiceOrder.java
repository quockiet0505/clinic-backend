package com.clinic.entity.medical;

import java.time.LocalDateTime;

import com.clinic.common.enums.ServiceOrderStatus;
import com.clinic.entity.base.BaseEntity;
import com.clinic.entity.staff.Staff;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "service_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOrder extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private MedicalRecord medicalRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordered_by", nullable = false)
    private Staff orderedBy;

    @Enumerated(EnumType.STRING)
    private ServiceOrderStatus status = ServiceOrderStatus.ORDERED;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "sample_collected_at")
    private LocalDateTime sampleCollectedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sample_collected_by")
    private Staff sampleCollectedBy;
}