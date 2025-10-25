// src/main/java/com/example/backend/HotelOwner/dto/HotelImageDto.java
package com.example.backend.HotelOwner.dto;

import com.example.backend.HotelOwner.domain.HotelImage;
import lombok.*;

import java.net.URI;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HotelImageDto {
    private Long id;
    private String url;
    private boolean cover;
    private int sortNo;
    private String caption;

    public static HotelImageDto from(HotelImage e) {
        return HotelImageDto.builder()
                .id(e.getId())
                .url(normalizeImageUrl(e.getUrl()))
                .cover(e.isCover())
                .sortNo(e.getSortNo())
                .caption(e.getCaption())
                .build();
    }

    private static String normalizeImageUrl(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }

        String trimmed = url.trim();
        int uploadsIndex = trimmed.indexOf("/uploads/");
        if (uploadsIndex >= 0) {
            return trimmed.substring(uploadsIndex);
        }

        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            try {
                URI uri = URI.create(trimmed);
                String path = uri.getPath();
                if (path != null && !path.isBlank()) {
                    return path.startsWith("/") ? path : "/" + path;
                }
            } catch (IllegalArgumentException ignored) {
                return trimmed;
            }
            return trimmed;
        }

        return trimmed.startsWith("/") ? trimmed : "/" + trimmed;
    }
}
