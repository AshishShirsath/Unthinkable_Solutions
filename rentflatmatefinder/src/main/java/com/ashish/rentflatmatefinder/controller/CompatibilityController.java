package com.ashish.rentflatmatefinder.controller;

import com.ashish.rentflatmatefinder.dto.response.CompatibilityResponse;
import com.ashish.rentflatmatefinder.dto.response.ApiResponse;
import com.ashish.rentflatmatefinder.service.CompatibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/listings")
@RequiredArgsConstructor
public class CompatibilityController {

    private final CompatibilityService compatibilityService;

    @GetMapping("/{id}/compatibility")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ApiResponse<CompatibilityResponse>> getCompatibility(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Compatibility score computed",
                compatibilityService.getCompatibility(id)));
    }
}
