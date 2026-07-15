package com.ashish.rentflatmatefinder.repository;

import com.ashish.rentflatmatefinder.entity.TenantProfile;
import com.ashish.rentflatmatefinder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantProfileRepository extends JpaRepository<TenantProfile, Long> {
    Optional<TenantProfile> findByUser(User user);
    boolean existsByUser(User user);
}
