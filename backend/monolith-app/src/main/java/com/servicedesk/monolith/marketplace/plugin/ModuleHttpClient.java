package com.servicedesk.monolith.marketplace.plugin;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP client interface for modules to make external API calls
 */
public interface ModuleHttpClient {

    /**
     * Synchronous GET request
     */
    HttpResponse get(String url, Map<String, String> headers);

    /**
     * Synchronous POST request
     */
    HttpResponse post(String url, String body, Map<String, String> headers);

    /**
     * Synchronous PUT request
     */
    HttpResponse put(String url, String body, Map<String, String> headers);

    /**
     * Synchronous DELETE request
     */
    HttpResponse delete(String url, Map<String, String> headers);

    /**
     * Async GET request
     */
    CompletableFuture<HttpResponse> getAsync(String url, Map<String, String> headers);

    /**
     * Async POST request
     */
    CompletableFuture<HttpResponse> postAsync(String url, String body, Map<String, String> headers);

    /**
     * HTTP Response wrapper
     */
    record HttpResponse(
            int statusCode,
            String body,
            Map<String, String> headers
    ) {
        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }
    }
}
