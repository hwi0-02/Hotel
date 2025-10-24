// src/main/java/com/example/backend/HotelOwner/controller/OwnerReservationController.java
package com.example.backend.HotelOwner.controller;

import com.example.backend.HotelOwner.dto.OwnerReservationDto;
import com.example.backend.HotelOwner.service.OwnerReservationService;
import com.example.backend.authlogin.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/owner/reservations")
@RequiredArgsConstructor
public class OwnerReservationController {

    private final OwnerReservationService ownerReservationService;
    private final JwtUtil jwtUtil;

    /** 오너 소유 모든 호텔의 예약 캘린더 */
    @GetMapping
    public ResponseEntity<List<OwnerReservationDto.CalendarEvent>> getMyHotelReservations(
            @RequestHeader("Authorization") String auth
    ) {
        Long ownerId = jwtUtil.extractUserId(auth.substring(7));
        return ResponseEntity.ok(ownerReservationService.getReservationsForOwner(ownerId));
    }

    /** 예약 상세 */
    @GetMapping("/{reservationId}")
    public ResponseEntity<OwnerReservationDto.DetailResponse> getReservationDetails(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long reservationId
    ) throws AccessDeniedException {
        Long ownerId = jwtUtil.extractUserId(auth.substring(7));
        return ResponseEntity.ok(ownerReservationService.getReservationDetails(ownerId, reservationId));
    }

    /** 체크인 */
    @PutMapping("/{reservationId}/check-in")
    public ResponseEntity<Void> checkIn(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long reservationId
    ) throws AccessDeniedException {
        Long ownerId = jwtUtil.extractUserId(auth.substring(7));
        ownerReservationService.checkIn(ownerId, reservationId);
        return ResponseEntity.ok().build();
    }

    /** 체크아웃 */
    @PutMapping("/{reservationId}/check-out")
    public ResponseEntity<Void> checkOut(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long reservationId
    ) throws AccessDeniedException {
        Long ownerId = jwtUtil.extractUserId(auth.substring(7));
        ownerReservationService.checkOut(ownerId, reservationId);
        return ResponseEntity.ok().build();
    }

    /** 체크인 취소 */
    @PutMapping("/{reservationId}/cancel-checkin")
    public ResponseEntity<Void> cancelCheckIn(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long reservationId
    ) throws AccessDeniedException {
        Long ownerId = jwtUtil.extractUserId(auth.substring(7));
        ownerReservationService.cancelCheckIn(ownerId, reservationId);
        return ResponseEntity.ok().build();
    }

    /** 체크아웃 취소 */
    @PutMapping("/{reservationId}/cancel-checkout")
    public ResponseEntity<Void> cancelCheckOut(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long reservationId
    ) throws AccessDeniedException {
        Long ownerId = jwtUtil.extractUserId(auth.substring(7));
        ownerReservationService.cancelCheckOut(ownerId, reservationId);
        return ResponseEntity.ok().build();
    }

    /** 예약 취소 */
    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<Void> cancelReservation(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long reservationId
    ) throws AccessDeniedException {
        Long ownerId = jwtUtil.extractUserId(auth.substring(7));
        ownerReservationService.cancelReservation(ownerId, reservationId);
        return ResponseEntity.ok().build();
    }

    /** ✅ 인하우스 게스트(현재 투숙중) 조회
     * 예: GET /api/owner/reservations/inhouse-guests?hotelId=5&roomIds=11&roomIds=13
     */
    @GetMapping("/inhouse-guests")
    public ResponseEntity<List<OwnerReservationDto.InhouseGuest>> getInhouseGuests(
            @RequestHeader("Authorization") String auth,
            @RequestParam Long hotelId,
            @RequestParam(value = "roomIds", required = false) List<Long> roomIds
    ) throws AccessDeniedException {
        Long ownerId = jwtUtil.extractUserId(auth.substring(7));
        return ResponseEntity.ok(ownerReservationService.getInhouseGuests(ownerId, hotelId, roomIds));
    }
}
