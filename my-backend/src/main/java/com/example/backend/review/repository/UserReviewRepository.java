package com.example.backend.review.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.review.domain.UserReview;

public interface UserReviewRepository extends JpaRepository<UserReview, Long> {

    // ✅ 특정 유저의 리뷰
    List<UserReview> findByReservation_UserId(Long userId);

    // ✅ 특정 호텔의 리뷰 (Reservation에 room 필드가 없는 경우)
    @Query("""
        SELECT r FROM UserReview r
        JOIN r.reservation res
        WHERE res.roomId IN (
            SELECT rm.id FROM Room rm
            WHERE rm.hotel.id = :hotelId
        )
    """)
    List<UserReview> findByHotelId(@Param("hotelId") Long hotelId);

    // ✅ 예약 + 사용자 기준 중복 리뷰 방지
    boolean existsByReservationIdAndReservation_UserId(Long reservationId, Long userId);
}
