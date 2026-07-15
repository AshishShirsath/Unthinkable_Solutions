package com.ashish.rentflatmatefinder.controller;

import com.ashish.rentflatmatefinder.dto.response.ApiResponse;
import com.ashish.rentflatmatefinder.entity.User;
import com.ashish.rentflatmatefinder.entity.Listing;
import com.ashish.rentflatmatefinder.exception.ResourceNotFoundException;
import com.ashish.rentflatmatefinder.repository.ListingRepository;
import com.ashish.rentflatmatefinder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;

    @GetMapping("/users")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllUsers() {
        List<Map<String, Object>> users = userRepository.findAll().stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "name", u.getFirstName() + " " + u.getLastName(),
                        "email", u.getEmail(),
                        "role", u.getRole().getName().name(),
                        "enabled", u.getEnabled()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("All users", users));
    }

    @GetMapping("/listings")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Object>> getAllListings() {
        return ResponseEntity.ok(ApiResponse.success("All listings",
                listingRepository.findAll().stream()
                        .map(l -> Map.<String, Object>of(
                                "id", l.getId(),
                                "title", l.getTitle(),
                                "city", l.getCity(),
                                "rent", l.getRent(),
                                "status", l.getStatus().name(),
                                "owner", l.getOwner().getEmail(),
                                "deleted", l.getDeleted()
                        ))
                        .collect(Collectors.toList())
        ));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> disableUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(false);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("User disabled", null));
    }

    @DeleteMapping("/listings/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteListing(@PathVariable Long id) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
        listing.setDeleted(true);
        listingRepository.save(listing);
        return ResponseEntity.ok(ApiResponse.success("Listing deleted successfully", null));
    }
}
