package com.clinic.entity.auth;

import com.clinic.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "device_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeviceToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Integer tokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "token", nullable = false, length = 255)
    private String token;

    @Column(name = "device_type", length = 50)
    private String deviceType; // "ANDROID", "IOS", "WEB"
}
