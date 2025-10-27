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
import com.example.backend.review.service.UserReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;

@Service("feHotelDetailService")
@RequiredArgsConstructor
public class FeHotelDetailService {

    private static final String PUBLIC_UPLOAD_BASE = "https://hwiyeong.shop";
    private static final Set<String> LEGACY_MEDIA_HOSTS = Set.of("localhost", "127.0.0.1", "images.example.com");

    private final HotelRepository hotelRepository;
    private final HotelImageRepository hotelImageRepository;
    private final RoomRepository roomRepository;
    private final HotelAmenityRepository hotelAmenityRepository;
    private final UserReviewService userReviewService;

    public HotelDetailDto getHotelDetail(Long id) {
        Hotel h = hotelRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("hotel not found"));

        // 호텔 이미지
        List<String> hotelImages = hotelImageRepository.findByHotel_IdOrderBySortNoAsc(id)
            .stream()
            .map(HotelImage::getUrl)
            .map(this::normalizeMediaUrl)
            .filter(Objects::nonNull)
            .toList();

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
            .rating(buildRating(h.getId()))
            .amenities(new HotelDetailDto.Amenities(left, right))
            .notice(null)
            .build();

        // 객실 DTO
        List<HotelDetailDto.RoomDto> roomDtos = new ArrayList<>();
        for (Room r : roomEntities) {
            List<String> photos = (r.getImages() == null) ? List.of()
                : r.getImages().stream()
                    .map(RoomImage::getUrl)
                    .map(this::normalizeMediaUrl)
                    .filter(Objects::nonNull)
                    .toList();

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

    private String normalizeMediaUrl(String raw) {
        if (raw == null) {
            return null;
        }

        String value = raw.trim();
        if (value.isEmpty()) {
            return null;
        }

        try {
            URI uri = URI.create(value);
            String host = uri.getHost();
            String path = uri.getPath();

            if (host == null) {
                return resolveRelative(path != null ? path : value);
            }

            String normalizedHost = host.toLowerCase(Locale.ROOT);
            if (normalizedHost.equals("hwiyeong.shop")) {
                if (path != null && path.startsWith("/uploads")) {
                    return composePublicUrl(path);
                }
                if ("http".equalsIgnoreCase(uri.getScheme())) {
                    return value.replaceFirst("^http://", "https://");
                }
                return value;
            }

            if (LEGACY_MEDIA_HOSTS.contains(normalizedHost)) {
                return composePublicUrl(path);
            }

            return value;
        } catch (IllegalArgumentException ex) {
            return resolveRelative(value);
        }
    }

    private String resolveRelative(String path) {
        if (path == null || path.isBlank()) {
            return PUBLIC_UPLOAD_BASE + "/uploads";
        }
        if (path.startsWith("/uploads") || path.startsWith("uploads")) {
            return PUBLIC_UPLOAD_BASE + ensureUploadsPrefix(path);
        }
        String normalised = path.startsWith("/") ? path : "/" + path;
        return PUBLIC_UPLOAD_BASE + normalised;
    }

    private String composePublicUrl(String path) {
        if (path == null || path.isBlank()) {
            return PUBLIC_UPLOAD_BASE + "/uploads";
        }
        return PUBLIC_UPLOAD_BASE + ensureUploadsPrefix(path);
    }

    private String ensureUploadsPrefix(String path) {
        String normalized = path.startsWith("/") ? path : "/" + path;

        if (normalized.equals("/uploads") || normalized.startsWith("/uploads/")) {
            return normalized;
        }

        if (normalized.startsWith("/uploads")) {
            String remainder = normalized.substring("/uploads".length());
            if (remainder.startsWith("/")) {
                return "/uploads" + remainder;
            }
            return "/uploads/" + remainder;
        }

        return "/uploads" + normalized;
    }

    private HotelDetailDto.Rating buildRating(Long hotelId) {
        if (hotelId == null) {
            return new HotelDetailDto.Rating(0.0, Map.of("리뷰수", 0.0), Map.of());
        }

        Map<String, Object> stats = userReviewService.getHotelReviewStats(hotelId);
        if (stats == null || stats.isEmpty()) {
            return new HotelDetailDto.Rating(0.0, Map.of("리뷰수", 0.0), Map.of());
        }

        double avg = toDouble(stats.get("average"));
        double count = toDouble(stats.get("count"));
        Map<String, Double> details = toDoubleMap(stats.get("details"));

        Map<String, Double> subs = Map.of("리뷰수", count);
        return HotelDetailDto.Rating.builder()
            .score(avg)
            .subs(subs)
            .details(details)
            .build();
    }

    private double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException ignored) {
                return 0.0;
            }
        }
        return 0.0;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Double> toDoubleMap(Object raw) {
        if (raw instanceof Map<?, ?> map) {
            Map<String, Double> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object keyObj = entry.getKey();
                if (keyObj == null) {
                    continue;
                }
                String key = String.valueOf(keyObj);
                double value = toDouble(entry.getValue());
                result.put(key, value);
            }
            return result;
        }
        return Map.of();
    }
}
