// src/main/java/com/example/backend/HotelOwner/file/LocalFileStorageService.java
package com.example.backend.HotelOwner.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;

@Service
public class LocalFileStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageService.class);

    private final Path rootDir = Paths.get("uploads"); // 프로젝트 루트 기준 ./uploads

    @Override
    public String storeFile(MultipartFile file) {
        try {
            if (!Files.exists(rootDir)) {
                Files.createDirectories(rootDir);
            }
            String original = file.getOriginalFilename();
            String clean = (original == null ? "file" : original).replaceAll("[\\\\/]+", "_");
            String filename = System.currentTimeMillis() + "_" + clean;
            Path target = rootDir.resolve(filename).normalize();
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
            // 정적 리소스 매핑이 /uploads/** 로 되어있다고 가정
            return "/uploads/" + filename;
        } catch (IOException e) {
            log.error("파일 저장 실패 - originalName={}", file.getOriginalFilename(), e);
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    @Override
    public void deleteFile(String urlOrPath) {
        if (urlOrPath == null || urlOrPath.isBlank()) return;
        String candidate = urlOrPath.trim();

        if (candidate.startsWith("http://") || candidate.startsWith("https://")) {
            try {
                candidate = new URI(candidate).getPath();
            } catch (URISyntaxException e) {
                log.warn("잘못된 파일 경로 URI - value={}", urlOrPath, e);
            }
        }

        try {
            // "/uploads/xxx" 혹은 "uploads/xxx" → 로컬 경로로 변환
            if (candidate.contains("/uploads/")) {
                String fn = candidate.substring(candidate.lastIndexOf("/uploads/") + "/uploads/".length());
                Path p = rootDir.resolve(fn).normalize();
                Files.deleteIfExists(p);
                return;
            }
            if (candidate.startsWith("/uploads/")) {
                String fn = candidate.substring("/uploads/".length());
                Path p = rootDir.resolve(fn).normalize();
                Files.deleteIfExists(p);
                return;
            }
            if (candidate.startsWith("uploads/")) {
                Path p = rootDir.resolve(candidate.substring("uploads/".length())).normalize();
                Files.deleteIfExists(p);
                return;
            }

            // 그 외: 절대/상대 경로 모두 시도
            Path p = Paths.get(candidate);
            if (!p.isAbsolute()) {
                p = rootDir.resolve(candidate).normalize();
            }
            Files.deleteIfExists(p);
        } catch (IOException e) {
            log.warn("파일 삭제 실패 - path={}", candidate, e);
        }
    }
}
