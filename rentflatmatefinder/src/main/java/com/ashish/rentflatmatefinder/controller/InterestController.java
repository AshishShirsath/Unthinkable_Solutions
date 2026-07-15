package com.ashish.rentflatmatefinder.controller;

import com.ashish.rentflatmatefinder.dto.request.InterestRequestDto;
import com.ashish.rentflatmatefinder.dto.response.ApiResponse;
import com.ashish.rentflatmatefinder.dto.response.InterestRequestResponse;
import com.ashish.rentflatmatefinder.service.InterestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/interests")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;

    @PostMapping
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ApiResponse<InterestRequestResponse>> sendInterest(
            @Valid @RequestBody InterestRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Interest sent successfully",
                        interestService.sendInterest(dto)));
    }

    @GetMapping("/sent")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ApiResponse<List<InterestRequestResponse>>> getSent() {
        return ResponseEntity.ok(ApiResponse.success("Sent interests",
                interestService.getSentInterests()));
    }

    @GetMapping("/received")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<InterestRequestResponse>>> getReceived() {
        return ResponseEntity.ok(ApiResponse.success("Received interests",
                interestService.getReceivedInterests()));
    }

    @PutMapping("/{id}/accept")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<InterestRequestResponse>> accept(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Interest accepted",
                interestService.acceptInterest(id)));
    }

    @PutMapping("/{id}/decline")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<InterestRequestResponse>> decline(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Interest declined",
                interestService.declineInterest(id)));
    }
}
