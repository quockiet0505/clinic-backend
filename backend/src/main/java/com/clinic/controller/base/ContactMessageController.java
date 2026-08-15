package com.clinic.controller.base;

import com.clinic.dto.base.ContactMessageRequest;
import com.clinic.dto.common.ApiResponse;
import com.clinic.service.base.ContactMessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import com.clinic.dto.base.ContactMessageResponse;
import com.clinic.common.enums.MessageStatus;

@RestController
@RequestMapping("/api/v1/contact-messages")
public class ContactMessageController {

    @Autowired
    private ContactMessageService contactMessageService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createContactMessage(@Valid @RequestBody ContactMessageRequest request) {
        contactMessageService.createContactMessage(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<Page<ContactMessageResponse>>> getAllContactMessages(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String status) {
        Page<ContactMessageResponse> messages = contactMessageService.getAllContactMessages(pageable, status);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PutMapping("/admin/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateMessageStatus(
            @PathVariable Long id,
            @RequestParam MessageStatus status) {
        contactMessageService.updateMessageStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
