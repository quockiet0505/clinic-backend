package com.clinic.controller.common;

import com.clinic.dto.common.ApiResponse;
import com.clinic.service.common.FileStorageService;
import com.clinic.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/image")
    public ResponseEntity<ApiResponse<String>> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = fileStorageService.storeFile(file);
        return ResponseUtil.success("Tải ảnh lên thành công", url);
    }
}
