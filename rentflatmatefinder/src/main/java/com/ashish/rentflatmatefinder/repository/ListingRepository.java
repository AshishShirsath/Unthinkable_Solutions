package com.ashish.rentflatmatefinder.repository;

import com.ashish.rentflatmatefinder.entity.Listing;
import com.ashish.rentflatmatefinder.entity.ListingStatus;
import com.ashish.rentflatmatefinder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

    List<Listing> findByDeletedFalse();

    List<Listing> findByOwnerAndDeletedFalse(User owner);

    List<Listing> findByStatusAndDeletedFalse(ListingStatus status);

    @Query("SELECT l FROM Listing l WHERE l.status = :status AND l.deleted = false " +
           "AND (:city IS NULL OR LOWER(l.city) LIKE LOWER(CONCAT('%', :city, '%'))) " +
           "AND (:maxBudget IS NULL OR l.rent <= :maxBudget) " +
           "AND (:minBudget IS NULL OR l.rent >= :minBudget)")
    List<Listing> findByFilters(
            @Param("status") ListingStatus status,
            @Param("city") String city,
            @Param("minBudget") BigDecimal minBudget,
            @Param("maxBudget") BigDecimal maxBudget
    );
}