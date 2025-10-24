package com.example.backend.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentExportRequest {

    public enum Scope {
        PAGE,
        ALL,
        SELECTION
    }

    @NotBlank
    private String format; // csv, excel, pdf

    @NotNull
    private Scope scope;

    private List<Long> selectedIds;

    private List<String> columns;

    @Valid
    private PaymentFilterDto filters;

    private boolean async;

    public List<Long> getSelectedIds() {
        return selectedIds == null ? null : new ArrayList<>(selectedIds);
    }

    public void setSelectedIds(List<Long> selectedIds) {
        this.selectedIds = selectedIds == null ? null : new ArrayList<>(selectedIds);
    }

    public List<String> getColumns() {
        return columns == null ? null : new ArrayList<>(columns);
    }

    public void setColumns(List<String> columns) {
        this.columns = columns == null ? null : new ArrayList<>(columns);
    }

    public PaymentFilterDto getFilters() {
        return filters == null ? null : filters.copy();
    }

    public void setFilters(PaymentFilterDto filters) {
        this.filters = filters == null ? null : filters.copy();
    }
}
