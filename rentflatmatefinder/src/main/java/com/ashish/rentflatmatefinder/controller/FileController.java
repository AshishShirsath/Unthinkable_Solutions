package com.ashish.rentflatmatefinder.controller;

import com.ashish.rentflatmatefinder.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private static final String UPLOAD_DIR = "uploads";

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<List<String>>> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;
                String extension = "";
                String originalFilename = file.getOriginalFilename();
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String filename = UUID.randomUUID().toString() + extension;
                Path filePath = uploadPath.resolve(filename);
                Files.copy(file.getInputStream(), filePath);
                // Return relative URL that our WebMvcConfigurer serves
                urls.add("http://localhost:8080/uploads/" + filename);
            }
            return ResponseEntity.ok(ApiResponse.success("Files uploaded successfully", urls));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(ApiResponse.error("Failed to upload files: " + e.getMessage()));
        }
    }
}
