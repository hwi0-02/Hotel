package com.example.backend.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class PaymentDTOs {

    @Getter
    @Setter
    @ToString
    public static class PaymentDTO {
        private Long reservationId;

        private String customerName;
        private String email;
        private String phone;

        private int amount;
        private String orderId;
        private String orderName;
        private String paymentKey;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class TossPaymentResponse {

        @JsonProperty("orderId")
        private String orderId;

        @JsonProperty("orderName")
        private String orderName;

        @JsonProperty("requestedAt")
        private String requestedAt;

        @JsonProperty("approvedAt")
        private String approvedAt;

        @JsonProperty("totalAmount")
        private Integer totalAmount;

        @JsonProperty("status")
        private String status;

        @JsonProperty("receipt")
        private Receipt receipt;

        @JsonProperty("method")
        private String method;

        public Receipt getReceipt() {
            return receipt == null ? null : receipt.copy();
        }

        public void setReceipt(Receipt receipt) {
            this.receipt = receipt == null ? null : receipt.copy();
        }

        @Data
        public static class Receipt {
            @JsonProperty("url")
            private String url;

            private Receipt copy() {
                Receipt copy = new Receipt();
                copy.url = this.url;
                return copy;
            }
        }

    }

}
