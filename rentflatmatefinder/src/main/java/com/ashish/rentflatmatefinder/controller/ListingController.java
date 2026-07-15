package com.ashish.rentflatmatefinder.controller;

import com.ashish.rentflatmatefinder.dto.request.CreateListingRequest;
import com.ashish.rentflatmatefinder.dto.request.UpdateListingRequest;
import com.ashish.rentflatmatefinder.dto.response.ApiResponse;
import com.ashish.rentflatmatefinder.dto.response.ListingResponse;
import com.ashish.rentflatmatefinder.service.ListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<ListingResponse>> createListing(
            @Valid @RequestBody CreateListingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Listing created successfully",
                        listingService.createListing(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ListingResponse>>> getAllListings(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) BigDecimal minBudget,
            @RequestParam(required = false) BigDecimal maxBudget) {
        return ResponseEntity.ok(ApiResponse.success("Listings fetched",
                listingService.getAllListings(city, minBudget, maxBudget)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ListingResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Listing fetched",
                listingService.getListingById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<ListingResponse>> updateListing(
            @PathVariable Long id,
            @Valid @RequestBody UpdateListingRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Listing updated",
                listingService.updateListing(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteListing(@PathVariable Long id) {
        listingService.deleteListing(id);
        return ResponseEntity.ok(ApiResponse.success("Listing deleted", null));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<ListingResponse>>> getMyListings() {
        return ResponseEntity.ok(ApiResponse.success("My listings",
                listingService.getMyListings()));
    }

    @PutMapping("/{id}/fill")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<ListingResponse>> markAsFilled(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Listing marked as filled",
                listingService.markAsFilled(id)));
}

}