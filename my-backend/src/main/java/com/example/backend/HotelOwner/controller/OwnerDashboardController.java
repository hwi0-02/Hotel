package com.example.backend.HotelOwner.controller;

import com.example.backend.HotelOwner.dto.OwnerChartDto;
import com.example.backend.HotelOwner.dto.OwnerDashboardDto;
import com.example.backend.HotelOwner.service.OwnerDashboardService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owner/dashboard")
@RequiredArgsConstructor
public class OwnerDashboardController {

    private final OwnerDashboardService ownerDashboardService;
    
    @GetMapping("/sales")
    public ResponseEntity<OwnerDashboardDto> getSalesData() {
        return ResponseEntity.ok(ownerDashboardService.getSalesData());
    }

    @GetMapping("/chart")
    public ResponseEntity<OwnerChartDto> getChartData(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long roomId) {
        return ResponseEntity.ok(ownerDashboardService.getChartData(period, startDate, endDate, roomId));
    }
}