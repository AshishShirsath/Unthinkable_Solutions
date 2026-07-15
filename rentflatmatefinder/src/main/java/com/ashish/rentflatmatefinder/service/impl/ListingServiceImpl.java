package com.ashish.rentflatmatefinder.service.impl;

import com.ashish.rentflatmatefinder.dto.request.CreateListingRequest;
import com.ashish.rentflatmatefinder.dto.request.UpdateListingRequest;
import com.ashish.rentflatmatefinder.dto.response.ListingResponse;
import com.ashish.rentflatmatefinder.entity.*;
import com.ashish.rentflatmatefinder.exception.BadRequestException;
import com.ashish.rentflatmatefinder.exception.ResourceNotFoundException;
import com.ashish.rentflatmatefinder.exception.UnauthorizedException;
import com.ashish.rentflatmatefinder.mapper.ListingMapper;
import com.ashish.rentflatmatefinder.repository.ListingRepository;
import com.ashish.rentflatmatefinder.repository.UserRepository;
import com.ashish.rentflatmatefinder.service.ListingService;
import com.ashish.rentflatmatefinder.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ListingServiceImpl implements ListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final ListingMapper listingMapper;

    @Override
    public ListingResponse createListing(CreateListingRequest request) {
        String email = SecurityUtils.getCurrentUserEmail();
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Listing listing = listingMapper.toEntity(request);
        listing.setOwner(owner);
        listing.setStatus(ListingStatus.AVAILABLE);
        listing.setDeleted(false);

        if (listing.getImages() == null) {
            listing.setImages(new java.util.ArrayList<>());
        }

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (String url : request.getImageUrls()) {
                ListingImage image = ListingImage.builder()
                        .imageUrl(url)
                        .listing(listing)
                        .build();
                listing.getImages().add(image);
            }
        }

        return listingMapper.toResponse(listingRepository.save(listing));
    }

    @Override
    public ListingResponse updateListing(Long listingId, UpdateListingRequest request) {
        String email = SecurityUtils.getCurrentUserEmail();
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (!listing.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You are not the owner of this listing");
        }

        if (request.getTitle() != null) listing.setTitle(request.getTitle());
        if (request.getDescription() != null) listing.setDescription(request.getDescription());
        if (request.getCity() != null) listing.setCity(request.getCity());
        if (request.getLocality() != null) listing.setLocality(request.getLocality());
        if (request.getAddress() != null) listing.setAddress(request.getAddress());
        if (request.getRent() != null) listing.setRent(request.getRent());
        if (request.getDeposit() != null) listing.setDeposit(request.getDeposit());
        if (request.getAvailableFrom() != null) listing.setAvailableFrom(request.getAvailableFrom());
        if (request.getRoomType() != null) listing.setRoomType(request.getRoomType());
        if (request.getFurnishingStatus() != null) listing.setFurnishingStatus(request.getFurnishingStatus());

        return listingMapper.toResponse(listingRepository.save(listing));
    }

    @Override
    public void deleteListing(Long listingId) {
        String email = SecurityUtils.getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        boolean isAdmin = currentUser.getRole().getName() == RoleName.ADMIN;
        if (!isAdmin && !listing.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not the owner of this listing");
        }

        listing.setDeleted(true);
        listingRepository.save(listing);
    }

    @Override
    @Transactional(readOnly = true)
    public ListingResponse getListingById(Long listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
        if (listing.getDeleted()) {
            throw new ResourceNotFoundException("Listing not found");
        }
        return listingMapper.toResponse(listing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListingResponse> getAllListings(String city, BigDecimal minBudget, BigDecimal maxBudget) {
        return listingRepository.findByFilters(ListingStatus.AVAILABLE, city, minBudget, maxBudget)
                .stream()
                .map(listingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListingResponse> getMyListings() {
        String email = SecurityUtils.getCurrentUserEmail();
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return listingRepository.findByOwnerAndDeletedFalse(owner).stream()
                .map(listingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ListingResponse markAsFilled(Long listingId) {
        String email = SecurityUtils.getCurrentUserEmail();
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (!listing.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You are not the owner of this listing");
        }

        listing.setStatus(ListingStatus.FILLED);
        return listingMapper.toResponse(listingRepository.save(listing));
    }
}