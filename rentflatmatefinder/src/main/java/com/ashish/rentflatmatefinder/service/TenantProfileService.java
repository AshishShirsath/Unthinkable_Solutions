package com.ashish.rentflatmatefinder.service;

import com.ashish.rentflatmatefinder.dto.request.TenantProfileRequest;
import com.ashish.rentflatmatefinder.dto.response.TenantProfileResponse;
import com.ashish.rentflatmatefinder.entity.TenantProfile;
import com.ashish.rentflatmatefinder.entity.User;
import com.ashish.rentflatmatefinder.exception.BadRequestException;
import com.ashish.rentflatmatefinder.exception.ResourceNotFoundException;
import com.ashish.rentflatmatefinder.repository.TenantProfileRepository;
import com.ashish.rentflatmatefinder.repository.UserRepository;
import com.ashish.rentflatmatefinder.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TenantProfileService {

    private final TenantProfileRepository tenantProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public TenantProfileResponse createOrUpdate(TenantProfileRequest request) {
        String email = SecurityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        TenantProfile profile = tenantProfileRepository.findByUser(user)
                .orElse(new TenantProfile());

        profile.setUser(user);
        profile.setPreferredCity(request.getPreferredCity());
        profile.setPreferredLocality(request.getPreferredLocality());
        profile.setMinBudget(request.getMinBudget());
        profile.setMaxBudget(request.getMaxBudget());
        profile.setMoveInDate(request.getMoveInDate());
        profile.setDescription(request.getDescription());

        TenantProfile saved = tenantProfileRepository.save(profile);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TenantProfileResponse getMyProfile() {
        String email = SecurityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        TenantProfile profile = tenantProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant profile not found. Please create one first."));

        return toResponse(profile);
    }

    private TenantProfileResponse toResponse(TenantProfile profile) {
        return TenantProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUser().getId())
                .preferredCity(profile.getPreferredCity())
                .preferredLocality(profile.getPreferredLocality())
                .minBudget(profile.getMinBudget())
                .maxBudget(profile.getMaxBudget())
                .moveInDate(profile.getMoveInDate())
                .description(profile.getDescription())
                .createdAt(profile.getCreatedAt())
                .build();
    }
}
