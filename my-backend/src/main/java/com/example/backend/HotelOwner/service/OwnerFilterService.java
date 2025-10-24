package com.example.backend.HotelOwner.service;

import com.example.backend.HotelOwner.domain.Hotel;
import com.example.backend.HotelOwner.domain.Room;
import com.example.backend.HotelOwner.dto.OwnerFilterDataDto;
import com.example.backend.HotelOwner.repository.HotelRepository;
import com.example.backend.HotelOwner.repository.RoomRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerFilterService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    public OwnerFilterDataDto getFilterDataByOwnerId(Long ownerId) {
        // 1. 업주가 소유한 모든 호텔을 조회합니다.
        List<Hotel> hotels = hotelRepository.findByOwnerId(ownerId);
        if (hotels.isEmpty()) {
            return new OwnerFilterDataDto(Collections.emptyList());
        }

        // 2. 조회된 호텔들의 ID 목록을 추출합니다.
        List<Long> hotelIds = hotels.stream().map(Hotel::getId).collect(Collectors.toList());

        // 3. 호텔 ID 목록을 사용해 관련된 모든 객실을 단 한 번의 쿼리로 조회합니다.
        List<Room> allRooms = roomRepository.findByHotelIdIn(hotelIds);

        // 4. 빠른 조회를 위해 호텔 ID를 Key로, 객실 목록을 Value로 갖는 Map을 생성합니다.
        Map<Long, List<Room>> roomsByHotelIdMap = allRooms.stream()
                .collect(Collectors.groupingBy(room -> room.getHotel().getId()));

        // 5. 호텔 목록을 순회하며 DTO를 생성합니다.
        List<OwnerFilterDataDto.HotelDto> hotelDtos = hotels.stream()
                .map(hotel -> {
                    // Map에서 현재 호텔 ID에 해당하는 객실 목록을 가져옵니다.
                    List<Room> currentHotelRooms = roomsByHotelIdMap.getOrDefault(hotel.getId(), Collections.emptyList());
                    
                    // 호텔에 속한 모든 객실에서 roomType을 추출한 후 중복을 제거합니다.
                    List<String> roomTypes = currentHotelRooms.stream()
                    .map(room -> room.getRoomType().name()) // .name() 추가
                    .distinct() // 중복 제거
                    .sorted()   // 가나다순 정렬
                    .collect(Collectors.toList());
            
            return new OwnerFilterDataDto.HotelDto(hotel.getId(), hotel.getName(), roomTypes);
        })
        .collect(Collectors.toList());

        return new OwnerFilterDataDto(hotelDtos);
    }
}