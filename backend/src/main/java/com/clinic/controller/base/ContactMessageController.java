package com.clinic.controller.base;

import com.clinic.dto.base.ContactMessageRequest;
import com.clinic.dto.common.ApiResponse;
import com.clinic.service.base.ContactMessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
