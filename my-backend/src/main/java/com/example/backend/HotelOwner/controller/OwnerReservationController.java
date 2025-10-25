// src/main/java/com/example/backend/HotelOwner/controller/OwnerReservationController.java
package com.example.backend.HotelOwner.controller;

import com.example.backend.HotelOwner.dto.OwnerReservationDto;
import com.example.backend.HotelOwner.service.OwnerReservationService;
import com.example.backend.admin.repository.AdminUserRepository;
import com.example.backend.authlogin.config.JwtUtil;
import com.example.backend.authlogin.domain.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/owner/reservations")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "https://hwiyeong.shop"}, allowCredentials = "true")
public class OwnerReservationController {

    private final OwnerReservationService ownerReservationService;
    private final JwtUtil jwtUtil;
    private final AdminUserRepository adminUserRepository;

    /** 오너 소유 모든 호텔의 예약 캘린더 */
    @GetMapping
    public ResponseEntity<List<OwnerReservationDto.CalendarEvent>> getMyHotelReservations(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
            HttpServletRequest request
    ) {
        Long ownerId = requireOwnerId(auth, request);
        return ResponseEntity.ok(ownerReservationService.getReservationsForOwner(ownerId));
    }

    /** 예약 상세 */
    @GetMapping("/{reservationId}")
    public ResponseEntity<OwnerReservationDto.DetailResponse> getReservationDetails(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
            @PathVariable Long reservationId,
            HttpServletRequest request
    ) throws AccessDeniedException {
        Long ownerId = requireOwnerId(auth, request);
        return ResponseEntity.ok(ownerReservationService.getReservationDetails(ownerId, reservationId));
    }

    /** 체크인 */
    @PutMapping("/{reservationId}/check-in")
    public ResponseEntity<Void> checkIn(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
            @PathVariable Long reservationId,
            HttpServletRequest request
    ) throws AccessDeniedException {
        Long ownerId = requireOwnerId(auth, request);
        ownerReservationService.checkIn(ownerId, reservationId);
        return ResponseEntity.ok().build();
    }

    /** 체크아웃 */
    @PutMapping("/{reservationId}/check-out")
    public ResponseEntity<Void> checkOut(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
            @PathVariable Long reservationId,
            HttpServletRequest request
    ) throws AccessDeniedException {
        Long ownerId = requireOwnerId(auth, request);
        ownerReservationService.checkOut(ownerId, reservationId);
        return ResponseEntity.ok().build();
    }

    /** 체크인 취소 */
    @PutMapping("/{reservationId}/cancel-checkin")
    public ResponseEntity<Void> cancelCheckIn(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
            @PathVariable Long reservationId,
            HttpServletRequest request
    ) throws AccessDeniedException {
        Long ownerId = requireOwnerId(auth, request);
        ownerReservationService.cancelCheckIn(ownerId, reservationId);
        return ResponseEntity.ok().build();
    }

    /** 체크아웃 취소 */
    @PutMapping("/{reservationId}/cancel-checkout")
    public ResponseEntity<Void> cancelCheckOut(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
            @PathVariable Long reservationId,
            HttpServletRequest request
    ) throws AccessDeniedException {
        Long ownerId = requireOwnerId(auth, request);
        ownerReservationService.cancelCheckOut(ownerId, reservationId);
        return ResponseEntity.ok().build();
    }

    /** 예약 취소 */
    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<Void> cancelReservation(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
            @PathVariable Long reservationId,
            HttpServletRequest request
    ) throws AccessDeniedException {
        Long ownerId = requireOwnerId(auth, request);
        ownerReservationService.cancelReservation(ownerId, reservationId);
        return ResponseEntity.ok().build();
    }

    /** ✅ 인하우스 게스트(현재 투숙중) 조회
     * 예: GET /api/owner/reservations/inhouse-guests?hotelId=5&roomIds=11&roomIds=13
     */
    @GetMapping("/inhouse-guests")
    public ResponseEntity<List<OwnerReservationDto.InhouseGuest>> getInhouseGuests(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
            @RequestParam Long hotelId,
            @RequestParam(value = "roomIds", required = false) List<Long> roomIds,
            HttpServletRequest request
    ) throws AccessDeniedException {
        Long ownerId = requireOwnerId(auth, request);
        return ResponseEntity.ok(ownerReservationService.getInhouseGuests(ownerId, hotelId, roomIds));
    }

    private Long requireOwnerId(String authHeader, HttpServletRequest request) {
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("token".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }

        if (token != null) {
            try {
                return jwtUtil.extractUserId(token);
            } catch (Exception ignored) {
                // fall through to security context resolution below
            }
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            String email = principal instanceof String ? (String) principal : authentication.getName();

            if (email != null && !"anonymousUser".equals(email)) {
                return adminUserRepository.findByEmail(email)
                        .map(User::getId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자 정보를 찾을 수 없습니다."));
            }
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 토큰이 필요합니다.");
    }
}
