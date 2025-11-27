package com.servicedesk.marketplace.plugin;

import java.util.Optional;

/**
 * Service locator for modules to access platform services
 */
public interface ModuleServiceLocator {

    /**
     * Get a service by its interface type
     */
    <T> Optional<T> getService(Class<T> serviceClass);

    /**
     * Get the HTTP client for external API calls
     */
    ModuleHttpClient getHttpClient();

    /**
     * Get the cache service for module data caching
     */
    ModuleCacheService getCacheService();

    /**
     * Get the event publisher for emitting events
     */
    ModuleEventPublisher getEventPublisher();

    /**
     * Get the storage service for persisting module data
     */
    ModuleStorageService getStorageService();

    /**
     * Get the notification service for sending notifications
     */
    ModuleNotificationService getNotificationService();
}
