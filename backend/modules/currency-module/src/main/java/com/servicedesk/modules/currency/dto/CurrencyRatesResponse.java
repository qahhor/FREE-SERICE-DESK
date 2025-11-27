package com.servicedesk.modules.currency.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for currency rates
 */
@Data
@Builder
public class CurrencyRatesResponse {

    private List<CurrencyRate> rates;
    private String date;
    private Instant updatedAt;
    private String source;

    /**
     * Find rate by currency code
     */
    public CurrencyRate findByCode(String currencyCode) {
        return rates.stream()
                .filter(r -> r.getCurrency().equalsIgnoreCase(currencyCode))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get popular currencies (USD, EUR, RUB, GBP)
     */
    public List<CurrencyRate> getPopularCurrencies() {
        List<String> popular = List.of("USD", "EUR", "RUB", "GBP", "CHF", "JPY", "CNY", "KZT");
        return rates.stream()
                .filter(r -> popular.contains(r.getCurrency()))
                .toList();
    }
}
