package com.servicedesk.modules.currency.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Request DTO for currency conversion
 */
@Data
public class ConversionRequest {

    private String fromCurrency;
    private String toCurrency;
    private BigDecimal amount;
}
