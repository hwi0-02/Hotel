package com.example.backend.fe_hotel_detail.service;

import com.example.backend.HotelOwner.domain.Amenity;
import com.example.backend.HotelOwner.domain.Hotel;
import com.example.backend.HotelOwner.domain.HotelImage;
import com.example.backend.HotelOwner.domain.Room;
import com.example.backend.HotelOwner.domain.RoomImage;
import com.example.backend.HotelOwner.repository.HotelAmenityRepository;
import com.example.backend.HotelOwner.repository.HotelImageRepository;
import com.example.backend.HotelOwner.repository.HotelRepository;
import com.example.backend.HotelOwner.repository.RoomRepository;
import com.example.backend.fe_hotel_detail.dto.HotelDetailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("feHotelDetailService")
@RequiredArgsConstructor
public class FeHotelDetailService {

    private final HotelRepository hotelRepository;
    private final HotelImageRepository hotelImageRepository;
    private final RoomRepository roomRepository;
    private final HotelAmenityRepository hotelAmenityRepository;

    public HotelDetailDto getHotelDetail(Long id) {
        Hotel h = hotelRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("hotel not found"));

        // 호텔 이미지
        List<String> hotelImages = hotelImageRepository.findByHotel_IdOrderBySortNoAsc(id)
            .stream().map(HotelImage::getUrl).toList();

        // 객실: LEFT JOIN FETCH로 모두 가져오되(이미지 없어도 포함), 혹시 모를 중복을 id기준 Dedup
        List<Room> fetched = roomRepository.findByHotelIdWithImages(id);
        Map<Long, Room> dedup = new LinkedHashMap<>();
        for (Room r : fetched) {
            // 동일 id면 최초 1건만 유지 (정렬은 쿼리에서 id ASC로 보장)
            dedup.putIfAbsent(r.getId(), r);
        }
        List<Room> roomEntities = new ArrayList<>(dedup.values());

        // 편의시설 → 좌/우 분배
        List<Amenity> amenList = hotelAmenityRepository.findAmenitiesByHotel_Id(id);
        List<String> amenNames = amenList.stream().map(Amenity::getName).toList();
        List<String> left = new ArrayList<>(), right = new ArrayList<>();
        for (int i = 0; i < amenNames.size(); i++) {
            (i % 2 == 0 ? left : right).add(amenNames.get(i));
        }

        // 호텔 DTO
        HotelDetailDto.HotelDto hotelDto = HotelDetailDto.HotelDto.builder()
            .id(h.getId())
            .name(h.getName())
            .address(h.getAddress())
            .description(h.getDescription())
            .images(hotelImages)
            .badges(List.of())
            .rating(new HotelDetailDto.Rating(0.0, Map.of()))
            .amenities(new HotelDetailDto.Amenities(left, right))
            .notice(null)
            .build();

        // 객실 DTO
        List<HotelDetailDto.RoomDto> roomDtos = new ArrayList<>();
        for (Room r : roomEntities) {
            List<String> photos = (r.getImages() == null) ? List.of()
                : r.getImages().stream().map(RoomImage::getUrl).toList();

            roomDtos.add(HotelDetailDto.RoomDto.builder()
                .id(r.getId())
                .name(safeRoomName(r))
                .size(parseIntSafe(r.getRoomSize()))
                .view(nullToDash(r.getViewName()))
                .bed(nullToDash(r.getBed()))
                .bath(r.getBath())
                .smoke(boolSafe(r.getSmoke()))
                .sharedBath(boolSafe(r.getSharedBath()))
                .window(boolSafe(r.getHasWindow()))
                .aircon(boolSafe(r.getAircon()))
                .water(boolSafe(r.getFreeWater()))
                .wifi(boolSafe(r.getWifi()))
                .cancelPolicy(r.getCancelPolicy())
                .payment(r.getPayment())
                .originalPrice(intSafe(r.getOriginalPrice()))
                .price(intSafe(r.getPrice()))
                .lastBookedHours(3) // 데모 값
                .photos(photos)
                .promos(List.of())
                .qty(intSafe(r.getRoomCount()))        // 재고(보유 수량)
                .capacityMin(intSafe(r.getCapacityMin()))
                .capacityMax(intSafe(r.getCapacityMax()))
                .build());
        }

        HotelDetailDto dto = new HotelDetailDto();
        dto.setHotel(hotelDto);
        dto.setRooms(roomDtos);
        return dto;
    }

    // "75㎡", "약 75 m²", "75m2" → 75
    private Integer parseIntSafe(String s) {
        if (s == null) return null;
        String digits = s.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return null;
        try { return Integer.parseInt(digits); }
        catch (NumberFormatException e) { return null; }
    }

    private int intSafe(Integer n) { return n == null ? 0 : n.intValue(); }
    private Boolean boolSafe(Boolean b) { return b != null && b; }
    private String nullToDash(String s) { return (s == null || s.isBlank()) ? "-" : s; }

    private String safeRoomName(Room r) {
        if (r.getName() != null && !r.getName().isBlank()) return r.getName();
        return (r.getRoomType() != null) ? r.getRoomType().name() : "객실";
    }
}
