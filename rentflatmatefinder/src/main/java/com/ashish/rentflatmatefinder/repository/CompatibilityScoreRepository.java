package com.ashish.rentflatmatefinder.repository;

import com.ashish.rentflatmatefinder.entity.CompatibilityScore;
import com.ashish.rentflatmatefinder.entity.Listing;
import com.ashish.rentflatmatefinder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompatibilityScoreRepository extends JpaRepository<CompatibilityScore, Long> {
    Optional<CompatibilityScore> findByTenantAndListing(User tenant, Listing listing);
    boolean existsByTenantAndListing(User tenant, Listing listing);
}
