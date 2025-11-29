package com.servicedesk.monolith.marketplace.service;

import com.servicedesk.monolith.marketplace.dto.InstallModuleRequest;
import com.servicedesk.monolith.marketplace.dto.ModuleInstallationDto;
import com.servicedesk.monolith.marketplace.entity.MarketplaceModule;
import com.servicedesk.monolith.marketplace.entity.ModuleInstallation;
import com.servicedesk.monolith.marketplace.entity.ModuleVersion;
import com.servicedesk.monolith.marketplace.plugin.*;
import com.servicedesk.monolith.marketplace.repository.MarketplaceModuleRepository;
import com.servicedesk.monolith.marketplace.repository.ModuleInstallationRepository;
import com.servicedesk.monolith.marketplace.repository.ModuleVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModuleInstallationService {

    private final ModuleInstallationRepository installationRepository;
    private final MarketplaceModuleRepository moduleRepository;
    private final ModuleVersionRepository versionRepository;
    private final ModuleLoaderService moduleLoaderService;

    @Transactional
    public ModuleInstallationDto installModule(InstallModuleRequest request, UUID tenantId, UUID userId) {
        log.info("Installing module {} for tenant {}", request.getModuleId(), tenantId);

        // Check if already installed
        if (installationRepository.existsByTenantIdAndModuleId(tenantId, request.getModuleId())) {
            throw new IllegalStateException("Module is already installed");
        }

        // Get module info
        MarketplaceModule module = moduleRepository.findByModuleId(request.getModuleId())
                .orElseThrow(() -> new IllegalArgumentException("Module not found: " + request.getModuleId()));

        // Determine version to install
        String version = request.getVersion();
        if (version == null || version.isBlank()) {
            version = module.getLatestVersion();
        }

        // Validate version exists
        ModuleVersion moduleVersion = versionRepository.findByModuleIdAndVersion(request.getModuleId(), version)
                .orElseThrow(() -> new IllegalArgumentException("Version not found: " + version));

        // Check dependencies
        validateDependencies(module.getDependencies(), tenantId);

        // Check licensing
        boolean needsLicense = module.getPricingModel() != MarketplaceModule.PricingModel.FREE;
        Instant trialExpiresAt = null;

        if (needsLicense && request.getLicenseKey() == null && !request.isEnableTrial()) {
            if (module.getTrialDays() > 0) {
                throw new IllegalStateException("This module requires a license or trial activation");
            } else {
                throw new IllegalStateException("This module requires a license key");
            }
        }

        if (request.isEnableTrial() && module.getTrialDays() > 0) {
            trialExpiresAt = Instant.now().plus(module.getTrialDays(), ChronoUnit.DAYS);
        }

        // Create installation record
        Map<String, Object> configuration = request.getConfiguration();
        if (configuration == null) {
            configuration = new HashMap<>();
        }

        ModuleInstallation installation = ModuleInstallation.builder()
                .tenantId(tenantId)
                .moduleId(request.getModuleId())
                .installedVersion(version)
                .status(ModuleInstallation.InstallationStatus.INSTALLING)
                .configuration(configuration)
                .installedBy(userId)
                .enabled(true)
                .autoUpdate(request.isAutoUpdate())
                .licenseKey(request.getLicenseKey())
                .trialStartedAt(trialExpiresAt != null ? Instant.now() : null)
                .trialExpiresAt(trialExpiresAt)
                .build();

        installation = installationRepository.save(installation);

        try {
            // Load and initialize the module
            moduleLoaderService.loadModule(request.getModuleId(), version, tenantId, configuration);

            // Update status to active
            installation.setStatus(ModuleInstallation.InstallationStatus.ACTIVE);
            installation.setHealthStatus(ModuleInstallation.HealthStatus.HEALTHY);
            installation.setLastHealthCheck(Instant.now());
            installation = installationRepository.save(installation);

            // Update install count
            module.setInstallCount(module.getInstallCount() + 1);
            moduleRepository.save(module);

            log.info("Module {} installed successfully for tenant {}", request.getModuleId(), tenantId);

        } catch (Exception e) {
            log.error("Failed to install module {}: {}", request.getModuleId(), e.getMessage(), e);
            installation.setStatus(ModuleInstallation.InstallationStatus.FAILED);
            installation.setErrorMessage(e.getMessage());
            installation.setHealthStatus(ModuleInstallation.HealthStatus.UNHEALTHY);
            installationRepository.save(installation);
            throw new RuntimeException("Failed to install module: " + e.getMessage(), e);
        }

        return enrichInstallationDto(ModuleInstallationDto.fromEntity(installation), module);
    }

    @Transactional
    public ModuleInstallationDto uninstallModule(String moduleId, UUID tenantId) {
        log.info("Uninstalling module {} for tenant {}", moduleId, tenantId);

        ModuleInstallation installation = installationRepository.findByTenantIdAndModuleId(tenantId, moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not installed"));

        installation.setStatus(ModuleInstallation.InstallationStatus.UNINSTALLING);
        installationRepository.save(installation);

        try {
            // Unload the module
            moduleLoaderService.unloadModule(moduleId, tenantId);

            // Mark as uninstalled
            installation.setStatus(ModuleInstallation.InstallationStatus.UNINSTALLED);
            installation.setEnabled(false);
            installationRepository.save(installation);

            log.info("Module {} uninstalled for tenant {}", moduleId, tenantId);

        } catch (Exception e) {
            log.error("Failed to uninstall module {}: {}", moduleId, e.getMessage(), e);
            installation.setStatus(ModuleInstallation.InstallationStatus.ACTIVE);
            installation.setErrorMessage("Uninstall failed: " + e.getMessage());
            installationRepository.save(installation);
            throw new RuntimeException("Failed to uninstall module: " + e.getMessage(), e);
        }

        return ModuleInstallationDto.fromEntity(installation);
    }

    @Transactional
    public ModuleInstallationDto enableModule(String moduleId, UUID tenantId) {
        ModuleInstallation installation = getInstallation(moduleId, tenantId);

        if (installation.isEnabled()) {
            return ModuleInstallationDto.fromEntity(installation);
        }

        moduleLoaderService.enableModule(moduleId, tenantId, installation.getConfiguration());

        installation.setEnabled(true);
        installation = installationRepository.save(installation);

        log.info("Module {} enabled for tenant {}", moduleId, tenantId);
        return ModuleInstallationDto.fromEntity(installation);
    }

    @Transactional
    public ModuleInstallationDto disableModule(String moduleId, UUID tenantId) {
        ModuleInstallation installation = getInstallation(moduleId, tenantId);

        if (!installation.isEnabled()) {
            return ModuleInstallationDto.fromEntity(installation);
        }

        moduleLoaderService.disableModule(moduleId, tenantId);

        installation.setEnabled(false);
        installation = installationRepository.save(installation);

        log.info("Module {} disabled for tenant {}", moduleId, tenantId);
        return ModuleInstallationDto.fromEntity(installation);
    }

    @Transactional
    public ModuleInstallationDto updateConfiguration(String moduleId, UUID tenantId, Map<String, Object> newConfig) {
        ModuleInstallation installation = getInstallation(moduleId, tenantId);

        Map<String, Object> oldConfig = installation.getConfiguration();
        installation.setConfiguration(newConfig);
        installation = installationRepository.save(installation);

        // Notify module of config change
        if (installation.isEnabled()) {
            moduleLoaderService.updateConfiguration(moduleId, tenantId, oldConfig, newConfig);
        }

        log.info("Configuration updated for module {} tenant {}", moduleId, tenantId);
        return ModuleInstallationDto.fromEntity(installation);
    }

    @Transactional
    public ModuleInstallationDto updateModule(String moduleId, UUID tenantId, String targetVersion) {
        ModuleInstallation installation = getInstallation(moduleId, tenantId);
        MarketplaceModule module = moduleRepository.findByModuleId(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found"));

        String newVersion = targetVersion != null ? targetVersion : module.getLatestVersion();

        if (installation.getInstalledVersion().equals(newVersion)) {
            return enrichInstallationDto(ModuleInstallationDto.fromEntity(installation), module);
        }

        installation.setStatus(ModuleInstallation.InstallationStatus.UPDATING);
        installationRepository.save(installation);

        try {
            moduleLoaderService.updateModule(moduleId, installation.getInstalledVersion(), newVersion, tenantId);

            installation.setInstalledVersion(newVersion);
            installation.setStatus(ModuleInstallation.InstallationStatus.ACTIVE);
            installation = installationRepository.save(installation);

            log.info("Module {} updated to version {} for tenant {}", moduleId, newVersion, tenantId);

        } catch (Exception e) {
            log.error("Failed to update module {}: {}", moduleId, e.getMessage(), e);
            installation.setStatus(ModuleInstallation.InstallationStatus.ACTIVE);
            installation.setErrorMessage("Update failed: " + e.getMessage());
            installationRepository.save(installation);
            throw new RuntimeException("Failed to update module: " + e.getMessage(), e);
        }

        return enrichInstallationDto(ModuleInstallationDto.fromEntity(installation), module);
    }

    @Transactional(readOnly = true)
    public List<ModuleInstallationDto> getInstalledModules(UUID tenantId) {
        return installationRepository.findByTenantId(tenantId).stream()
                .filter(i -> i.getStatus() != ModuleInstallation.InstallationStatus.UNINSTALLED)
                .map(installation -> {
                    ModuleInstallationDto dto = ModuleInstallationDto.fromEntity(installation);
                    moduleRepository.findByModuleId(installation.getModuleId())
                            .ifPresent(module -> enrichInstallationDto(dto, module));
                    return dto;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ModuleInstallationDto> getInstallationDetails(String moduleId, UUID tenantId) {
        return installationRepository.findByTenantIdAndModuleId(tenantId, moduleId)
                .map(installation -> {
                    ModuleInstallationDto dto = ModuleInstallationDto.fromEntity(installation);
                    moduleRepository.findByModuleId(moduleId)
                            .ifPresent(module -> enrichInstallationDto(dto, module));
                    return dto;
                });
    }

    private ModuleInstallation getInstallation(String moduleId, UUID tenantId) {
        return installationRepository.findByTenantIdAndModuleId(tenantId, moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not installed: " + moduleId));
    }

    private void validateDependencies(List<String> dependencies, UUID tenantId) {
        if (dependencies == null || dependencies.isEmpty()) {
            return;
        }

        List<String> missingDependencies = dependencies.stream()
                .filter(dep -> !installationRepository.existsByTenantIdAndModuleId(tenantId, dep))
                .toList();

        if (!missingDependencies.isEmpty()) {
            throw new IllegalStateException("Missing required modules: " + String.join(", ", missingDependencies));
        }
    }

    private ModuleInstallationDto enrichInstallationDto(ModuleInstallationDto dto, MarketplaceModule module) {
        dto.setModuleName(module.getName());
        dto.setModuleIcon(module.getIcon());
        dto.setLatestVersion(module.getLatestVersion());
        dto.setUpdateAvailable(!dto.getInstalledVersion().equals(module.getLatestVersion()));
        return dto;
    }
}
