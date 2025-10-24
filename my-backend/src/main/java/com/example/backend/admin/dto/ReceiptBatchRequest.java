package com.example.backend.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotEmpty;
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
public class ReceiptBatchRequest {
    private List<Long> paymentIds;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate from;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate to;

    private Long hotelId;

    private boolean includeCancelled;

    @NotEmpty(message = "다운로드할 형식을 지정해야 합니다")
    private List<String> formats; // pdf, csv, excel

    public List<Long> getPaymentIds() {
        return paymentIds == null ? null : new ArrayList<>(paymentIds);
    }

    public void setPaymentIds(List<Long> paymentIds) {
        this.paymentIds = paymentIds == null ? null : new ArrayList<>(paymentIds);
    }

    public List<String> getFormats() {
        return formats == null ? null : new ArrayList<>(formats);
    }

    public void setFormats(List<String> formats) {
        this.formats = formats == null ? null : new ArrayList<>(formats);
    }
}
