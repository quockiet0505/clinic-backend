package com.clinic.dto.base;

import com.clinic.common.enums.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactMessageResponse {
    private Long messageId;
    private String fullName;
    private String phone;
    private String email;
    private String subject;
    private String content;
    private MessageStatus status;
    private LocalDateTime repliedAt;
    private String replyContent;
    private LocalDateTime createdAt;
}
