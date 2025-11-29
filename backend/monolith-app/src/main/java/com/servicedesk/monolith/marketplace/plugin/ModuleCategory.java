package com.servicedesk.monolith.marketplace.plugin;

/**
 * Categories for marketplace modules
 */
public enum ModuleCategory {
    INTEGRATION("Integrations", "Connect with external services and APIs"),
    ANALYTICS("Analytics & Reports", "Data visualization and reporting tools"),
    AUTOMATION("Automation", "Workflow automation and triggers"),
    COMMUNICATION("Communication", "Messaging and notification channels"),
    PRODUCTIVITY("Productivity", "Tools to improve agent efficiency"),
    CUSTOMER_EXPERIENCE("Customer Experience", "Enhance customer interactions"),
    SECURITY("Security", "Security and compliance features"),
    AI_ML("AI & Machine Learning", "Artificial intelligence features"),
    FINANCE("Finance", "Financial and billing tools"),
    UTILITIES("Utilities", "General utility modules"),
    THEMES("Themes & UI", "Visual customization"),
    LANGUAGES("Languages", "Additional language packs");

    private final String displayName;
    private final String description;

    ModuleCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
