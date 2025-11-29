package com.servicedesk.monolith.marketplace.plugin;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Represents a dashboard widget provided by a module
 */
@Data
@Builder
public class ModuleWidget {

    /**
     * Unique widget identifier
     */
    private String id;

    /**
     * Widget display title
     */
    private String title;

    /**
     * Widget description
     */
    private String description;

    /**
     * Widget type: CHART, TABLE, METRIC, CUSTOM
     */
    private WidgetType type;

    /**
     * Widget size: SMALL (1x1), MEDIUM (2x1), LARGE (2x2), FULL (4x1)
     */
    private WidgetSize size;

    /**
     * API endpoint to fetch widget data
     */
    private String dataEndpoint;

    /**
     * Refresh interval in seconds (0 for no auto-refresh)
     */
    @Builder.Default
    private int refreshInterval = 0;

    /**
     * Custom component name for CUSTOM type widgets
     */
    private String componentName;

    /**
     * Default configuration for the widget
     */
    private Map<String, Object> defaultConfig;

    /**
     * Permissions required to view this widget
     */
    private String requiredPermission;

    public enum WidgetType {
        CHART,
        TABLE,
        METRIC,
        LIST,
        CUSTOM
    }

    public enum WidgetSize {
        SMALL,
        MEDIUM,
        LARGE,
        FULL
    }
}
