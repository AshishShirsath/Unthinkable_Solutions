package com.ashish.rentflatmatefinder.repository;

import com.ashish.rentflatmatefinder.entity.InterestRequest;
import com.ashish.rentflatmatefinder.entity.InterestStatus;
import com.ashish.rentflatmatefinder.entity.Listing;
import com.ashish.rentflatmatefinder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterestRequestRepository extends JpaRepository<InterestRequest, Long> {
    List<InterestRequest> findByTenant(User tenant);
    List<InterestRequest> findByListingOwner(User owner);
    Optional<InterestRequest> findByTenantAndListing(User tenant, Listing listing);
    boolean existsByTenantAndListingAndStatusNot(User tenant, Listing listing, InterestStatus status);
    List<InterestRequest> findByListing(Listing listing);
    List<InterestRequest> findByListingOwnerAndStatus(User owner, InterestStatus status);
}
