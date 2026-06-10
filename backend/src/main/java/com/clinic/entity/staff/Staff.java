package com.clinic.entity.staff;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.clinic.common.enums.StaffType;
import com.clinic.entity.auth.Account;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Integer staffId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id", unique = true)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expertise_id")
    private Expertise expertise;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(length = 10)
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "staff_type", nullable = false)
    private StaffType staffType;

    @Column(length = 100)
    private String experience;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "featured_priority")
    private Integer featuredPriority = 0;

    @Column(name = "is_deleted")
    private Integer isDeleted = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void setEmail(String string) {
     // TODO Auto-generated method stub
     throw new UnsupportedOperationException("Unimplemented method 'setEmail'");
    }

    public void setPassword(String encode) {
     // TODO Auto-generated method stub
     throw new UnsupportedOperationException("Unimplemented method 'setPassword'");
    }
}