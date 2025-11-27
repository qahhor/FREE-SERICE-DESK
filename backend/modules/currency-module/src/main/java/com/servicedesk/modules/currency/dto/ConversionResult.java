package com.servicedesk.modules.currency.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for currency conversion result
 */
@Data
@Builder
public class ConversionResult {

    private String fromCurrency;
    private String toCurrency;
    private BigDecimal amount;
    private BigDecimal result;
    private BigDecimal rate;
    private BigDecimal inverseRate;
    private String date;
    private Instant calculatedAt;

    /**
     * Get formatted result string
     */
    public String getFormattedResult() {
        return String.format("%.2f %s = %.2f %s", amount, fromCurrency, result, toCurrency);
    }
}
