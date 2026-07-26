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
        if (file.isEmpty()) {
            return ResponseUtil.error("Tệp không được để trống", null);
        }

        // Validate file size (e.g., max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseUtil.error("Kích thước tệp vượt quá giới hạn (5MB)", null);
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
            return ResponseUtil.error("Chỉ hỗ trợ định dạng hình ảnh hoặc PDF", null);
        }

        String url = fileStorageService.storeFile(file);
        return ResponseUtil.success("Tải ảnh lên thành công", url);
    }
}
