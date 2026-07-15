package com.ashish.rentflatmatefinder.service;

import com.ashish.rentflatmatefinder.dto.request.InterestRequestDto;
import com.ashish.rentflatmatefinder.dto.response.CompatibilityResponse;
import com.ashish.rentflatmatefinder.dto.response.InterestRequestResponse;
import com.ashish.rentflatmatefinder.entity.*;
import com.ashish.rentflatmatefinder.exception.BadRequestException;
import com.ashish.rentflatmatefinder.exception.ResourceNotFoundException;
import com.ashish.rentflatmatefinder.exception.UnauthorizedException;
import com.ashish.rentflatmatefinder.repository.*;
import com.ashish.rentflatmatefinder.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterestService {

    private final InterestRequestRepository interestRequestRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final CompatibilityService compatibilityService;
    private final EmailService emailService;

    @Transactional
    public InterestRequestResponse sendInterest(InterestRequestDto dto) {
        String email = SecurityUtils.getCurrentUserEmail();
        User tenant = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Listing listing = listingRepository.findById(dto.getListingId())
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (listing.getStatus() != ListingStatus.AVAILABLE) {
            throw new BadRequestException("This listing is not available");
        }

        if (listing.getOwner().getId().equals(tenant.getId())) {
            throw new BadRequestException("You cannot express interest in your own listing");
        }

        boolean alreadyExists = interestRequestRepository
                .existsByTenantAndListingAndStatusNot(tenant, listing, InterestStatus.DECLINED);
        if (alreadyExists) {
            throw new BadRequestException("You have already expressed interest in this listing");
        }

        // Compute compatibility score
        CompatibilityResponse compatibility = compatibilityService.computeAndStore(tenant, listing);

        InterestRequest interestRequest = InterestRequest.builder()
                .tenant(tenant)
                .listing(listing)
                .status(InterestStatus.PENDING)
                .compatibilityScore(compatibility.getScore())
                .scoreExplanation(compatibility.getExplanation())
                .message(dto.getMessage())
                .build();

        // ensure not-nullable audit fields are set (some builders leave defaults null)
        interestRequest.setDeleted(false);

        InterestRequest saved = interestRequestRepository.save(interestRequest);

        // Send email to owner if compatibility score is high (> 80)
        if (compatibility.getScore() > 80) {
            emailService.sendInterestNotificationToOwner(
                    listing.getOwner().getEmail(),
                    listing.getOwner().getFirstName(),
                    tenant.getFirstName() + " " + tenant.getLastName(),
                    listing.getTitle(),
                    compatibility.getScore(),
                    compatibility.getExplanation()
            );
        }

        return toResponse(saved);
    }

    @Transactional
    public InterestRequestResponse acceptInterest(Long interestId) {
        String email = SecurityUtils.getCurrentUserEmail();
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        InterestRequest interest = interestRequestRepository.findById(interestId)
                .orElseThrow(() -> new ResourceNotFoundException("Interest request not found"));

        if (!interest.getListing().getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You are not the owner of this listing");
        }

        if (interest.getStatus() != InterestStatus.PENDING) {
            throw new BadRequestException("Interest request is no longer pending");
        }

        interest.setStatus(InterestStatus.ACCEPTED);
        interestRequestRepository.save(interest);

        // Create chat room
        ChatRoom chatRoom = ChatRoom.builder()
                .interestRequest(interest)
                .tenant(interest.getTenant())
                .owner(owner)
                .build();
        chatRoomRepository.save(chatRoom);

        // Notify tenant
        emailService.sendInterestAcceptedToTenant(
                interest.getTenant().getEmail(),
                interest.getTenant().getFirstName(),
                interest.getListing().getTitle(),
                owner.getFirstName() + " " + owner.getLastName()
        );

        return toResponse(interest);
    }

    @Transactional
    public InterestRequestResponse declineInterest(Long interestId) {
        String email = SecurityUtils.getCurrentUserEmail();
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        InterestRequest interest = interestRequestRepository.findById(interestId)
                .orElseThrow(() -> new ResourceNotFoundException("Interest request not found"));

        if (!interest.getListing().getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You are not the owner of this listing");
        }

        if (interest.getStatus() != InterestStatus.PENDING) {
            throw new BadRequestException("Interest request is no longer pending");
        }

        interest.setStatus(InterestStatus.DECLINED);
        interestRequestRepository.save(interest);

        // Notify tenant
        emailService.sendInterestDeclinedToTenant(
                interest.getTenant().getEmail(),
                interest.getTenant().getFirstName(),
                interest.getListing().getTitle()
        );

        return toResponse(interest);
    }

    @Transactional(readOnly = true)
    public List<InterestRequestResponse> getSentInterests() {
        String email = SecurityUtils.getCurrentUserEmail();
        User tenant = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return interestRequestRepository.findByTenant(tenant).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InterestRequestResponse> getReceivedInterests() {
        String email = SecurityUtils.getCurrentUserEmail();
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return interestRequestRepository.findByListingOwner(owner).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    private InterestRequestResponse toResponse(InterestRequest ir) {
        return InterestRequestResponse.builder()
                .id(ir.getId())
                .tenantId(ir.getTenant().getId())
                .tenantName(ir.getTenant().getFirstName() + " " + ir.getTenant().getLastName())
                .tenantEmail(ir.getTenant().getEmail())
                .listingId(ir.getListing().getId())
                .listingTitle(ir.getListing().getTitle())
                .listingCity(ir.getListing().getCity())
                .status(ir.getStatus())
                .compatibilityScore(ir.getCompatibilityScore())
                .scoreExplanation(ir.getScoreExplanation())
                .message(ir.getMessage())
                .chatRoomId(ir.getChatRoom() != null ? ir.getChatRoom().getId() : null)
                .createdAt(ir.getCreatedAt())
                .build();
    }
}
