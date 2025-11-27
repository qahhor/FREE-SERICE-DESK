package com.servicedesk.marketplace.controller;

import com.servicedesk.marketplace.dto.ModuleDto;
import com.servicedesk.marketplace.dto.ModuleSearchRequest;
import com.servicedesk.marketplace.plugin.ModuleCategory;
import com.servicedesk.marketplace.service.MarketplaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/marketplace")
@RequiredArgsConstructor
@Tag(name = "Marketplace", description = "Browse and discover modules")
public class MarketplaceController {

    private final MarketplaceService marketplaceService;

    @PostMapping("/search")
    @Operation(summary = "Search modules")
    public ResponseEntity<Page<ModuleDto>> searchModules(
            @RequestBody ModuleSearchRequest request,
            @RequestHeader(value = "X-Tenant-ID", required = false) UUID tenantId) {
        return ResponseEntity.ok(marketplaceService.searchModules(request, tenantId));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured modules")
    public ResponseEntity<List<ModuleDto>> getFeaturedModules(
            @RequestHeader(value = "X-Tenant-ID", required = false) UUID tenantId) {
        return ResponseEntity.ok(marketplaceService.getFeaturedModules(tenantId));
    }

    @GetMapping("/newest")
    @Operation(summary = "Get newest modules")
    public ResponseEntity<List<ModuleDto>> getNewestModules(
            @RequestHeader(value = "X-Tenant-ID", required = false) UUID tenantId) {
        return ResponseEntity.ok(marketplaceService.getNewestModules(tenantId));
    }

    @GetMapping("/popular")
    @Operation(summary = "Get popular modules")
    public ResponseEntity<List<ModuleDto>> getPopularModules(
            @RequestHeader(value = "X-Tenant-ID", required = false) UUID tenantId) {
        return ResponseEntity.ok(marketplaceService.getPopularModules(tenantId));
    }

    @GetMapping("/official")
    @Operation(summary = "Get official modules")
    public ResponseEntity<List<ModuleDto>> getOfficialModules() {
        return ResponseEntity.ok(marketplaceService.getOfficialModules());
    }

    @GetMapping("/modules/{moduleId}")
    @Operation(summary = "Get module details")
    public ResponseEntity<ModuleDto> getModuleDetails(
            @PathVariable String moduleId,
            @RequestHeader(value = "X-Tenant-ID", required = false) UUID tenantId) {
        return marketplaceService.getModuleDetails(moduleId, tenantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all categories with module counts")
    public ResponseEntity<List<MarketplaceService.CategoryInfo>> getCategories() {
        return ResponseEntity.ok(marketplaceService.getCategories());
    }

    @GetMapping("/categories/{category}")
    @Operation(summary = "Get modules by category")
    public ResponseEntity<Page<ModuleDto>> getModulesByCategory(
            @PathVariable ModuleCategory category,
            @RequestHeader(value = "X-Tenant-ID", required = false) UUID tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(marketplaceService.getModulesByCategory(category, tenantId, page, size));
    }
}
