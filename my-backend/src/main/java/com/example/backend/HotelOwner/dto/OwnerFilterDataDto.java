package com.example.backend.HotelOwner.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OwnerFilterDataDto {
    private List<HotelDto> hotels;

    public List<HotelDto> getHotels() {
        return hotels == null ? null : new ArrayList<>(hotels);
    }

    public void setHotels(List<HotelDto> hotels) {
        this.hotels = hotels == null ? null : new ArrayList<>(hotels);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotelDto {
        private Long hotelId;
        private String hotelName;
        private List<String> roomTypes;

        public List<String> getRoomTypes() {
            return roomTypes == null ? null : new ArrayList<>(roomTypes);
        }

        public void setRoomTypes(List<String> roomTypes) {
            this.roomTypes = roomTypes == null ? null : new ArrayList<>(roomTypes);
        }
    }
}