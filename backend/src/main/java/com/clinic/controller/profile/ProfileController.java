package com.clinic.controller.profile;

import com.clinic.dto.auth.ChangePasswordRequest;
import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.profile.UpdateProfileRequest;
import com.clinic.dto.profile.UserProfileResponse;
import com.clinic.service.profile.ProfileService;
import com.clinic.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {
        return ResponseUtil.success("Success", profileService.getMyProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(@RequestBody UpdateProfileRequest request) {
        return ResponseUtil.success("Profile updated", profileService.updateProfile(request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(request);
        return ResponseUtil.success("Password changed successfully", null);
    }
}