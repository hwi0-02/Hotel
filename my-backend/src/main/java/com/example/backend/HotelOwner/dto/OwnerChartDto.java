package com.example.backend.HotelOwner.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class OwnerChartDto {
    private List<String> labels;
    private List<Double> data;

    public List<String> getLabels() {
        return labels == null ? null : new ArrayList<>(labels);
    }

    public void setLabels(List<String> labels) {
        this.labels = labels == null ? null : new ArrayList<>(labels);
    }

    public List<Double> getData() {
        return data == null ? null : new ArrayList<>(data);
    }

    public void setData(List<Double> data) {
        this.data = data == null ? null : new ArrayList<>(data);
    }
}