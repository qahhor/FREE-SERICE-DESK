package com.servicedesk.modules.currency.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO representing a currency exchange rate from CBU.uz
 */
@Data
public class CurrencyRate {

    private Integer id;

    @JsonProperty("Code")
    private String code;

    @JsonProperty("Ccy")
    private String currency;

    @JsonProperty("CcyNm_RU")
    private String nameRu;

    @JsonProperty("CcyNm_UZ")
    private String nameUz;

    @JsonProperty("CcyNm_UZC")
    private String nameUzCyrillic;

    @JsonProperty("CcyNm_EN")
    private String nameEn;

    @JsonProperty("Nominal")
    private String nominal;

    @JsonProperty("Rate")
    private String rate;

    @JsonProperty("Diff")
    private String diff;

    @JsonProperty("Date")
    private String date;

    /**
     * Get the rate as a BigDecimal
     */
    public BigDecimal getRateAsDecimal() {
        try {
            return new BigDecimal(rate.replace(",", "."));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Get the diff as a BigDecimal
     */
    public BigDecimal getDiffAsDecimal() {
        try {
            return new BigDecimal(diff.replace(",", "."));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Get nominal as integer
     */
    public int getNominalAsInt() {
        try {
            return Integer.parseInt(nominal);
        } catch (Exception e) {
            return 1;
        }
    }

    /**
     * Check if rate increased
     */
    public boolean isIncreased() {
        return getDiffAsDecimal().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Check if rate decreased
     */
    public boolean isDecreased() {
        return getDiffAsDecimal().compareTo(BigDecimal.ZERO) < 0;
    }
}
