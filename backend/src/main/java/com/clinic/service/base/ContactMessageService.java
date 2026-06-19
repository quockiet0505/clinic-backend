package com.clinic.service.base;

import com.clinic.common.enums.MessageStatus;
import com.clinic.dto.base.ContactMessageRequest;
import com.clinic.entity.base.ContactMessage;
import com.clinic.repository.base.ContactMessageRepository;
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
}
