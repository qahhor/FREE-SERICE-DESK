package com.servicedesk.monolith.marketplace.controller;

import com.servicedesk.monolith.marketplace.plugin.ModuleRequest;
import com.servicedesk.monolith.marketplace.plugin.ModuleResponse;
import com.servicedesk.monolith.marketplace.service.ModuleLoaderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller that proxies requests to module endpoints
 */
@RestController
@RequestMapping("/api/modules/{moduleId}/api")
@RequiredArgsConstructor
@Tag(name = "Module Endpoints", description = "Access module-provided endpoints")
public class ModuleEndpointController {

    private final ModuleLoaderService moduleLoaderService;

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST,
            RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    @Operation(summary = "Execute module endpoint")
    public ResponseEntity<String> executeModuleEndpoint(
            @PathVariable String moduleId,
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestHeader(value = "X-User-ID", required = false) UUID userId,
            @RequestBody(required = false) String body,
            HttpServletRequest request) {

        // Extract path after /api/modules/{moduleId}/api
        String fullPath = request.getRequestURI();
        String basePath = "/api/modules/" + moduleId + "/api";
        String modulePath = fullPath.substring(fullPath.indexOf(basePath) + basePath.length());
        if (modulePath.isEmpty()) {
            modulePath = "/";
        }

        // Build module request
        ModuleRequest moduleRequest = ModuleRequest.builder()
                .method(request.getMethod())
                .path(modulePath)
                .headers(extractHeaders(request))
                .queryParams(extractQueryParams(request))
                .body(body)
                .userId(userId)
                .tenantId(tenantId)
                .build();

        // Execute and return response
        ModuleResponse response = moduleLoaderService.executeEndpoint(moduleId, tenantId, moduleRequest);

        ResponseEntity.BodyBuilder builder = ResponseEntity.status(response.getStatusCode());

        if (response.getHeaders() != null) {
            response.getHeaders().forEach(builder::header);
        }

        if (response.getContentType() != null) {
            builder.header("Content-Type", response.getContentType());
        }

        return builder.body(response.getBody());
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }
        return headers;
    }

    private Map<String, String> extractQueryParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) {
                params.put(key, values[0]);
            }
        });
        return params;
    }
}
