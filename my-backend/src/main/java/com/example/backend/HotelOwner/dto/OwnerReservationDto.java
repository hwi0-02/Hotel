// src/main/java/com/example/backend/HotelOwner/dto/OwnerReservationDto.java
package com.example.backend.HotelOwner.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class OwnerReservationDto {

    @Getter
    @Builder
    public static class CalendarEvent {
        private Long id;
        private String title;
        private LocalDate start;
        private LocalDate end;
        private String color;
        private String status;
        private ExtendedProps extendedProps;
    }

    @Getter
    @Builder
    public static class ExtendedProps {
        private String guestName;
        private String roomName;
        private String resStatus;
        private String roomType;
    }

    @Getter
    @Builder
    public static class DetailResponse {
        private Long id;
        private String hotelName;
        private String guestName;
        private String guestPhone;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private String roomType;
        private Integer numAdult;
        private Integer numKid;
        private String reservationStatus;
        private String resStatus;
    }

    /** ✅ 인하우스 게스트 카드용 DTO */
    @Getter
    @Builder
    public static class InhouseGuest {
        private Long reservationId;
        private Long roomId;
        private String roomName;
        private String guestName;
        private String guestPhone;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private String resStatus;
    }

    public static LocalDate toLocalDate(Instant instant) {
        if (instant == null) return null;
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
