package com.clinic.entity.medical;

import java.time.LocalDateTime;

import com.clinic.common.enums.ServiceOrderStatus;
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
import jakarta.persistence.PrePersist;
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
public class ServiceOrder {
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

    @Column(name = "custom_service_name", length = 255)
    private String customServiceName;

    @Column(name = "doctor_note", columnDefinition = "TEXT")
    private String doctorNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordered_by", nullable = false)
    private Staff orderedBy;

    @Column(name = "price_at_time", precision = 10, scale = 2, nullable = false)
    private java.math.BigDecimal priceAtTime;

    @Enumerated(EnumType.STRING)
    private ServiceOrderStatus status = ServiceOrderStatus.ORDERED;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "sample_collected_at")
    private LocalDateTime sampleCollectedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sample_collected_by")
    private Staff sampleCollectedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
