package com.example.backend.HotelOwner.service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.backend.HotelOwner.dto.OwnerChartDto;
import com.example.backend.HotelOwner.dto.OwnerDashboardDto;
import com.example.backend.HotelOwner.repository.OwnerDashboardRepository;
import com.example.backend.authlogin.domain.User;
import com.example.backend.authlogin.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OwnerDashboardService {

    private final OwnerDashboardRepository ownerDashboardRepository;
    private final UserRepository userRepository;

    public OwnerDashboardDto getSalesData() {
        OwnerDashboardDto dto = new OwnerDashboardDto();
        LocalDate today = LocalDate.now();
        ZoneId zoneId = ZoneId.systemDefault(); // 시스템 기본 시간대 사용

        Long ownerId = resolveCurrentOwnerId();
        if (ownerId == null) {
            // 권한/세션 없으면 0으로 반환
            dto.setTodaySales(0);
            dto.setWeeklySales(0);
            dto.setMonthlySales(0);
            dto.setTodaySalesChange(0.0);
            dto.setWeeklySalesChange(0.0);
            dto.setMonthlySalesChange(0.0);
            return dto;
        }

        // --- Instant 변환 로직 추가 ---
        Instant todayStart = today.atStartOfDay(zoneId).toInstant();
        Instant todayEnd = today.plusDays(1).atStartOfDay(zoneId).toInstant();
        double todaySales = ownerDashboardRepository.findSalesByDate(todayStart, todayEnd, ownerId);
        dto.setTodaySales(todaySales);

        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        double weeklySales = ownerDashboardRepository.findSalesBetweenDates(
                startOfWeek.atStartOfDay(zoneId).toInstant(),
                endOfWeek.plusDays(1).atStartOfDay(zoneId).toInstant(),
                ownerId);
        dto.setWeeklySales(weeklySales);

        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        double monthlySales = ownerDashboardRepository.findSalesBetweenDates(
                startOfMonth.atStartOfDay(zoneId).toInstant(),
                endOfMonth.plusDays(1).atStartOfDay(zoneId).toInstant(),
                ownerId);
        dto.setMonthlySales(monthlySales);

        // --- 이전 기간 매출 계산 ---
        LocalDate yesterday = today.minusDays(1);
        Instant yesterdayStart = yesterday.atStartOfDay(zoneId).toInstant();
        Instant yesterdayEnd = today.atStartOfDay(zoneId).toInstant();
        double yesterdaySales = ownerDashboardRepository.findSalesByDate(yesterdayStart, yesterdayEnd, ownerId);

        LocalDate startOfLastWeek = startOfWeek.minusWeeks(1);
        LocalDate endOfLastWeek = endOfWeek.minusWeeks(1);
        double lastWeekSales = ownerDashboardRepository.findSalesBetweenDates(
                startOfLastWeek.atStartOfDay(zoneId).toInstant(),
                endOfLastWeek.plusDays(1).atStartOfDay(zoneId).toInstant(),
                ownerId);

        LocalDate startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDate endOfLastMonth = startOfLastMonth.with(TemporalAdjusters.lastDayOfMonth());
        double lastMonthSales = ownerDashboardRepository.findSalesBetweenDates(
                startOfLastMonth.atStartOfDay(zoneId).toInstant(),
                endOfLastMonth.plusDays(1).atStartOfDay(zoneId).toInstant(),
                ownerId);


        // --- 증감률 계산 및 DTO에 설정 ---
        dto.setTodaySalesChange(calculatePercentageChange(todaySales, yesterdaySales));
        dto.setWeeklySalesChange(calculatePercentageChange(weeklySales, lastWeekSales));
        dto.setMonthlySalesChange(calculatePercentageChange(monthlySales, lastMonthSales));

        return dto;
    }

    private Double calculatePercentageChange(double current, double previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0; // 이전 매출이 0일 경우, 현재 매출이 있으면 100% 증가로 처리
        }
        return ((current - previous) / previous) * 100.0;
    }

    public OwnerChartDto getChartData(String period, LocalDate startDate, LocalDate endDate, Long roomId) {
        Long ownerId = resolveCurrentOwnerId();
        if (ownerId == null) {
            return new OwnerChartDto(Collections.emptyList(), Collections.emptyList());
        }
        if (period != null) {
            endDate = LocalDate.now();
            switch (period) {
                case "7d":
                    startDate = endDate.minusDays(6);
                    break;
                case "30d":
                    startDate = endDate.minusDays(29);
                    break;
                case "1y":
                    startDate = endDate.minusYears(1).withDayOfMonth(1);
                    return getMonthlyChartData(startDate, endDate, roomId, ownerId);
                case "5y":
                    startDate = endDate.minusYears(5).withDayOfMonth(1);
                    return getMonthlyChartData(startDate, endDate, roomId, ownerId);
                case "10y":
                    startDate = endDate.minusYears(10).withDayOfMonth(1);
                    return getMonthlyChartData(startDate, endDate, roomId, ownerId);
                default:
                    startDate = LocalDate.now().minusDays(6); // 기본값
            }
        }
        return getDailyChartData(startDate, endDate, roomId, ownerId);
    }

    private OwnerChartDto getDailyChartData(LocalDate startDate, LocalDate endDate, Long roomId, Long ownerId) {
        ZoneId zoneId = ZoneId.systemDefault();
        Instant startInstant = startDate.atStartOfDay(zoneId).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(zoneId).toInstant();

        List<Map<String, Object>> salesData = ownerDashboardRepository.findDailySalesBetweenDates(startInstant, endInstant, roomId, ownerId);
        Map<String, Double> salesMap = salesData.stream()
                .collect(Collectors.toMap(
                        s -> (String) s.get("date"),
                        s -> ((Number) s.get("dailySales")).doubleValue()
                ));

        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            String formattedDate = date.format(formatter);
            labels.add(formattedDate);
            data.add(salesMap.getOrDefault(formattedDate, 0.0));
        }

        return new OwnerChartDto(labels, data);
    }

    private OwnerChartDto getMonthlyChartData(LocalDate startDate, LocalDate endDate, Long roomId, Long ownerId) {
        ZoneId zoneId = ZoneId.systemDefault();
        Instant startInstant = startDate.atStartOfDay(zoneId).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(zoneId).toInstant();

        List<Map<String, Object>> salesData = ownerDashboardRepository.findMonthlySalesBetweenDates(startInstant, endInstant, roomId, ownerId);
        Map<String, Double> salesMap = salesData.stream()
                .collect(Collectors.toMap(
                        s -> (String) s.get("month"),
                        s -> ((Number) s.get("monthlySales")).doubleValue()
                ));

        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        YearMonth startMonth = YearMonth.from(startDate);
        YearMonth endMonth = YearMonth.from(endDate);

        for (YearMonth month = startMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
            String formattedMonth = month.format(formatter);
            labels.add(formattedMonth);
            data.add(salesMap.getOrDefault(formattedMonth, 0.0));
        }
        return new OwnerChartDto(labels, data);
    }

    private Long resolveCurrentOwnerId() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) return null;
            String email = null;
            Object principal = auth.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.User u) {
                email = u.getUsername();
            } else if (principal instanceof String s) {
                email = s;
            }
            if (email == null || email.isBlank()) return null;
            return userRepository.findByEmail(email).map(User::getId).orElse(null);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
