package com.example.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementReportDto {
    private LocalDate from;
    private LocalDate to;
    private List<SettlementLine> lines;
    private long totalGross;
    private long totalVat;
    private long totalPlatformFee;
    private long totalNetPayout;

    public List<SettlementLine> getLines() {
        return lines == null ? null : new ArrayList<>(lines);
    }

    public void setLines(List<SettlementLine> lines) {
        this.lines = lines == null ? null : new ArrayList<>(lines);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettlementLine {
        private Long hotelId;
        private String hotelName;
        private long grossAmount;
        private long vat;
        private long platformFee;
        private long netPayout;
        private long reservationCount;
    }
}
