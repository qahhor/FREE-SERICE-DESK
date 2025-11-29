package com.servicedesk.monolith.marketplace.controller;

import com.servicedesk.monolith.marketplace.dto.InstallModuleRequest;
import com.servicedesk.monolith.marketplace.dto.ModuleInstallationDto;
import com.servicedesk.monolith.marketplace.plugin.ModuleMenuItem;
import com.servicedesk.monolith.marketplace.plugin.ModuleWidget;
import com.servicedesk.monolith.marketplace.service.ModuleInstallationService;
import com.servicedesk.monolith.marketplace.service.ModuleLoaderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
@Tag(name = "Module Installation", description = "Install and manage modules")
public class ModuleInstallationController {

    private final ModuleInstallationService installationService;
    private final ModuleLoaderService loaderService;

    @PostMapping("/install")
    @Operation(summary = "Install a module")
    public ResponseEntity<ModuleInstallationDto> installModule(
            @Valid @RequestBody InstallModuleRequest request,
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestHeader("X-User-ID") UUID userId) {
        return ResponseEntity.ok(installationService.installModule(request, tenantId, userId));
    }

    @DeleteMapping("/{moduleId}")
    @Operation(summary = "Uninstall a module")
    public ResponseEntity<ModuleInstallationDto> uninstallModule(
            @PathVariable String moduleId,
            @RequestHeader("X-Tenant-ID") UUID tenantId) {
        return ResponseEntity.ok(installationService.uninstallModule(moduleId, tenantId));
    }

    @PostMapping("/{moduleId}/enable")
    @Operation(summary = "Enable a module")
    public ResponseEntity<ModuleInstallationDto> enableModule(
            @PathVariable String moduleId,
            @RequestHeader("X-Tenant-ID") UUID tenantId) {
        return ResponseEntity.ok(installationService.enableModule(moduleId, tenantId));
    }

    @PostMapping("/{moduleId}/disable")
    @Operation(summary = "Disable a module")
    public ResponseEntity<ModuleInstallationDto> disableModule(
            @PathVariable String moduleId,
            @RequestHeader("X-Tenant-ID") UUID tenantId) {
        return ResponseEntity.ok(installationService.disableModule(moduleId, tenantId));
    }

    @PutMapping("/{moduleId}/configuration")
    @Operation(summary = "Update module configuration")
    public ResponseEntity<ModuleInstallationDto> updateConfiguration(
            @PathVariable String moduleId,
            @RequestBody Map<String, Object> configuration,
            @RequestHeader("X-Tenant-ID") UUID tenantId) {
        return ResponseEntity.ok(installationService.updateConfiguration(moduleId, tenantId, configuration));
    }

    @PostMapping("/{moduleId}/update")
    @Operation(summary = "Update module to latest version")
    public ResponseEntity<ModuleInstallationDto> updateModule(
            @PathVariable String moduleId,
            @RequestParam(required = false) String version,
            @RequestHeader("X-Tenant-ID") UUID tenantId) {
        return ResponseEntity.ok(installationService.updateModule(moduleId, tenantId, version));
    }

    @GetMapping("/installed")
    @Operation(summary = "Get all installed modules")
    public ResponseEntity<List<ModuleInstallationDto>> getInstalledModules(
            @RequestHeader("X-Tenant-ID") UUID tenantId) {
        return ResponseEntity.ok(installationService.getInstalledModules(tenantId));
    }

    @GetMapping("/{moduleId}/installation")
    @Operation(summary = "Get installation details")
    public ResponseEntity<ModuleInstallationDto> getInstallationDetails(
            @PathVariable String moduleId,
            @RequestHeader("X-Tenant-ID") UUID tenantId) {
        return installationService.getInstallationDetails(moduleId, tenantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/widgets")
    @Operation(summary = "Get all widgets from installed modules")
    public ResponseEntity<List<ModuleWidget>> getWidgets(
            @RequestHeader("X-Tenant-ID") UUID tenantId) {
        return ResponseEntity.ok(loaderService.getWidgets(tenantId));
    }

    @GetMapping("/menu-items")
    @Operation(summary = "Get all menu items from installed modules")
    public ResponseEntity<List<ModuleMenuItem>> getMenuItems(
            @RequestHeader("X-Tenant-ID") UUID tenantId) {
        return ResponseEntity.ok(loaderService.getMenuItems(tenantId));
    }
}
