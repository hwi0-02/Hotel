// src/main/java/com/example/backend/HotelOwner/repository/RoomRepository.java
package com.example.backend.HotelOwner.repository;

import com.example.backend.HotelOwner.domain.Hotel;
import com.example.backend.HotelOwner.domain.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByRoomType(Room.RoomType roomType);

    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.hotel WHERE r.hotel.id = :hotelId")
    List<Room> findByHotelId(@Param("hotelId") Long hotelId);

    List<Room> findByHotel(Hotel hotel);

    @Deprecated
    boolean existsByIdAndHotelOwnerEmail(Long roomId, String ownerEmail);

    boolean existsByIdAndHotel_Owner_Email(Long roomId, String ownerEmail);

    @Query("SELECT DISTINCT r FROM Room r LEFT JOIN FETCH r.images WHERE r.hotel.id = :hotelId")
    List<Room> findByHotelIdWithImages(@Param("hotelId") Long hotelId);

    @Query("SELECT r.hotel.id FROM Room r WHERE r.id = :roomId")
    Long findHotelIdByRoomId(@Param("roomId") Long roomId);

    Page<Room> findByHotel_IdAndNameContaining(Long hotelId, String name, Pageable pageable);
    Page<Room> findByHotel_Id(Long hotelId, Pageable pageable);
    Page<Room> findByNameContaining(String name, Pageable pageable);

    boolean existsByHotel_Id(Long hotelId);

    List<Room> findByHotelIdIn(List<Long> hotelIds);

    @Query(value = """
        SELECT r.hotel_id  AS hotelId,
               MIN(r.price) AS minPrice
        FROM room r
        WHERE r.hotel_id IN (:hotelIds)
          AND r.status = 'ACTIVE'
        GROUP BY r.hotel_id
        """, nativeQuery = true)
    List<MinPriceRow> findMinPriceByHotelIds(@Param("hotelIds") List<Long> hotelIds);

    interface MinPriceRow {
        Long getHotelId();
        Long getMinPrice();
    }

    // ✅ 호출부가 찾는 메서드(존재 여부만 true/false)
    @Query("SELECT (COUNT(r) > 0) FROM Room r WHERE r.hotel.id = :hotelId")
    boolean countAnyByHotelId(@Param("hotelId") Long hotelId);
}
