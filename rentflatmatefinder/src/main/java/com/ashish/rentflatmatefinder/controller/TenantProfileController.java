package com.ashish.rentflatmatefinder.controller;

import com.ashish.rentflatmatefinder.dto.request.TenantProfileRequest;
import com.ashish.rentflatmatefinder.dto.response.ApiResponse;
import com.ashish.rentflatmatefinder.dto.response.TenantProfileResponse;
import com.ashish.rentflatmatefinder.service.TenantProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tenant-profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TENANT')")
public class TenantProfileController {

    private final TenantProfileService tenantProfileService;

    @PostMapping
    public ResponseEntity<ApiResponse<TenantProfileResponse>> createOrUpdate(
            @Valid @RequestBody TenantProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tenant profile saved",
                tenantProfileService.createOrUpdate(request)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<TenantProfileResponse>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.success(
                "Profile fetched",
                tenantProfileService.getMyProfile()));
    }
}
