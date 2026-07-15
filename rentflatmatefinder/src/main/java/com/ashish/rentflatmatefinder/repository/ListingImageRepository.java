package com.ashish.rentflatmatefinder.repository;

import com.ashish.rentflatmatefinder.entity.ListingImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ListingImageRepository
        extends JpaRepository<ListingImage, Long> {

}