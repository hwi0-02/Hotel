package com.example.backend.HotelOwner.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateHotelRequest {
    private String name;
    private String address;
    private Integer starRating;
    private String description;

    private Double lat;
    private Double lng;
    private String thumbnailUrl;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Amenities {
        private List<String> left;
        private List<String> right;

        public List<String> getLeft() {
            return left == null ? null : new ArrayList<>(left);
        }

        public void setLeft(List<String> left) {
            this.left = left == null ? null : new ArrayList<>(left);
        }

        public List<String> getRight() {
            return right == null ? null : new ArrayList<>(right);
        }

        public void setRight(List<String> right) {
            this.right = right == null ? null : new ArrayList<>(right);
        }

        Amenities copy() {
            Amenities copy = new Amenities();
            copy.left = getLeft();
            copy.right = getRight();
            return copy;
        }
    }
    private Amenities amenities;
    private List<String> highlightKeys;

    public Amenities getAmenities() {
        return amenities == null ? null : amenities.copy();
    }

    public void setAmenities(Amenities amenities) {
        this.amenities = amenities == null ? null : amenities.copy();
    }

    public List<String> getHighlightKeys() {
        return highlightKeys == null ? null : new ArrayList<>(highlightKeys);
    }

    public void setHighlightKeys(List<String> highlightKeys) {
        this.highlightKeys = highlightKeys == null ? null : new ArrayList<>(highlightKeys);
    }
}
