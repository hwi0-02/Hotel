package com.example.backend.HotelOwner.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OwnerHotelDto {
    private Long id;
    private String name;
    private String address;
    private int starRating;
    private String approvalStatus;

    // 추가: 신청 현황 상세를 위해 노출
    private String businessName;
    private Long businessId;
    private LocalDateTime createdAt;
    private LocalDateTime approvalDate;
    private String rejectionReason;
}