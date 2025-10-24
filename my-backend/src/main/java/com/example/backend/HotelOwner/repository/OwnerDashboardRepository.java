package com.example.backend.HotelOwner.repository;

import com.example.backend.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Repository
public interface OwnerDashboardRepository extends JpaRepository<Payment, Long> {

    @Query("""
        SELECT COALESCE(SUM(p.totalPrice), 0)
        FROM Payment p
        JOIN Reservation r ON p.reservationId = r.id
        JOIN Room rm ON r.roomId = rm.id
        JOIN rm.hotel h
        WHERE r.endDate >= :startOfDay
          AND r.endDate < :endOfDay
          AND r.status <> 'CANCELLED'
          AND (:ownerId IS NULL OR h.owner.id = :ownerId)
    """)
    Double findSalesByDate(@Param("startOfDay") Instant startOfDay,
                           @Param("endOfDay") Instant endOfDay,
                           @Param("ownerId") Long ownerId);

    @Query("""
        SELECT COALESCE(SUM(p.totalPrice), 0)
        FROM Payment p
        JOIN Reservation r ON p.reservationId = r.id
        JOIN Room rm ON r.roomId = rm.id
        JOIN rm.hotel h
        WHERE r.endDate BETWEEN :startDate AND :endDate
          AND r.status <> 'CANCELLED'
          AND (:ownerId IS NULL OR h.owner.id = :ownerId)
    """)
    Double findSalesBetweenDates(@Param("startDate") Instant startDate,
                                 @Param("endDate") Instant endDate,
                                 @Param("ownerId") Long ownerId);

    @Query("""
        SELECT p
        FROM Payment p
        JOIN Reservation r ON p.reservationId = r.id
        JOIN Room rm ON r.roomId = rm.id
        JOIN rm.hotel h
        WHERE r.endDate BETWEEN :startDate AND :endDate
          AND r.status <> 'CANCELLED'
          AND (:ownerId IS NULL OR h.owner.id = :ownerId)
    """)
    List<Payment> findPaymentsBetweenDates(@Param("startDate") Instant startDate,
                                           @Param("endDate") Instant endDate,
                                           @Param("ownerId") Long ownerId);

    @Query("""
        SELECT FUNCTION('DATE_FORMAT', r.endDate, '%Y-%m-%d') as date, SUM(p.totalPrice) as dailySales
        FROM Payment p
        JOIN Reservation r ON p.reservationId = r.id
        JOIN Room rm ON r.roomId = rm.id
        JOIN rm.hotel h
        WHERE r.endDate BETWEEN :startDate AND :endDate
          AND r.status <> 'CANCELLED'
          AND (:roomId IS NULL OR r.roomId = :roomId)
          AND (:ownerId IS NULL OR h.owner.id = :ownerId)
        GROUP BY date
        ORDER BY date
    """)
    List<Map<String, Object>> findDailySalesBetweenDates(@Param("startDate") Instant startDate,
                                                         @Param("endDate") Instant endDate,
                                                         @Param("roomId") Long roomId,
                                                         @Param("ownerId") Long ownerId);

    @Query("""
        SELECT FUNCTION('DATE_FORMAT', r.endDate, '%Y-%m') as month, SUM(p.totalPrice) as monthlySales
        FROM Payment p
        JOIN Reservation r ON p.reservationId = r.id
        JOIN Room rm ON r.roomId = rm.id
        JOIN rm.hotel h
        WHERE r.endDate BETWEEN :startDate AND :endDate
          AND r.status <> 'CANCELLED'
          AND (:roomId IS NULL OR r.roomId = :roomId)
          AND (:ownerId IS NULL OR h.owner.id = :ownerId)
        GROUP BY month
        ORDER BY month
    """)
    List<Map<String, Object>> findMonthlySalesBetweenDates(@Param("startDate") Instant startDate,
                                                           @Param("endDate") Instant endDate,
                                                           @Param("roomId") Long roomId,
                                                           @Param("ownerId") Long ownerId);
}
