// src/main/java/com/example/backend/HotelOwner/dto/OwnerHotelUpdateRequest.java
package com.example.backend.HotelOwner.dto;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class OwnerHotelUpdateRequest {
    private String name;
    private String address;
    private Integer starRating;
    private String description;


    private Amenities amenities; // { left:[], right:[] }

    @Getter
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

        private Amenities copy() {
            Amenities copy = new Amenities();
            copy.left = getLeft();
            copy.right = getRight();
            return copy;
        }
    }

    public Amenities getAmenities() {
        return amenities == null ? null : amenities.copy();
    }

    public void setAmenities(Amenities amenities) {
        this.amenities = amenities == null ? null : amenities.copy();
    }
}
