package com.example.backend.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class DashboardDetailDto {
    private List<DailyPoint> dailyRevenue;
    private List<DailyPoint> dailySignups;
    private List<MonthlyPoint> monthlySignups;
    private List<HotelRevenuePoint> topHotels;

    public List<DailyPoint> getDailyRevenue() {
        return dailyRevenue == null ? null : new ArrayList<>(dailyRevenue);
    }

    public void setDailyRevenue(List<DailyPoint> dailyRevenue) {
        this.dailyRevenue = dailyRevenue == null ? null : new ArrayList<>(dailyRevenue);
    }

    public List<DailyPoint> getDailySignups() {
        return dailySignups == null ? null : new ArrayList<>(dailySignups);
    }

    public void setDailySignups(List<DailyPoint> dailySignups) {
        this.dailySignups = dailySignups == null ? null : new ArrayList<>(dailySignups);
    }

    public List<MonthlyPoint> getMonthlySignups() {
        return monthlySignups == null ? null : new ArrayList<>(monthlySignups);
    }

    public void setMonthlySignups(List<MonthlyPoint> monthlySignups) {
        this.monthlySignups = monthlySignups == null ? null : new ArrayList<>(monthlySignups);
    }

    public List<HotelRevenuePoint> getTopHotels() {
        return topHotels == null ? null : new ArrayList<>(topHotels);
    }

    public void setTopHotels(List<HotelRevenuePoint> topHotels) {
        this.topHotels = topHotels == null ? null : new ArrayList<>(topHotels);
    }

    @Getter
    @Builder
    public static class DailyPoint {
        private String date;
        private Long value;
    }

    @Getter
    @Builder
    public static class MonthlyPoint {
        private Integer month;
        private Integer count;
    }

    @Getter
    @Builder
    public static class HotelRevenuePoint {
        private Long hotelId;
        private String hotelName;
        private Long revenue;
        private Integer reservationCount;
        private Integer roomCount;
        private Double averageRating;
    }
}