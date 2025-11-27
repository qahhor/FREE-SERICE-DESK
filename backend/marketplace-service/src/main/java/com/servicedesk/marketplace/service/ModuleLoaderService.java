package com.servicedesk.marketplace.service;

import com.servicedesk.marketplace.plugin.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for loading, managing, and executing module lifecycle
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModuleLoaderService {

    private final ApplicationContext applicationContext;
    private final ModuleServiceLocatorImpl serviceLocator;

    @Value("${marketplace.modules.path:./modules}")
    private String modulesPath;

    // Cache of loaded modules per tenant: Map<tenantId, Map<moduleId, ModulePlugin>>
    private final Map<UUID, Map<String, ModulePlugin>> loadedModules = new ConcurrentHashMap<>();

    // Registry of available module plugins
    private final Map<String, Class<? extends ModulePlugin>> moduleRegistry = new ConcurrentHashMap<>();

    /**
     * Register a module plugin class
     */
    public void registerModule(String moduleId, Class<? extends ModulePlugin> moduleClass) {
        moduleRegistry.put(moduleId, moduleClass);
        log.info("Registered module: {}", moduleId);
    }

    /**
     * Load and initialize a module for a tenant
     */
    public void loadModule(String moduleId, String version, UUID tenantId, Map<String, Object> configuration) {
        log.info("Loading module {} v{} for tenant {}", moduleId, version, tenantId);

        Class<? extends ModulePlugin> moduleClass = moduleRegistry.get(moduleId);
        if (moduleClass == null) {
            // Try to load from Spring context (for built-in modules)
            try {
                ModulePlugin plugin = applicationContext.getBean(moduleId, ModulePlugin.class);
                initializeModule(plugin, tenantId, configuration);
                return;
            } catch (Exception e) {
                throw new IllegalArgumentException("Module not found in registry: " + moduleId);
            }
        }

        try {
            ModulePlugin plugin = moduleClass.getDeclaredConstructor().newInstance();
            initializeModule(plugin, tenantId, configuration);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate module: " + moduleId, e);
        }
    }

    private void initializeModule(ModulePlugin plugin, UUID tenantId, Map<String, Object> configuration) {
        String moduleId = plugin.getModuleId();

        // Create context
        ModuleContext context = ModuleContext.builder()
                .tenantId(tenantId)
                .configuration(configuration)
                .installationId(UUID.randomUUID())
                .dataPath(modulesPath + "/" + moduleId + "/" + tenantId)
                .baseUrl("/api/modules/" + moduleId)
                .serviceLocator(serviceLocator)
                .build();

        // Call install lifecycle hook
        plugin.onInstall(context);

        // Call enable lifecycle hook
        plugin.onEnable(context);

        // Store in loaded modules map
        loadedModules.computeIfAbsent(tenantId, k -> new ConcurrentHashMap<>())
                .put(moduleId, plugin);

        log.info("Module {} initialized for tenant {}", moduleId, tenantId);
    }

    /**
     * Unload a module for a tenant
     */
    public void unloadModule(String moduleId, UUID tenantId) {
        log.info("Unloading module {} for tenant {}", moduleId, tenantId);

        Map<String, ModulePlugin> tenantModules = loadedModules.get(tenantId);
        if (tenantModules == null) {
            return;
        }

        ModulePlugin plugin = tenantModules.remove(moduleId);
        if (plugin != null) {
            ModuleContext context = createContext(plugin, tenantId, Map.of());
            plugin.onDisable(context);
            plugin.onUninstall(context);
        }
    }

    /**
     * Enable a module for a tenant
     */
    public void enableModule(String moduleId, UUID tenantId, Map<String, Object> configuration) {
        ModulePlugin plugin = getLoadedModule(moduleId, tenantId);
        if (plugin == null) {
            loadModule(moduleId, null, tenantId, configuration);
        } else {
            ModuleContext context = createContext(plugin, tenantId, configuration);
            plugin.onEnable(context);
        }
    }

    /**
     * Disable a module for a tenant
     */
    public void disableModule(String moduleId, UUID tenantId) {
        ModulePlugin plugin = getLoadedModule(moduleId, tenantId);
        if (plugin != null) {
            ModuleContext context = createContext(plugin, tenantId, Map.of());
            plugin.onDisable(context);
        }
    }

    /**
     * Update module configuration
     */
    public void updateConfiguration(String moduleId, UUID tenantId,
                                    Map<String, Object> oldConfig, Map<String, Object> newConfig) {
        ModulePlugin plugin = getLoadedModule(moduleId, tenantId);
        if (plugin != null) {
            ModuleContext context = createContext(plugin, tenantId, newConfig);
            plugin.onConfigurationChange(context, oldConfig, newConfig);
        }
    }

    /**
     * Update a module to a new version
     */
    public void updateModule(String moduleId, String fromVersion, String toVersion, UUID tenantId) {
        log.info("Updating module {} from {} to {} for tenant {}", moduleId, fromVersion, toVersion, tenantId);

        // Get current configuration
        Map<String, Object> config = Map.of();
        ModulePlugin currentPlugin = getLoadedModule(moduleId, tenantId);
        if (currentPlugin != null) {
            // Disable old version
            disableModule(moduleId, tenantId);
        }

        // Load new version
        loadModule(moduleId, toVersion, tenantId, config);
    }

    /**
     * Execute a module endpoint
     */
    public ModuleResponse executeEndpoint(String moduleId, UUID tenantId, ModuleRequest request) {
        ModulePlugin plugin = getLoadedModule(moduleId, tenantId);
        if (plugin == null) {
            return ModuleResponse.notFound("Module not loaded: " + moduleId);
        }

        // Find matching endpoint
        Optional<ModuleEndpoint> endpoint = plugin.getEndpoints().stream()
                .filter(e -> e.getMethod().equalsIgnoreCase(request.getMethod()))
                .filter(e -> matchesPath(e.getPath(), request.getPath()))
                .findFirst();

        if (endpoint.isEmpty()) {
            return ModuleResponse.notFound("Endpoint not found");
        }

        try {
            return endpoint.get().getHandler().apply(request);
        } catch (Exception e) {
            log.error("Error executing module endpoint: {}", e.getMessage(), e);
            return ModuleResponse.error(500, "Internal module error: " + e.getMessage());
        }
    }

    /**
     * Get health status for a module
     */
    public ModuleHealth getModuleHealth(String moduleId, UUID tenantId) {
        ModulePlugin plugin = getLoadedModule(moduleId, tenantId);
        if (plugin == null) {
            return ModuleHealth.unhealthy("Module not loaded");
        }
        return plugin.getHealth();
    }

    /**
     * Get all widgets from loaded modules
     */
    public List<ModuleWidget> getWidgets(UUID tenantId) {
        Map<String, ModulePlugin> tenantModules = loadedModules.get(tenantId);
        if (tenantModules == null) {
            return List.of();
        }

        return tenantModules.values().stream()
                .flatMap(plugin -> plugin.getWidgets().stream())
                .toList();
    }

    /**
     * Get all menu items from loaded modules
     */
    public List<ModuleMenuItem> getMenuItems(UUID tenantId) {
        Map<String, ModulePlugin> tenantModules = loadedModules.get(tenantId);
        if (tenantModules == null) {
            return List.of();
        }

        return tenantModules.values().stream()
                .flatMap(plugin -> plugin.getMenuItems().stream())
                .toList();
    }

    /**
     * Get loaded module for tenant
     */
    public ModulePlugin getLoadedModule(String moduleId, UUID tenantId) {
        Map<String, ModulePlugin> tenantModules = loadedModules.get(tenantId);
        return tenantModules != null ? tenantModules.get(moduleId) : null;
    }

    /**
     * Get all loaded modules for tenant
     */
    public Collection<ModulePlugin> getLoadedModules(UUID tenantId) {
        Map<String, ModulePlugin> tenantModules = loadedModules.get(tenantId);
        return tenantModules != null ? tenantModules.values() : List.of();
    }

    private ModuleContext createContext(ModulePlugin plugin, UUID tenantId, Map<String, Object> configuration) {
        return ModuleContext.builder()
                .tenantId(tenantId)
                .configuration(configuration)
                .dataPath(modulesPath + "/" + plugin.getModuleId() + "/" + tenantId)
                .baseUrl("/api/modules/" + plugin.getModuleId())
                .serviceLocator(serviceLocator)
                .build();
    }

    private boolean matchesPath(String pattern, String path) {
        // Simple path matching (supports {param} placeholders)
        String regex = pattern.replaceAll("\\{[^}]+\\}", "[^/]+");
        return path.matches(regex);
    }
}
