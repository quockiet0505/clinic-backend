package com.clinic.entity.crm;

import com.clinic.entity.auth.Account;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer notificationId;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @Enumerated(EnumType.STRING)
    private Type type;

    private String content;
    private LocalDateTime sentAt;

    public enum Type {
        EMAIL, SYSTEM
    }
}