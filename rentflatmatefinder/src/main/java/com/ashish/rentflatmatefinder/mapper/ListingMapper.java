package com.ashish.rentflatmatefinder.mapper;

import com.ashish.rentflatmatefinder.dto.request.CreateListingRequest;
import com.ashish.rentflatmatefinder.dto.response.ListingResponse;
import com.ashish.rentflatmatefinder.entity.Listing;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ListingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Listing toEntity(CreateListingRequest request);

    @Mapping(target = "ownerName",
            expression = "java(listing.getOwner().getFirstName() + \" \" + listing.getOwner().getLastName())")
    @Mapping(target = "ownerEmail",
            source = "owner.email")
    @Mapping(target = "imageUrls",
            expression = "java(listing.getImages().stream().map(image -> image.getImageUrl()).toList())")
    ListingResponse toResponse(Listing listing);

}