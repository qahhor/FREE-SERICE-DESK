package com.servicedesk.modules.currency.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicedesk.modules.currency.dto.*;
import com.servicedesk.marketplace.plugin.ModuleCacheService;
import com.servicedesk.marketplace.plugin.ModuleHttpClient;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for fetching and processing currency exchange rates from CBU.uz
 */
@Slf4j
public class CurrencyService {

    private static final String CBU_API_URL = "https://cbu.uz/uz/arkhiv-kursov-valyut/json/";
    private static final String CBU_API_URL_BY_DATE = "https://cbu.uz/uz/arkhiv-kursov-valyut/json/all/%s/";
    private static final String CACHE_KEY_RATES = "currency:rates";
    private static final String CACHE_KEY_RATES_DATE = "currency:rates:%s";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final ModuleHttpClient httpClient;
    private final ModuleCacheService cacheService;
    private final ObjectMapper objectMapper;

    public CurrencyService(ModuleHttpClient httpClient, ModuleCacheService cacheService) {
        this.httpClient = httpClient;
        this.cacheService = cacheService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get current exchange rates
     */
    public CurrencyRatesResponse getCurrentRates() {
        // Check cache first
        Optional<CurrencyRatesResponse> cached = cacheService.get(CACHE_KEY_RATES, CurrencyRatesResponse.class);
        if (cached.isPresent()) {
            log.debug("Returning cached currency rates");
            return cached.get();
        }

        // Fetch from CBU API
        log.info("Fetching currency rates from CBU.uz");
        ModuleHttpClient.HttpResponse response = httpClient.get(CBU_API_URL, Map.of(
                "Accept", "application/json",
                "User-Agent", "ServiceDesk/1.0"
        ));

        if (!response.isSuccess()) {
            log.error("Failed to fetch currency rates: {}", response.statusCode());
            throw new RuntimeException("Failed to fetch currency rates from CBU.uz");
        }

        try {
            List<CurrencyRate> rates = objectMapper.readValue(
                    response.body(),
                    new TypeReference<List<CurrencyRate>>() {}
            );

            String date = rates.isEmpty() ? LocalDate.now().toString() : rates.get(0).getDate();

            CurrencyRatesResponse result = CurrencyRatesResponse.builder()
                    .rates(rates)
                    .date(date)
                    .updatedAt(Instant.now())
                    .source("cbu.uz")
                    .build();

            // Cache the result
            cacheService.put(CACHE_KEY_RATES, result, CACHE_TTL);

            return result;
        } catch (Exception e) {
            log.error("Failed to parse currency rates: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse currency rates", e);
        }
    }

    /**
     * Get exchange rates for a specific date
     */
    public CurrencyRatesResponse getRatesByDate(LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String cacheKey = String.format(CACHE_KEY_RATES_DATE, dateStr);

        // Check cache first
        Optional<CurrencyRatesResponse> cached = cacheService.get(cacheKey, CurrencyRatesResponse.class);
        if (cached.isPresent()) {
            return cached.get();
        }

        // Fetch from CBU API
        String url = String.format(CBU_API_URL_BY_DATE, dateStr);
        log.info("Fetching currency rates from CBU.uz for date: {}", dateStr);

        ModuleHttpClient.HttpResponse response = httpClient.get(url, Map.of(
                "Accept", "application/json",
                "User-Agent", "ServiceDesk/1.0"
        ));

        if (!response.isSuccess()) {
            log.error("Failed to fetch currency rates for date {}: {}", dateStr, response.statusCode());
            throw new RuntimeException("Failed to fetch currency rates from CBU.uz");
        }

        try {
            List<CurrencyRate> rates = objectMapper.readValue(
                    response.body(),
                    new TypeReference<List<CurrencyRate>>() {}
            );

            CurrencyRatesResponse result = CurrencyRatesResponse.builder()
                    .rates(rates)
                    .date(dateStr)
                    .updatedAt(Instant.now())
                    .source("cbu.uz")
                    .build();

            // Cache the result (longer TTL for historical data)
            cacheService.put(cacheKey, result, Duration.ofHours(24));

            return result;
        } catch (Exception e) {
            log.error("Failed to parse currency rates: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse currency rates", e);
        }
    }

    /**
     * Get rate for a specific currency
     */
    public Optional<CurrencyRate> getRate(String currencyCode) {
        CurrencyRatesResponse rates = getCurrentRates();
        return Optional.ofNullable(rates.findByCode(currencyCode));
    }

    /**
     * Get popular currencies (for dashboard widget)
     */
    public List<CurrencyRate> getPopularRates() {
        return getCurrentRates().getPopularCurrencies();
    }

    /**
     * Convert currency
     */
    public ConversionResult convert(ConversionRequest request) {
        CurrencyRatesResponse rates = getCurrentRates();

        BigDecimal amount = request.getAmount();
        String from = request.getFromCurrency().toUpperCase();
        String to = request.getToCurrency().toUpperCase();

        // Both are UZS
        if (from.equals("UZS") && to.equals("UZS")) {
            return ConversionResult.builder()
                    .fromCurrency(from)
                    .toCurrency(to)
                    .amount(amount)
                    .result(amount)
                    .rate(BigDecimal.ONE)
                    .inverseRate(BigDecimal.ONE)
                    .date(rates.getDate())
                    .calculatedAt(Instant.now())
                    .build();
        }

        BigDecimal result;
        BigDecimal rate;
        BigDecimal inverseRate;

        if (from.equals("UZS")) {
            // UZS to foreign currency
            CurrencyRate toRate = rates.findByCode(to);
            if (toRate == null) {
                throw new IllegalArgumentException("Currency not found: " + to);
            }
            rate = toRate.getRateAsDecimal().divide(BigDecimal.valueOf(toRate.getNominalAsInt()), 10, RoundingMode.HALF_UP);
            inverseRate = BigDecimal.ONE.divide(rate, 10, RoundingMode.HALF_UP);
            result = amount.divide(rate, 4, RoundingMode.HALF_UP);
        } else if (to.equals("UZS")) {
            // Foreign currency to UZS
            CurrencyRate fromRate = rates.findByCode(from);
            if (fromRate == null) {
                throw new IllegalArgumentException("Currency not found: " + from);
            }
            rate = fromRate.getRateAsDecimal().divide(BigDecimal.valueOf(fromRate.getNominalAsInt()), 10, RoundingMode.HALF_UP);
            inverseRate = BigDecimal.ONE.divide(rate, 10, RoundingMode.HALF_UP);
            result = amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        } else {
            // Foreign to foreign (via UZS)
            CurrencyRate fromRate = rates.findByCode(from);
            CurrencyRate toRate = rates.findByCode(to);
            if (fromRate == null) {
                throw new IllegalArgumentException("Currency not found: " + from);
            }
            if (toRate == null) {
                throw new IllegalArgumentException("Currency not found: " + to);
            }

            BigDecimal fromRatePerUnit = fromRate.getRateAsDecimal()
                    .divide(BigDecimal.valueOf(fromRate.getNominalAsInt()), 10, RoundingMode.HALF_UP);
            BigDecimal toRatePerUnit = toRate.getRateAsDecimal()
                    .divide(BigDecimal.valueOf(toRate.getNominalAsInt()), 10, RoundingMode.HALF_UP);

            // Convert: from -> UZS -> to
            BigDecimal uzsAmount = amount.multiply(fromRatePerUnit);
            result = uzsAmount.divide(toRatePerUnit, 4, RoundingMode.HALF_UP);

            rate = fromRatePerUnit.divide(toRatePerUnit, 10, RoundingMode.HALF_UP);
            inverseRate = BigDecimal.ONE.divide(rate, 10, RoundingMode.HALF_UP);
        }

        return ConversionResult.builder()
                .fromCurrency(from)
                .toCurrency(to)
                .amount(amount)
                .result(result)
                .rate(rate)
                .inverseRate(inverseRate)
                .date(rates.getDate())
                .calculatedAt(Instant.now())
                .build();
    }

    /**
     * Get historical rates for a currency
     */
    public List<CurrencyRate> getHistoricalRates(String currencyCode, int days) {
        List<CurrencyRate> historical = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < days; i++) {
            LocalDate date = today.minusDays(i);
            try {
                CurrencyRatesResponse rates = getRatesByDate(date);
                CurrencyRate rate = rates.findByCode(currencyCode);
                if (rate != null) {
                    historical.add(rate);
                }
            } catch (Exception e) {
                log.warn("Could not fetch rates for date {}: {}", date, e.getMessage());
            }
        }

        return historical;
    }

    /**
     * Clear cached rates
     */
    public void clearCache() {
        cacheService.evictByPrefix("currency:");
        log.info("Currency cache cleared");
    }
}
