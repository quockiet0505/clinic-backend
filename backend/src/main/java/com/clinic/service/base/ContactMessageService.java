package com.clinic.service.base;

import com.clinic.common.enums.MessageStatus;
import com.clinic.dto.base.ContactMessageRequest;
import com.clinic.entity.base.ContactMessage;
import com.clinic.repository.base.ContactMessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.clinic.dto.base.ContactMessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactMessageService {

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @Transactional
    public void createContactMessage(ContactMessageRequest request) {
        ContactMessage message = new ContactMessage();
        message.setFullName(request.getFullName());
        message.setPhone(request.getPhone());
        message.setEmail(request.getEmail());
        message.setSubject(request.getSubject() != null && !request.getSubject().isEmpty() ? request.getSubject() : "Khác");
        message.setContent(request.getContent());
        message.setStatus(MessageStatus.PENDING);
        
        contactMessageRepository.save(message);
    }

    public Page<ContactMessageResponse> getAllContactMessages(Pageable pageable, String status) {
        Page<ContactMessage> page;
        if (status != null && !status.trim().isEmpty()) {
            page = contactMessageRepository.findByStatus(MessageStatus.valueOf(status.toUpperCase()), pageable);
        } else {
            page = contactMessageRepository.findAll(pageable);
        }
        return page.map(this::mapToResponse);
    }

    @Transactional
    public void updateMessageStatus(Long id, MessageStatus status) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ContactMessage not found with id: " + id));
        message.setStatus(status);
        contactMessageRepository.save(message);
    }

    private ContactMessageResponse mapToResponse(ContactMessage message) {
        return new ContactMessageResponse(
                message.getMessageId(),
                message.getFullName(),
                message.getPhone(),
                message.getEmail(),
                message.getSubject(),
                message.getContent(),
                message.getStatus(),
                message.getRepliedAt(),
                message.getReplyContent(),
                message.getCreatedAt()
        );
    }
}
