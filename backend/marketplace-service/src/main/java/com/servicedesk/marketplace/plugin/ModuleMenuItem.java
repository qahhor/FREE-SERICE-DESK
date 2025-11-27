package com.servicedesk.marketplace.plugin;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Represents a menu item added by a module
 */
@Data
@Builder
public class ModuleMenuItem {

    /**
     * Unique identifier for the menu item
     */
    private String id;

    /**
     * Display label (supports i18n keys)
     */
    private String label;

    /**
     * Icon name (Material Icons)
     */
    private String icon;

    /**
     * Route path for Angular router
     */
    private String route;

    /**
     * Parent menu item ID (for submenus)
     */
    private String parentId;

    /**
     * Menu location: MAIN, SETTINGS, USER, ADMIN
     */
    private MenuLocation location;

    /**
     * Display order within the menu
     */
    @Builder.Default
    private int order = 100;

    /**
     * Required permission to see this menu item
     */
    private String requiredPermission;

    /**
     * Child menu items
     */
    private List<ModuleMenuItem> children;

    /**
     * Badge text to display (e.g., "New", count)
     */
    private String badge;

    /**
     * Badge color: PRIMARY, ACCENT, WARN
     */
    private BadgeColor badgeColor;

    public enum MenuLocation {
        MAIN,
        SETTINGS,
        USER,
        ADMIN,
        TOOLBAR
    }

    public enum BadgeColor {
        PRIMARY,
        ACCENT,
        WARN,
        SUCCESS
    }
}
