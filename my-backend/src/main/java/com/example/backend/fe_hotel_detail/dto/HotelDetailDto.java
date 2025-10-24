package com.example.backend.fe_hotel_detail.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HotelDetailDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Rating {
        private double score;
        private Map<String, Double> subs;

        public Map<String, Double> getSubs() {
            return subs == null ? null : Map.copyOf(subs);
        }

        public void setSubs(Map<String, Double> subs) {
            this.subs = subs == null ? null : Map.copyOf(subs);
        }

        Rating copy() {
            return Rating.builder()
                    .score(score)
                    .subs(getSubs())
                    .build();
        }
    }

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

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class HotelDto {
        private Long id;
        private String name;
        private String address;
        private String description;

        // 좌표/하이라이트 미사용 → 제거
        private List<String> images;
        private List<String> badges;
        private Rating rating;
        private Amenities amenities;
        private String notice;

        public List<String> getImages() {
            return images == null ? null : new ArrayList<>(images);
        }

        public void setImages(List<String> images) {
            this.images = images == null ? null : new ArrayList<>(images);
        }

        public List<String> getBadges() {
            return badges == null ? null : new ArrayList<>(badges);
        }

        public void setBadges(List<String> badges) {
            this.badges = badges == null ? null : new ArrayList<>(badges);
        }

        public Rating getRating() {
            return rating == null ? null : rating.copy();
        }

        public void setRating(Rating rating) {
            this.rating = rating == null ? null : rating.copy();
        }

        public Amenities getAmenities() {
            return amenities == null ? null : amenities.copy();
        }

        public void setAmenities(Amenities amenities) {
            this.amenities = amenities == null ? null : amenities.copy();
        }
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RoomDto {
        private Long id;
        private String name;
        private Integer size;          // m² 단위의 숫자만 (예: "75㎡" → 75)
        private String view;
        private String bed;
        private Integer bath;
        private Boolean smoke;
        private Boolean sharedBath;
        private Boolean window;
        private Boolean aircon;
        private Boolean water;
        private Boolean wifi;
        private String cancelPolicy;
        private String payment;
        private Integer originalPrice;
        private Integer price;
        private Integer lastBookedHours;  // 최근 예약 시점(시간) 데모용
        private List<String> photos;
        private List<Map<String, String>> promos;
        private Integer qty;              // 재고 수량
        private Integer capacityMin;
        private Integer capacityMax;

        public List<String> getPhotos() {
            return photos == null ? null : new ArrayList<>(photos);
        }

        public void setPhotos(List<String> photos) {
            this.photos = photos == null ? null : new ArrayList<>(photos);
        }

        public List<Map<String, String>> getPromos() {
            return promos == null ? null : new ArrayList<>(promos);
        }

        public void setPromos(List<Map<String, String>> promos) {
            this.promos = promos == null ? null : new ArrayList<>(promos);
        }
    }

    private HotelDto hotel;
    private List<RoomDto> rooms;

    public HotelDto getHotel() {
        return copyHotel(hotel);
    }

    public void setHotel(HotelDto hotel) {
        this.hotel = copyHotel(hotel);
    }

    public List<RoomDto> getRooms() {
        return rooms == null ? null : new ArrayList<>(rooms);
    }

    public void setRooms(List<RoomDto> rooms) {
        this.rooms = rooms == null ? null : new ArrayList<>(rooms);
    }

    private static HotelDto copyHotel(HotelDto source) {
        if (source == null) {
            return null;
        }
        return HotelDto.builder()
                .id(source.getId())
                .name(source.getName())
                .address(source.getAddress())
                .description(source.getDescription())
                .images(source.getImages())
                .badges(source.getBadges())
                .rating(source.getRating())
                .amenities(source.getAmenities())
                .notice(source.getNotice())
                .build();
    }
}
