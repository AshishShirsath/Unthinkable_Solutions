package com.ashish.rentflatmatefinder.controller;

import com.ashish.rentflatmatefinder.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public ApiResponse<String> health() {

        return ApiResponse.<String>builder()
                .success(true)
                .message("Application is running successfully")
                .data("UP")
                .timestamp(LocalDateTime.now())
                .build();
    }
}