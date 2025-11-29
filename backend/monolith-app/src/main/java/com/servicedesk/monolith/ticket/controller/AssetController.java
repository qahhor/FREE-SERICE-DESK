package com.servicedesk.monolith.ticket.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicedesk.monolith.ticket.dto.AssetDto;
import com.servicedesk.monolith.ticket.entity.*;
import com.servicedesk.monolith.ticket.service.AssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AssetController {

    private final AssetService assetService;
    private final ObjectMapper objectMapper;

    // ==================== Asset CRUD ====================

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<Page<AssetDto.ListResponse>> getAllAssets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Asset.AssetStatus status,
            @RequestParam(required = false) Asset.AssetType type,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID ownerId) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Page<Asset> assets;

        if (search != null && !search.isEmpty()) {
            assets = assetService.searchAssets(search, PageRequest.of(page, size, sort));
        } else if (status != null) {
            assets = assetService.getAssetsByStatus(status, PageRequest.of(page, size, sort));
        } else if (type != null) {
            assets = assetService.getAssetsByType(type, PageRequest.of(page, size, sort));
        } else if (categoryId != null) {
            assets = assetService.getAssetsByCategory(categoryId, PageRequest.of(page, size, sort));
        } else if (ownerId != null) {
            assets = assetService.getAssetsByOwner(ownerId, PageRequest.of(page, size, sort));
        } else {
            assets = assetService.getAllAssets(PageRequest.of(page, size, sort));
        }

        Page<AssetDto.ListResponse> response = assets.map(this::toListResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<AssetDto.Response> getAsset(@PathVariable UUID id) {
        return assetService.getAsset(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tag/{assetTag}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<AssetDto.Response> getAssetByTag(@PathVariable String assetTag) {
        return assetService.getAssetByTag(assetTag)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssetDto.Response> createAsset(
            @Valid @RequestBody AssetDto.CreateRequest request) {
        Asset asset = assetService.createAsset(request);
        return ResponseEntity.ok(toResponse(asset));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssetDto.Response> updateAsset(
            @PathVariable UUID id,
            @Valid @RequestBody AssetDto.UpdateRequest request) {
        Asset asset = assetService.updateAsset(id, request);
        return ResponseEntity.ok(toResponse(asset));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAsset(@PathVariable UUID id) {
        assetService.deleteAsset(id);
        return ResponseEntity.ok().build();
    }

    // ==================== Ticket Linking ====================

    @PostMapping("/{assetId}/tickets/{ticketId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<Void> linkTicket(
            @PathVariable UUID assetId,
            @PathVariable UUID ticketId) {
        assetService.linkAssetToTicket(assetId, ticketId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{assetId}/tickets/{ticketId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<Void> unlinkTicket(
            @PathVariable UUID assetId,
            @PathVariable UUID ticketId) {
        assetService.unlinkAssetFromTicket(assetId, ticketId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/ticket/{ticketId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<List<AssetDto.ListResponse>> getAssetsForTicket(@PathVariable UUID ticketId) {
        List<Asset> assets = assetService.getAssetsForTicket(ticketId);
        List<AssetDto.ListResponse> response = assets.stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // ==================== Maintenance ====================

    @GetMapping("/{assetId}/maintenance")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<List<AssetDto.MaintenanceResponse>> getMaintenanceHistory(
            @PathVariable UUID assetId) {
        List<AssetMaintenance> maintenance = assetService.getMaintenanceForAsset(assetId);
        List<AssetDto.MaintenanceResponse> response = maintenance.stream()
                .map(this::toMaintenanceResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{assetId}/maintenance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssetDto.MaintenanceResponse> scheduleMaintenance(
            @PathVariable UUID assetId,
            @Valid @RequestBody AssetDto.MaintenanceRequest request) {
        AssetMaintenance maintenance = assetService.scheduleMaintenance(assetId, request);
        return ResponseEntity.ok(toMaintenanceResponse(maintenance));
    }

    @PostMapping("/maintenance/{maintenanceId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<AssetDto.MaintenanceResponse> completeMaintenance(
            @PathVariable UUID maintenanceId,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) java.math.BigDecimal cost) {
        AssetMaintenance maintenance = assetService.completeMaintenance(maintenanceId, notes, cost);
        return ResponseEntity.ok(toMaintenanceResponse(maintenance));
    }

    @GetMapping("/maintenance/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<List<AssetDto.MaintenanceResponse>> getOverdueMaintenance() {
        List<AssetMaintenance> overdue = assetService.getOverdueMaintenance();
        List<AssetDto.MaintenanceResponse> response = overdue.stream()
                .map(this::toMaintenanceResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // ==================== History ====================

    @GetMapping("/{assetId}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<List<AssetDto.HistoryResponse>> getAssetHistory(@PathVariable UUID assetId) {
        return assetService.getAsset(assetId)
                .map(asset -> {
                    List<AssetDto.HistoryResponse> history = asset.getHistory().stream()
                            .map(this::toHistoryResponse)
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(history);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Categories ====================

    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<List<AssetDto.CategoryResponse>> getCategories() {
        List<AssetCategory> categories = assetService.getAllCategories();
        List<AssetDto.CategoryResponse> response = categories.stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/categories/tree")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<List<AssetDto.CategoryResponse>> getCategoryTree() {
        List<AssetCategory> rootCategories = assetService.getRootCategories();
        List<AssetDto.CategoryResponse> response = rootCategories.stream()
                .map(this::toCategoryResponseWithChildren)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssetDto.CategoryResponse> createCategory(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String icon,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) UUID parentId) {
        AssetCategory category = assetService.createCategory(name, description, icon, color, parentId);
        return ResponseEntity.ok(toCategoryResponse(category));
    }

    // ==================== Metrics ====================

    @GetMapping("/metrics")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<AssetDto.AssetMetrics> getMetrics() {
        return ResponseEntity.ok(assetService.getMetrics());
    }

    // ==================== Mappers ====================

    private AssetDto.Response toResponse(Asset asset) {
        Map<String, Object> customFields = null;
        if (asset.getCustomFields() != null) {
            try {
                customFields = objectMapper.readValue(asset.getCustomFields(),
                        new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                log.error("Failed to parse custom fields", e);
            }
        }

        return AssetDto.Response.builder()
                .id(asset.getId())
                .assetTag(asset.getAssetTag())
                .name(asset.getName())
                .description(asset.getDescription())
                .category(asset.getCategory() != null ? AssetDto.CategorySummary.builder()
                        .id(asset.getCategory().getId())
                        .name(asset.getCategory().getName())
                        .icon(asset.getCategory().getIcon())
                        .color(asset.getCategory().getColor())
                        .build() : null)
                .status(asset.getStatus())
                .type(asset.getType())
                .owner(asset.getOwner() != null ? AssetDto.UserSummary.builder()
                        .id(asset.getOwner().getId())
                        .email(asset.getOwner().getEmail())
                        .fullName(asset.getOwner().getFirstName() + " " + asset.getOwner().getLastName())
                        .build() : null)
                .department(asset.getDepartment())
                .location(asset.getLocation())
                .costCenter(asset.getCostCenter())
                .manufacturer(asset.getManufacturer())
                .model(asset.getModel())
                .serialNumber(asset.getSerialNumber())
                .version(asset.getVersion())
                .licenseKey(asset.getLicenseKey())
                .licenseType(asset.getLicenseType())
                .licenseExpiry(asset.getLicenseExpiry())
                .purchaseDate(asset.getPurchaseDate())
                .purchaseCost(asset.getPurchaseCost())
                .warrantyExpiry(asset.getWarrantyExpiry())
                .depreciationRate(asset.getDepreciationRate())
                .currentValue(asset.getCurrentValue())
                .ipAddress(asset.getIpAddress())
                .macAddress(asset.getMacAddress())
                .hostname(asset.getHostname())
                .customFields(customFields)
                .projectId(asset.getProject() != null ? asset.getProject().getId() : null)
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt())
                .warrantyActive(asset.getWarrantyExpiry() != null &&
                        asset.getWarrantyExpiry().isAfter(LocalDate.now()))
                .licenseActive(asset.getLicenseExpiry() != null &&
                        asset.getLicenseExpiry().isAfter(LocalDate.now()))
                .relatedTicketsCount(asset.getLinkedTickets() != null ?
                        asset.getLinkedTickets().size() : 0)
                .build();
    }

    private AssetDto.ListResponse toListResponse(Asset asset) {
        return AssetDto.ListResponse.builder()
                .id(asset.getId())
                .assetTag(asset.getAssetTag())
                .name(asset.getName())
                .categoryName(asset.getCategory() != null ? asset.getCategory().getName() : null)
                .status(asset.getStatus())
                .type(asset.getType())
                .ownerName(asset.getOwner() != null ?
                        asset.getOwner().getFirstName() + " " + asset.getOwner().getLastName() : null)
                .location(asset.getLocation())
                .warrantyExpiry(asset.getWarrantyExpiry())
                .createdAt(asset.getCreatedAt())
                .build();
    }

    private AssetDto.MaintenanceResponse toMaintenanceResponse(AssetMaintenance m) {
        return AssetDto.MaintenanceResponse.builder()
                .id(m.getId())
                .assetId(m.getAsset().getId())
                .assetName(m.getAsset().getName())
                .maintenanceType(m.getMaintenanceType())
                .scheduledDate(m.getScheduledDate())
                .completedDate(m.getCompletedDate())
                .status(m.getStatus())
                .assignedToId(m.getAssignedTo() != null ? m.getAssignedTo().getId() : null)
                .assignedToName(m.getAssignedTo() != null ?
                        m.getAssignedTo().getFirstName() + " " + m.getAssignedTo().getLastName() : null)
                .notes(m.getNotes())
                .cost(m.getCost())
                .createdAt(m.getCreatedAt())
                .build();
    }

    private AssetDto.HistoryResponse toHistoryResponse(AssetHistory h) {
        return AssetDto.HistoryResponse.builder()
                .id(h.getId())
                .action(h.getAction())
                .fieldName(h.getFieldName())
                .oldValue(h.getOldValue())
                .newValue(h.getNewValue())
                .description(h.getDescription())
                .userId(h.getUser() != null ? h.getUser().getId() : null)
                .userName(h.getUser() != null ?
                        h.getUser().getFirstName() + " " + h.getUser().getLastName() : null)
                .timestamp(h.getCreatedAt())
                .build();
    }

    private AssetDto.CategoryResponse toCategoryResponse(AssetCategory c) {
        return AssetDto.CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .icon(c.getIcon())
                .color(c.getColor())
                .parentId(c.getParent() != null ? c.getParent().getId() : null)
                .sortOrder(c.getSortOrder())
                .assetCount((long) c.getAssets().size())
                .build();
    }

    private AssetDto.CategoryResponse toCategoryResponseWithChildren(AssetCategory c) {
        AssetDto.CategoryResponse response = toCategoryResponse(c);
        if (c.getChildren() != null && !c.getChildren().isEmpty()) {
            response.setChildren(c.getChildren().stream()
                    .filter(child -> !Boolean.TRUE.equals(child.getDeleted()))
                    .map(this::toCategoryResponseWithChildren)
                    .collect(Collectors.toList()));
        }
        return response;
    }
}
