// src/main/java/com/example/backend/HotelOwner/dto/OwnerSalesGraphDataDto.java
package com.example.backend.HotelOwner.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class OwnerSalesGraphDataDto {
    private List<DataPoint> dataPoints;

    public List<DataPoint> getDataPoints() {
        return dataPoints == null ? null : new ArrayList<>(dataPoints);
    }

    public void setDataPoints(List<DataPoint> dataPoints) {
        this.dataPoints = dataPoints == null ? null : new ArrayList<>(dataPoints);
    }

    @Getter
    @Builder
    public static class DataPoint {
        private String label; // 날짜(yyyy-MM-dd) / 월(yyyy-MM) / 년(yyyy)
        private Long value;   // 매출액
    }
}
