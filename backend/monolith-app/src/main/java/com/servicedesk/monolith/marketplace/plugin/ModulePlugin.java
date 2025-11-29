package com.servicedesk.monolith.marketplace.plugin;

import org.pf4j.ExtensionPoint;

import java.util.List;
import java.util.Map;

/**
 * Base interface for all Service Desk modules/plugins.
 * Modules must implement this interface to be recognized by the marketplace.
 */
public interface ModulePlugin extends ExtensionPoint {

    /**
     * Unique identifier for the module
     */
    String getModuleId();

    /**
     * Human-readable name
     */
    String getName();

    /**
     * Module description
     */
    String getDescription();

    /**
     * Module version (semver format)
     */
    String getVersion();

    /**
     * Module author/vendor
     */
    String getAuthor();

    /**
     * Module category
     */
    ModuleCategory getCategory();

    /**
     * Module icon URL or base64 data
     */
    default String getIcon() {
        return null;
    }

    /**
     * List of permissions required by this module
     */
    default List<String> getRequiredPermissions() {
        return List.of();
    }

    /**
     * List of module IDs this module depends on
     */
    default List<String> getDependencies() {
        return List.of();
    }

    /**
     * Configuration schema for this module (JSON Schema format)
     */
    default String getConfigurationSchema() {
        return "{}";
    }

    /**
     * Default configuration values
     */
    default Map<String, Object> getDefaultConfiguration() {
        return Map.of();
    }

    /**
     * Called when module is installed
     */
    default void onInstall(ModuleContext context) {
        // Default: do nothing
    }

    /**
     * Called when module is uninstalled
     */
    default void onUninstall(ModuleContext context) {
        // Default: do nothing
    }

    /**
     * Called when module is enabled
     */
    default void onEnable(ModuleContext context) {
        // Default: do nothing
    }

    /**
     * Called when module is disabled
     */
    default void onDisable(ModuleContext context) {
        // Default: do nothing
    }

    /**
     * Called when module configuration is updated
     */
    default void onConfigurationChange(ModuleContext context, Map<String, Object> oldConfig, Map<String, Object> newConfig) {
        // Default: do nothing
    }

    /**
     * Get list of REST endpoints provided by this module
     */
    default List<ModuleEndpoint> getEndpoints() {
        return List.of();
    }

    /**
     * Get list of dashboard widgets provided by this module
     */
    default List<ModuleWidget> getWidgets() {
        return List.of();
    }

    /**
     * Get list of menu items to add to the UI
     */
    default List<ModuleMenuItem> getMenuItems() {
        return List.of();
    }

    /**
     * Get list of event handlers for system events
     */
    default List<ModuleEventHandler> getEventHandlers() {
        return List.of();
    }

    /**
     * Health check for the module
     */
    default ModuleHealth getHealth() {
        return ModuleHealth.healthy();
    }
}
