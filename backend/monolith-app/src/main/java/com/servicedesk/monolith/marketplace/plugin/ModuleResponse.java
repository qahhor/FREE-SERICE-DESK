package com.servicedesk.monolith.marketplace.plugin;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an HTTP response from a module endpoint
 */
@Data
@Builder
public class ModuleResponse {

    private int statusCode;
    private Map<String, String> headers;
    private String body;
    private String contentType;

    public static ModuleResponse ok(String body) {
        return ModuleResponse.builder()
                .statusCode(200)
                .body(body)
                .contentType("application/json")
                .headers(new HashMap<>())
                .build();
    }

    public static ModuleResponse ok(Object body, com.fasterxml.jackson.databind.ObjectMapper mapper) {
        try {
            return ModuleResponse.builder()
                    .statusCode(200)
                    .body(mapper.writeValueAsString(body))
                    .contentType("application/json")
                    .headers(new HashMap<>())
                    .build();
        } catch (Exception e) {
            return error(500, "Serialization error: " + e.getMessage());
        }
    }

    public static ModuleResponse created(String body) {
        return ModuleResponse.builder()
                .statusCode(201)
                .body(body)
                .contentType("application/json")
                .headers(new HashMap<>())
                .build();
    }

    public static ModuleResponse noContent() {
        return ModuleResponse.builder()
                .statusCode(204)
                .headers(new HashMap<>())
                .build();
    }

    public static ModuleResponse error(int statusCode, String message) {
        return ModuleResponse.builder()
                .statusCode(statusCode)
                .body("{\"error\":\"" + message + "\"}")
                .contentType("application/json")
                .headers(new HashMap<>())
                .build();
    }

    public static ModuleResponse notFound(String message) {
        return error(404, message);
    }

    public static ModuleResponse badRequest(String message) {
        return error(400, message);
    }

    public static ModuleResponse unauthorized() {
        return error(401, "Unauthorized");
    }

    public static ModuleResponse forbidden() {
        return error(403, "Forbidden");
    }
}
