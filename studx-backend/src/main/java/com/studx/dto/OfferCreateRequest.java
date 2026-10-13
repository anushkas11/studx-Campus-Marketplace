package com.studx.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class OfferCreateRequest {

    @NotNull(message = "Offered price is required")
    @DecimalMin(value = "1.00", message = "Offer price must be at least ₹1.00")
    private BigDecimal offeredPrice;

    private String message;

    public OfferCreateRequest() {
    }

    public OfferCreateRequest(BigDecimal offeredPrice, String message) {
        this.offeredPrice = offeredPrice;
        this.message = message;
    }

    public BigDecimal getOfferedPrice() {
        return offeredPrice;
    }

    public void setOfferedPrice(BigDecimal offeredPrice) {
        this.offeredPrice = offeredPrice;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
