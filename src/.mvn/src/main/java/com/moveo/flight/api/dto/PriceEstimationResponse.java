package com.moveo.flight.api.dto;

import java.math.BigDecimal;

public class PriceEstimationResponse {
    private BigDecimal estimatedTotalPrice;

    public PriceEstimationResponse(BigDecimal estimated) {
        this.estimatedTotalPrice = estimated;
    }

    public BigDecimal getEstimatedTotalPrice() {
        return this.estimatedTotalPrice;
    }
}
