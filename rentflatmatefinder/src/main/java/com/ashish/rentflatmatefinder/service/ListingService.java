package com.ashish.rentflatmatefinder.service;

import com.ashish.rentflatmatefinder.dto.request.CreateListingRequest;
import com.ashish.rentflatmatefinder.dto.request.UpdateListingRequest;
import com.ashish.rentflatmatefinder.dto.response.ListingResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ListingService {

    ListingResponse createListing(CreateListingRequest request);

    ListingResponse updateListing(Long listingId, UpdateListingRequest request);

    void deleteListing(Long listingId);

    ListingResponse getListingById(Long listingId);

    List<ListingResponse> getAllListings(String city, BigDecimal minBudget, BigDecimal maxBudget);

    List<ListingResponse> getMyListings();

    ListingResponse markAsFilled(Long listingId);
}