package com.servicedesk.modules.currency.config;

import com.servicedesk.modules.currency.CurrencyExchangePlugin;
import com.servicedesk.marketplace.service.ModuleLoaderService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for the Currency Exchange module
 */
@Configuration
@ConditionalOnClass(ModuleLoaderService.class)
@RequiredArgsConstructor
@Slf4j
public class CurrencyModuleAutoConfiguration {

    private final ModuleLoaderService moduleLoaderService;

    @Bean
    public CurrencyExchangePlugin currencyExchangePlugin() {
        return new CurrencyExchangePlugin();
    }

    @PostConstruct
    public void registerModule() {
        log.info("Registering Currency Exchange module");
        moduleLoaderService.registerModule("currency-exchange", CurrencyExchangePlugin.class);
    }
}
