package com.servicedesk.modules.currency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicedesk.marketplace.plugin.*;
import com.servicedesk.modules.currency.dto.*;
import com.servicedesk.modules.currency.service.CurrencyService;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Currency Exchange Module Plugin
 * Provides currency exchange rates from CBU.uz (Central Bank of Uzbekistan)
 */
@Slf4j
public class CurrencyExchangePlugin implements ModulePlugin {

    private static final String MODULE_ID = "currency-exchange";
    private static final String VERSION = "1.0.0";

    private CurrencyService currencyService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getModuleId() {
        return MODULE_ID;
    }

    @Override
    public String getName() {
        return "Currency Exchange";
    }

    @Override
    public String getDescription() {
        return "Real-time currency exchange rates from the Central Bank of Uzbekistan (CBU.uz). " +
                "Features include current rates, historical data, currency conversion, and dashboard widgets.";
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String getAuthor() {
        return "Service Desk Team";
    }

    @Override
    public ModuleCategory getCategory() {
        return ModuleCategory.FINANCE;
    }

    @Override
    public String getIcon() {
        return "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNCIgaGVpZ2h0PSIyNCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9ImN1cnJlbnRDb2xvciIgc3Ryb2tlLXdpZHRoPSIyIiBzdHJva2UtbGluZWNhcD0icm91bmQiIHN0cm9rZS1saW5lam9pbj0icm91bmQiPjxsaW5lIHgxPSIxMiIgeTE9IjEiIHgyPSIxMiIgeTI9IjIzIi8+PHBhdGggZD0iTTE3IDVINy41YTMuNSAzLjUgMCAwIDAgMCA3aDlhMy41IDMuNSAwIDAgMSAwIDdINiIvPjwvc3ZnPg==";
    }

    @Override
    public List<String> getRequiredPermissions() {
        return List.of("modules.currency.view", "modules.currency.convert");
    }

    @Override
    public String getConfigurationSchema() {
        return """
            {
                "type": "object",
                "properties": {
                    "defaultCurrency": {
                        "type": "string",
                        "title": "Default Currency",
                        "description": "Default currency for conversions",
                        "default": "USD"
                    },
                    "popularCurrencies": {
                        "type": "array",
                        "title": "Popular Currencies",
                        "description": "Currencies to show in the widget",
                        "items": { "type": "string" },
                        "default": ["USD", "EUR", "RUB", "GBP"]
                    },
                    "refreshInterval": {
                        "type": "integer",
                        "title": "Refresh Interval (minutes)",
                        "description": "How often to refresh rates",
                        "default": 30,
                        "minimum": 5,
                        "maximum": 1440
                    },
                    "showDiff": {
                        "type": "boolean",
                        "title": "Show Rate Changes",
                        "description": "Display rate change from previous day",
                        "default": true
                    }
                }
            }
            """;
    }

    @Override
    public Map<String, Object> getDefaultConfiguration() {
        return Map.of(
                "defaultCurrency", "USD",
                "popularCurrencies", List.of("USD", "EUR", "RUB", "GBP", "CNY", "KZT"),
                "refreshInterval", 30,
                "showDiff", true
        );
    }

    @Override
    public void onInstall(ModuleContext context) {
        log.info("Installing Currency Exchange module for tenant {}", context.getTenantId());
    }

    @Override
    public void onUninstall(ModuleContext context) {
        log.info("Uninstalling Currency Exchange module for tenant {}", context.getTenantId());
        // Clear any cached data
        context.getServiceLocator().getCacheService().evictByPrefix("currency:");
    }

    @Override
    public void onEnable(ModuleContext context) {
        log.info("Enabling Currency Exchange module for tenant {}", context.getTenantId());
        // Initialize currency service
        currencyService = new CurrencyService(
                context.getServiceLocator().getHttpClient(),
                context.getServiceLocator().getCacheService()
        );
    }

    @Override
    public void onDisable(ModuleContext context) {
        log.info("Disabling Currency Exchange module for tenant {}", context.getTenantId());
        currencyService = null;
    }

    @Override
    public List<ModuleEndpoint> getEndpoints() {
        return List.of(
                // Get all current rates
                ModuleEndpoint.builder()
                        .method("GET")
                        .path("/rates")
                        .description("Get all current exchange rates")
                        .requiredPermissions(List.of("modules.currency.view"))
                        .handler(this::handleGetRates)
                        .build(),

                // Get popular rates
                ModuleEndpoint.builder()
                        .method("GET")
                        .path("/rates/popular")
                        .description("Get popular currency rates")
                        .requiredPermissions(List.of("modules.currency.view"))
                        .handler(this::handleGetPopularRates)
                        .build(),

                // Get specific currency rate
                ModuleEndpoint.builder()
                        .method("GET")
                        .path("/rates/{currency}")
                        .description("Get rate for a specific currency")
                        .requiredPermissions(List.of("modules.currency.view"))
                        .handler(this::handleGetRate)
                        .build(),

                // Get historical rates
                ModuleEndpoint.builder()
                        .method("GET")
                        .path("/rates/history/{currency}")
                        .description("Get historical rates for a currency")
                        .requiredPermissions(List.of("modules.currency.view"))
                        .handler(this::handleGetHistoricalRates)
                        .build(),

                // Get rates by date
                ModuleEndpoint.builder()
                        .method("GET")
                        .path("/rates/date/{date}")
                        .description("Get rates for a specific date")
                        .requiredPermissions(List.of("modules.currency.view"))
                        .handler(this::handleGetRatesByDate)
                        .build(),

                // Convert currency
                ModuleEndpoint.builder()
                        .method("POST")
                        .path("/convert")
                        .description("Convert amount between currencies")
                        .requiredPermissions(List.of("modules.currency.convert"))
                        .handler(this::handleConvert)
                        .build(),

                // Widget data endpoint
                ModuleEndpoint.builder()
                        .method("GET")
                        .path("/widget/data")
                        .description("Get data for dashboard widget")
                        .requiredPermissions(List.of("modules.currency.view"))
                        .handler(this::handleWidgetData)
                        .build()
        );
    }

    @Override
    public List<ModuleWidget> getWidgets() {
        return List.of(
                ModuleWidget.builder()
                        .id("currency-rates-widget")
                        .title("Currency Rates")
                        .description("Display current exchange rates for popular currencies")
                        .type(ModuleWidget.WidgetType.TABLE)
                        .size(ModuleWidget.WidgetSize.MEDIUM)
                        .dataEndpoint("/api/modules/currency-exchange/api/widget/data")
                        .refreshInterval(1800) // 30 minutes
                        .requiredPermission("modules.currency.view")
                        .defaultConfig(Map.of(
                                "currencies", List.of("USD", "EUR", "RUB", "GBP"),
                                "showDiff", true
                        ))
                        .build(),

                ModuleWidget.builder()
                        .id("currency-converter-widget")
                        .title("Currency Converter")
                        .description("Quick currency conversion tool")
                        .type(ModuleWidget.WidgetType.CUSTOM)
                        .size(ModuleWidget.WidgetSize.SMALL)
                        .componentName("CurrencyConverterWidget")
                        .requiredPermission("modules.currency.convert")
                        .build()
        );
    }

    @Override
    public List<ModuleMenuItem> getMenuItems() {
        return List.of(
                ModuleMenuItem.builder()
                        .id("currency-exchange-menu")
                        .label("Currency Exchange")
                        .icon("attach_money")
                        .route("/modules/currency-exchange")
                        .location(ModuleMenuItem.MenuLocation.MAIN)
                        .order(50)
                        .requiredPermission("modules.currency.view")
                        .build()
        );
    }

    @Override
    public ModuleHealth getHealth() {
        try {
            if (currencyService != null) {
                CurrencyRatesResponse rates = currencyService.getCurrentRates();
                if (rates != null && !rates.getRates().isEmpty()) {
                    return ModuleHealth.healthy("Connected to CBU.uz, " + rates.getRates().size() + " currencies available");
                }
            }
            return ModuleHealth.degraded("Currency service not initialized");
        } catch (Exception e) {
            return ModuleHealth.unhealthy("Failed to connect to CBU.uz: " + e.getMessage());
        }
    }

    // Endpoint handlers

    private ModuleResponse handleGetRates(ModuleRequest request) {
        try {
            CurrencyRatesResponse rates = currencyService.getCurrentRates();
            return ModuleResponse.ok(rates, objectMapper);
        } catch (Exception e) {
            return ModuleResponse.error(500, e.getMessage());
        }
    }

    private ModuleResponse handleGetPopularRates(ModuleRequest request) {
        try {
            List<CurrencyRate> rates = currencyService.getPopularRates();
            return ModuleResponse.ok(rates, objectMapper);
        } catch (Exception e) {
            return ModuleResponse.error(500, e.getMessage());
        }
    }

    private ModuleResponse handleGetRate(ModuleRequest request) {
        try {
            String currency = extractPathParam(request.getPath(), "/rates/");
            return currencyService.getRate(currency)
                    .map(rate -> ModuleResponse.ok(rate, objectMapper))
                    .orElse(ModuleResponse.notFound("Currency not found: " + currency));
        } catch (Exception e) {
            return ModuleResponse.error(500, e.getMessage());
        }
    }

    private ModuleResponse handleGetHistoricalRates(ModuleRequest request) {
        try {
            String currency = extractPathParam(request.getPath(), "/rates/history/");
            int days = Integer.parseInt(request.getQueryParams().getOrDefault("days", "7"));
            List<CurrencyRate> rates = currencyService.getHistoricalRates(currency, days);
            return ModuleResponse.ok(rates, objectMapper);
        } catch (Exception e) {
            return ModuleResponse.error(500, e.getMessage());
        }
    }

    private ModuleResponse handleGetRatesByDate(ModuleRequest request) {
        try {
            String dateStr = extractPathParam(request.getPath(), "/rates/date/");
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
            CurrencyRatesResponse rates = currencyService.getRatesByDate(date);
            return ModuleResponse.ok(rates, objectMapper);
        } catch (Exception e) {
            return ModuleResponse.error(500, e.getMessage());
        }
    }

    private ModuleResponse handleConvert(ModuleRequest request) {
        try {
            ConversionRequest conversionRequest = objectMapper.readValue(request.getBody(), ConversionRequest.class);
            ConversionResult result = currencyService.convert(conversionRequest);
            return ModuleResponse.ok(result, objectMapper);
        } catch (IllegalArgumentException e) {
            return ModuleResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            return ModuleResponse.error(500, e.getMessage());
        }
    }

    private ModuleResponse handleWidgetData(ModuleRequest request) {
        try {
            List<CurrencyRate> rates = currencyService.getPopularRates();
            Map<String, Object> widgetData = Map.of(
                    "rates", rates,
                    "updatedAt", currencyService.getCurrentRates().getUpdatedAt().toString(),
                    "source", "CBU.uz"
            );
            return ModuleResponse.ok(widgetData, objectMapper);
        } catch (Exception e) {
            return ModuleResponse.error(500, e.getMessage());
        }
    }

    private String extractPathParam(String path, String prefix) {
        if (path.startsWith(prefix)) {
            String param = path.substring(prefix.length());
            int slashIndex = param.indexOf('/');
            return slashIndex > 0 ? param.substring(0, slashIndex) : param;
        }
        return "";
    }
}
