package com.example.backend.HotelOwner.controller;

import com.example.backend.HotelOwner.dto.OwnerFilterDataDto;
import com.example.backend.HotelOwner.service.OwnerFilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerFilterController {

    private final OwnerFilterService ownerFilterService;

    // 업주 ID를 기반으로 소유 호텔 및 객실 목록 조회
    @GetMapping("/filter-data")
    public ResponseEntity<OwnerFilterDataDto> getFilterData(@RequestParam Long ownerId) {
        // 현재는 ownerId를 쿼리 파라미터로 받습니다.
        OwnerFilterDataDto filterData = ownerFilterService.getFilterDataByOwnerId(ownerId);
        return ResponseEntity.ok(filterData);
    }
}