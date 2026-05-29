package com.clinic.util;

import org.springframework.http.ResponseEntity;

import com.clinic.dto.common.ApiResponse;

public class ResponseUtil {

    private ResponseUtil() {}

    public static <T> ResponseEntity<ApiResponse<T>> success(
            String message,
            T data
    ) {

        return ResponseEntity.ok(
                ApiResponse.<T>builder()
                        .success(true)
                        .message(message)
                        .data(data)
                        .build()
        );
    }
}