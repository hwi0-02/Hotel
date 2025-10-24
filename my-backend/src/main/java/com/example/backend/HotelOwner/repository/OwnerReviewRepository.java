package com.example.backend.HotelOwner.repository;

import com.example.backend.HotelOwner.domain.Room;
import com.example.backend.review.domain.UserReview;
import com.example.backend.hotel_reservation.domain.Reservation; // Reservation import
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OwnerReviewRepository extends JpaRepository<UserReview, Long>, JpaSpecificationExecutor<UserReview> {

    /**
     * ✅ 최종 수정: Subquery를 사용하여 hotelId 조건을 명확하고 안전하게 처리합니다.
     */
    static Specification<UserReview> hasHotelId(Long hotelId) {
        return (root, query, cb) -> {
            // 1. Subquery를 생성합니다. (결과: Long 타입의 Room ID 목록)
            Subquery<Long> roomSubquery = query.subquery(Long.class);
            Root<Room> roomRoot = roomSubquery.from(Room.class);
            
            // 2. Subquery의 SELECT 절: Room의 ID를 선택합니다.
            // 3. Subquery의 WHERE 절: Room의 hotel.id가 파라미터로 받은 hotelId와 같은 경우
            roomSubquery.select(roomRoot.get("id"))
                .where(cb.equal(roomRoot.get("hotel").get("id"), hotelId));

            // 4. 메인 쿼리: UserReview와 연관된 Reservation의 roomId가 Subquery 결과에 포함되는지(IN) 확인합니다.
            Join<UserReview, Reservation> reservationJoin = root.join("reservation");
            return cb.in(reservationJoin.get("roomId")).value(roomSubquery);
        };
    }

    static Specification<UserReview> hasRoomType(String roomType) {
        return (root, query, cb) -> {
            Subquery<Long> roomSubquery = query.subquery(Long.class);
            Root<Room> roomRoot = roomSubquery.from(Room.class);
            roomSubquery.select(roomRoot.get("id"))
                .where(cb.equal(roomRoot.get("roomType"), roomType));

            Join<UserReview, Reservation> reservationJoin = root.join("reservation");
            return cb.in(reservationJoin.get("roomId")).value(roomSubquery);
        };
    }

    static Specification<UserReview> hasRoomId(Long roomId) {
        return (root, query, cb) -> cb.equal(root.join("reservation").get("roomId"), roomId);
    }

    static Specification<UserReview> hasOwnerId(Long ownerId) {
        return (root, query, cb) -> {
            Subquery<Long> roomSubquery = query.subquery(Long.class);
            Root<Room> roomRoot = roomSubquery.from(Room.class);
            
            // roomRoot.get("hotel").get("ownerId") -> roomRoot.get("hotel").get("owner").get("id")
            roomSubquery.select(roomRoot.get("id"))
                .where(cb.equal(roomRoot.get("hotel").get("owner").get("id"), ownerId));

            Join<UserReview, Reservation> reservationJoin = root.join("reservation");
            return cb.in(reservationJoin.get("roomId")).value(roomSubquery);
        };
    }

    static Specification<UserReview> isReported(boolean isReported) {
        return (root, query, cb) -> cb.equal(root.get("reported"), isReported);
    }

    static Specification<UserReview> isReplied(boolean isReplied) {
        return (root, query, cb) -> {
            if (isReplied) {
                return cb.and(
                    cb.isNotNull(root.get("adminReply")),
                    cb.notEqual(root.get("adminReply"), "")
                );
            } else {
                return cb.or(
                    cb.isNull(root.get("adminReply")),
                    cb.equal(root.get("adminReply"), "")
                );
            }
        };
    }

    static Specification<UserReview> isHidden(boolean isHidden) {
        return (root, query, cb) -> cb.equal(root.get("hidden"), isHidden);
    }
}