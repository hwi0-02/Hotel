package com.example.backend.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class PaymentAnalyticsDto {
    private List<TimeBucket> byPeriod;
    private List<CategoryBucket> byHotel;
    private List<CategoryBucket> byMethod;

    public List<TimeBucket> getByPeriod() {
        return byPeriod == null ? null : new ArrayList<>(byPeriod);
    }

    public void setByPeriod(List<TimeBucket> byPeriod) {
        this.byPeriod = byPeriod == null ? null : new ArrayList<>(byPeriod);
    }

    public List<CategoryBucket> getByHotel() {
        return byHotel == null ? null : new ArrayList<>(byHotel);
    }

    public void setByHotel(List<CategoryBucket> byHotel) {
        this.byHotel = byHotel == null ? null : new ArrayList<>(byHotel);
    }

    public List<CategoryBucket> getByMethod() {
        return byMethod == null ? null : new ArrayList<>(byMethod);
    }

    public void setByMethod(List<CategoryBucket> byMethod) {
        this.byMethod = byMethod == null ? null : new ArrayList<>(byMethod);
    }

    @Getter
    @Builder
    public static class TimeBucket {
        private String period;
        private long amount;
        private long count;
    }

    @Getter
    @Builder
    public static class CategoryBucket {
        private String label;
        private long amount;
        private long count;
    }
}

