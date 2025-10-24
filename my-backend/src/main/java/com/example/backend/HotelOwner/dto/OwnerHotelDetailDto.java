// src/main/java/com/example/backend/HotelOwner/dto/OwnerHotelDetailDto.java
package com.example.backend.HotelOwner.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class OwnerHotelDetailDto {
    private Long id;
    private String name;
    private String address;
    private Integer starRating;
    private String description;


    private Amenities amenities;

    // 참고용: 상태 문자열
    private String approvalStatus;

    @Getter
    @NoArgsConstructor
    public static class Amenities {
        private List<String> left;
        private List<String> right;

        public Amenities(List<String> left, List<String> right) {
            this.left = left == null ? null : new ArrayList<>(left);
            this.right = right == null ? null : new ArrayList<>(right);
        }

        public List<String> getLeft() {
            return left == null ? null : new ArrayList<>(left);
        }

        public List<String> getRight() {
            return right == null ? null : new ArrayList<>(right);
        }
    }
}
