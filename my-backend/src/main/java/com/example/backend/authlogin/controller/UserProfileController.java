package com.example.backend.authlogin.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.HotelOwner.service.FileStorageService;
import com.example.backend.authlogin.domain.User;
import com.example.backend.authlogin.repository.LoginRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final LoginRepository loginRepository;
    private final FileStorageService fileStorageService;

    private String extractEmail(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        return (principal instanceof String s) ? s : null;
    }

    @PatchMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfileImage(Authentication authentication,
                                                @RequestPart("file") MultipartFile file) {
        String email = extractEmail(authentication);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인이 필요합니다."));
        }
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "업로드할 이미지를 선택해 주세요."));
        }

        User user = loginRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String storedPath = fileStorageService.storeFile(file);
        user.setProfileImageUrl(storedPath);
        loginRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "profileImageUrl", storedPath
        ));
    }
}
