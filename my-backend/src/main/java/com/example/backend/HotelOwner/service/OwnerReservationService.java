// src/main/java/com/example/backend/HotelOwner/service/OwnerReservationService.java
package com.example.backend.HotelOwner.service;

import com.example.backend.HotelOwner.domain.Room;
import com.example.backend.HotelOwner.dto.OwnerReservationDto;
import com.example.backend.HotelOwner.dto.OwnerReservationDto.CalendarEvent;
import com.example.backend.HotelOwner.repository.OwnerPaymentRepository;
import com.example.backend.HotelOwner.repository.OwnerReservationRepository;
import com.example.backend.HotelOwner.repository.OwnerRoomRepository;
import com.example.backend.authlogin.domain.User;
import com.example.backend.authlogin.repository.UserRepository;
import com.example.backend.hotel_reservation.domain.Reservation;
import com.example.backend.hotel_reservation.domain.Reservation.ResStatus;
import com.example.backend.hotel_reservation.domain.Reservation.Status;
import com.example.backend.payment.domain.Payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerReservationService {

    private final OwnerReservationRepository ownerReservationRepository;
    private final UserRepository userRepository;
    private final OwnerRoomRepository ownerRoomRepository;
    private final OwnerPaymentRepository ownerPaymentRepository;

    // ====== (네가 올린 기존 메서드들 그대로) ======

    @Transactional(readOnly = true)
    public List<CalendarEvent> getReservationsForOwner(Long ownerId) {
        List<Reservation> reservations = ownerReservationRepository.findAllByOwnerId(ownerId);

        Map<String, String> roomTypeColorMap = new HashMap<>();
        roomTypeColorMap.put("스위트룸",   "#ef4444");
        roomTypeColorMap.put("디럭스룸",   "#3b82f6");
        roomTypeColorMap.put("스탠다드룸", "#22c55e");
        roomTypeColorMap.put("싱글룸",     "#f97316");
        roomTypeColorMap.put("트윈룸",     "#a855f7");

        return reservations.stream()
            .filter(res -> res.getStatus() == Reservation.Status.COMPLETED
                        || res.getStatus() == Reservation.Status.CANCELLED)
            .map(res -> {
                User user = userRepository.findById(res.getUserId()).orElse(null);
                Room room = ownerRoomRepository.findById(res.getRoomId()).orElse(null);

                String guestName     = (user != null) ? user.getName() : "알 수 없음";
                String roomTypeName  = (room != null && room.getRoomType() != null) ? room.getRoomType().name() : "기타";
                String eventColor    = (res.getStatus() == Reservation.Status.CANCELLED)
                        ? "#9ca3af"
                        : roomTypeColorMap.getOrDefault(roomTypeName, "#848484");

                return OwnerReservationDto.CalendarEvent.builder()
                    .id(res.getId())
                    .title(guestName)
                    .start(OwnerReservationDto.toLocalDate(res.getStartDate()))
                    .end(OwnerReservationDto.toLocalDate(res.getEndDate()).plusDays(1))
                    .color(eventColor)
                    .status(res.getStatus().name())
                    .extendedProps(OwnerReservationDto.ExtendedProps.builder()
                        .guestName(guestName)
                        .roomName((room != null) ? room.getName() : "삭제된 객실")
                        .roomType(roomTypeName)
                        .resStatus(res.getResStatus() != null ? res.getResStatus().name()
                                                              : Reservation.ResStatus.NOT_VISITED.name())
                        .build())
                    .build();
            })
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OwnerReservationDto.DetailResponse getReservationDetails(Long ownerId, Long reservationId)
            throws AccessDeniedException {

        Reservation reservation = ownerReservationRepository.findOneForOwner(ownerId, reservationId)
                .orElseThrow(() -> new AccessDeniedException("해당 예약은 이 오너 소유가 아닙니다."));

        User user = userRepository.findById(reservation.getUserId()).orElseThrow();
        Room room = ownerRoomRepository.findById(reservation.getRoomId()).orElseThrow();

        return OwnerReservationDto.DetailResponse.builder()
            .id(reservation.getId())
            .hotelName(room.getHotel().getName())
            .guestName(user.getName())
            .guestPhone(user.getPhone())
            .checkInDate(OwnerReservationDto.toLocalDate(reservation.getStartDate()))
            .checkOutDate(OwnerReservationDto.toLocalDate(reservation.getEndDate()))
            .roomType(room.getRoomType().name())
            .numAdult(reservation.getNumAdult())
            .numKid(reservation.getNumKid())
            .reservationStatus(reservation.getStatus().name())
            .resStatus(reservation.getResStatus() != null ? reservation.getResStatus().name()
                                                          : Reservation.ResStatus.NOT_VISITED.name())
            .build();
    }

    @Transactional
    public void checkIn(Long ownerId, Long reservationId) throws AccessDeniedException {
        Reservation reservation = ownerReservationRepository.findOneForOwner(ownerId, reservationId)
                .orElseThrow(() -> new AccessDeniedException("권한이 없습니다."));

        LocalDate today     = LocalDate.now();
        LocalDate startDate = OwnerReservationDto.toLocalDate(reservation.getStartDate());
        LocalDate endDate   = OwnerReservationDto.toLocalDate(reservation.getEndDate());

        if (today.isBefore(startDate) || !today.isBefore(endDate)) {
            throw new IllegalStateException("체크인 가능한 날짜가 아닙니다.");
        }
        reservation.setResStatus(ResStatus.CHECKED_IN);
    }

    @Transactional
    public void checkOut(Long ownerId, Long reservationId) throws AccessDeniedException {
        Reservation reservation = ownerReservationRepository.findOneForOwner(ownerId, reservationId)
                .orElseThrow(() -> new AccessDeniedException("권한이 없습니다."));

        if (!OwnerReservationDto.toLocalDate(reservation.getEndDate()).equals(LocalDate.now())) {
            throw new IllegalStateException("체크아웃 날짜가 아닙니다.");
        }
        reservation.setResStatus(ResStatus.CHECKED_OUT);
    }

    @Transactional
    public void cancelCheckIn(Long ownerId, Long reservationId) throws AccessDeniedException {
        Reservation reservation = ownerReservationRepository.findOneForOwner(ownerId, reservationId)
                .orElseThrow(() -> new AccessDeniedException("권한이 없습니다."));
        reservation.setResStatus(ResStatus.NOT_VISITED);
    }

    @Transactional
    public void cancelCheckOut(Long ownerId, Long reservationId) throws AccessDeniedException {
        Reservation reservation = ownerReservationRepository.findOneForOwner(ownerId, reservationId)
                .orElseThrow(() -> new AccessDeniedException("권한이 없습니다."));
        reservation.setResStatus(ResStatus.CHECKED_IN);
    }

    @Transactional
    public void cancelReservation(Long ownerId, Long reservationId) throws AccessDeniedException {
        Reservation reservation = ownerReservationRepository.findOneForOwner(ownerId, reservationId)
                .orElseThrow(() -> new AccessDeniedException("권한이 없습니다."));

        if (OwnerReservationDto.toLocalDate(reservation.getStartDate()).isBefore(LocalDate.now())) {
            throw new IllegalStateException("이미 지난 예약은 취소할 수 없습니다.");
        }
        reservation.setStatus(Status.CANCELLED);
        reservation.setResStatus(ResStatus.NOT_VISITED);

        ownerPaymentRepository.findByReservationId(reservationId)
            .ifPresent(p -> {
                if (p.getStatus() == Payment.Status.COMPLETED || p.getStatus() == Payment.Status.PENDING) {
                    p.setStatus(Payment.Status.CANCELLED);
                    p.setCanceledAt(LocalDateTime.now());
                }
            });
    }

    /** ✅ 인하우스 게스트 조회(현재 투숙중) */
    @Transactional(readOnly = true)
    public List<OwnerReservationDto.InhouseGuest> getInhouseGuests(Long ownerId, Long hotelId, List<Long> roomIds) {
        // 오너 소유 예약 전체 조회
        List<Reservation> all = ownerReservationRepository.findAllByOwnerId(ownerId);

        // 필요한 룸/유저를 한 번에 가져와서 N+1 방지
        Set<Long> roomIdSet = all.stream().map(Reservation::getRoomId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Room> roomMap = ownerRoomRepository.findAllById(roomIdSet)
                .stream().collect(Collectors.toMap(Room::getId, Function.identity()));

        Set<Long> userIds = all.stream().map(Reservation::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(userIds)
                .stream().collect(Collectors.toMap(User::getId, Function.identity()));

        Set<Long> filterRoomIds = (roomIds == null || roomIds.isEmpty()) ? null : new HashSet<>(roomIds);

        return all.stream()
                .filter(res -> res.getResStatus() == ResStatus.CHECKED_IN) // 현재 투숙중
                .filter(res -> {
                    Room room = roomMap.get(res.getRoomId());
                    return room != null
                        && room.getHotel() != null
                        && Objects.equals(room.getHotel().getId(), hotelId); // 지정 호텔
                })
                .filter(res -> filterRoomIds == null || filterRoomIds.contains(res.getRoomId()))
                .map(res -> {
                    Room room = roomMap.get(res.getRoomId());
                    User u = userMap.get(res.getUserId());
                    return OwnerReservationDto.InhouseGuest.builder()
                            .reservationId(res.getId())
                            .roomId(res.getRoomId())
                            .roomName(room != null ? room.getName() : "삭제된 객실")
                            .guestName(u != null ? u.getName() : "알 수 없음")
                            .guestPhone(u != null ? u.getPhone() : null)
                            .checkInDate(OwnerReservationDto.toLocalDate(res.getStartDate()))
                            .checkOutDate(OwnerReservationDto.toLocalDate(res.getEndDate()))
                            .resStatus(res.getResStatus() != null ? res.getResStatus().name() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
