package com.example.backend.HotelOwner.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OwnerDashboardDto {
    private double todaySales;
    private double weeklySales;
    private double monthlySales;

    private Double todaySalesChange;    // 전일 대비 증감률
    private Double weeklySalesChange;   // 전주 대비 증감률
    private Double monthlySalesChange;  // 전월 대비 증감률
}