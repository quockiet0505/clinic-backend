package com.clinic.entity.service;

import java.time.LocalDateTime;

import com.clinic.entity.staff.Staff;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "service_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Integer resultId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private ServiceOrder serviceOrder;

    @Column(name = "result_data", columnDefinition = "TEXT")
    private String resultData;

    @Column(columnDefinition = "TEXT")
    private String conclusion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entered_by", nullable = false)
    private Staff enteredBy;

    @Column(name = "entered_at", updatable = false)
    private LocalDateTime enteredAt;

    @PrePersist
    protected void onCreate() {
        this.enteredAt = LocalDateTime.now();
    }
}