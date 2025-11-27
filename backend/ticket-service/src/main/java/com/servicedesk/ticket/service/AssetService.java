package com.servicedesk.ticket.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicedesk.ticket.dto.AssetDto;
import com.servicedesk.ticket.entity.*;
import com.servicedesk.ticket.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetCategoryRepository categoryRepository;
    private final AssetMaintenanceRepository maintenanceRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    private static final AtomicLong assetCounter = new AtomicLong(System.currentTimeMillis() % 10000);

    // ==================== Asset CRUD ====================

    @Transactional
    public Asset createAsset(AssetDto.CreateRequest request) {
        Asset asset = Asset.builder()
                .assetTag(request.getAssetTag() != null ? request.getAssetTag() : generateAssetTag(request.getType()))
                .name(request.getName())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : Asset.AssetStatus.ACTIVE)
                .type(request.getType() != null ? request.getType() : Asset.AssetType.HARDWARE)
                .department(request.getDepartment())
                .location(request.getLocation())
                .costCenter(request.getCostCenter())
                .manufacturer(request.getManufacturer())
                .model(request.getModel())
                .serialNumber(request.getSerialNumber())
                .version(request.getVersion())
                .licenseKey(request.getLicenseKey())
                .licenseType(request.getLicenseType())
                .licenseExpiry(request.getLicenseExpiry())
                .purchaseDate(request.getPurchaseDate())
                .purchaseCost(request.getPurchaseCost())
                .warrantyExpiry(request.getWarrantyExpiry())
                .depreciationRate(request.getDepreciationRate())
                .currentValue(request.getPurchaseCost()) // Initial value = purchase cost
                .ipAddress(request.getIpAddress())
                .macAddress(request.getMacAddress())
                .hostname(request.getHostname())
                .build();

        // Set category
        if (request.getCategoryId() != null) {
            categoryRepository.findById(UUID.fromString(request.getCategoryId()))
                    .ifPresent(asset::setCategory);
        }

        // Set owner
        if (request.getOwnerId() != null) {
            userRepository.findById(UUID.fromString(request.getOwnerId()))
                    .ifPresent(asset::setOwner);
        }

        // Set custom fields
        if (request.getCustomFields() != null) {
            try {
                asset.setCustomFields(objectMapper.writeValueAsString(request.getCustomFields()));
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize custom fields", e);
            }
        }

        Asset saved = assetRepository.save(asset);

        // Add history entry
        addHistoryEntry(saved, "CREATED", null, null, null, "Asset created");

        log.info("Created asset: {} ({})", saved.getName(), saved.getAssetTag());
        return saved;
    }

    @Transactional
    public Asset updateAsset(UUID id, AssetDto.UpdateRequest request) {
        return assetRepository.findByIdAndDeletedFalse(id)
                .map(asset -> {
                    // Track changes for history
                    Map<String, String[]> changes = new HashMap<>();

                    if (request.getName() != null && !request.getName().equals(asset.getName())) {
                        changes.put("name", new String[]{asset.getName(), request.getName()});
                        asset.setName(request.getName());
                    }
                    if (request.getDescription() != null) {
                        asset.setDescription(request.getDescription());
                    }
                    if (request.getStatus() != null && request.getStatus() != asset.getStatus()) {
                        changes.put("status", new String[]{asset.getStatus().name(), request.getStatus().name()});
                        asset.setStatus(request.getStatus());
                    }
                    if (request.getType() != null) {
                        asset.setType(request.getType());
                    }
                    if (request.getDepartment() != null) {
                        asset.setDepartment(request.getDepartment());
                    }
                    if (request.getLocation() != null && !request.getLocation().equals(asset.getLocation())) {
                        changes.put("location", new String[]{asset.getLocation(), request.getLocation()});
                        asset.setLocation(request.getLocation());
                    }
                    if (request.getCostCenter() != null) {
                        asset.setCostCenter(request.getCostCenter());
                    }
                    if (request.getManufacturer() != null) {
                        asset.setManufacturer(request.getManufacturer());
                    }
                    if (request.getModel() != null) {
                        asset.setModel(request.getModel());
                    }
                    if (request.getSerialNumber() != null) {
                        asset.setSerialNumber(request.getSerialNumber());
                    }
                    if (request.getVersion() != null) {
                        asset.setVersion(request.getVersion());
                    }
                    if (request.getLicenseKey() != null) {
                        asset.setLicenseKey(request.getLicenseKey());
                    }
                    if (request.getLicenseType() != null) {
                        asset.setLicenseType(request.getLicenseType());
                    }
                    if (request.getLicenseExpiry() != null) {
                        asset.setLicenseExpiry(request.getLicenseExpiry());
                    }
                    if (request.getPurchaseDate() != null) {
                        asset.setPurchaseDate(request.getPurchaseDate());
                    }
                    if (request.getPurchaseCost() != null) {
                        asset.setPurchaseCost(request.getPurchaseCost());
                    }
                    if (request.getWarrantyExpiry() != null) {
                        asset.setWarrantyExpiry(request.getWarrantyExpiry());
                    }
                    if (request.getDepreciationRate() != null) {
                        asset.setDepreciationRate(request.getDepreciationRate());
                    }
                    if (request.getCurrentValue() != null) {
                        asset.setCurrentValue(request.getCurrentValue());
                    }
                    if (request.getIpAddress() != null) {
                        asset.setIpAddress(request.getIpAddress());
                    }
                    if (request.getMacAddress() != null) {
                        asset.setMacAddress(request.getMacAddress());
                    }
                    if (request.getHostname() != null) {
                        asset.setHostname(request.getHostname());
                    }

                    // Update category
                    if (request.getCategoryId() != null) {
                        categoryRepository.findById(UUID.fromString(request.getCategoryId()))
                                .ifPresent(asset::setCategory);
                    }

                    // Update owner
                    if (request.getOwnerId() != null) {
                        String oldOwner = asset.getOwner() != null ? asset.getOwner().getEmail() : null;
                        userRepository.findById(UUID.fromString(request.getOwnerId()))
                                .ifPresent(newOwner -> {
                                    changes.put("owner", new String[]{oldOwner, newOwner.getEmail()});
                                    asset.setOwner(newOwner);
                                });
                    }

                    // Update custom fields
                    if (request.getCustomFields() != null) {
                        try {
                            asset.setCustomFields(objectMapper.writeValueAsString(request.getCustomFields()));
                        } catch (JsonProcessingException e) {
                            log.error("Failed to serialize custom fields", e);
                        }
                    }

                    // Record history for significant changes
                    for (Map.Entry<String, String[]> change : changes.entrySet()) {
                        addHistoryEntry(asset, "UPDATED", change.getKey(),
                                change.getValue()[0], change.getValue()[1], null);
                    }

                    return assetRepository.save(asset);
                })
                .orElseThrow(() -> new RuntimeException("Asset not found: " + id));
    }

    @Transactional
    public void deleteAsset(UUID id) {
        assetRepository.findByIdAndDeletedFalse(id).ifPresent(asset -> {
            asset.setDeleted(true);
            addHistoryEntry(asset, "DELETED", null, null, null, "Asset deleted");
            assetRepository.save(asset);
            log.info("Deleted asset: {} ({})", asset.getName(), asset.getAssetTag());
        });
    }

    public Optional<Asset> getAsset(UUID id) {
        return assetRepository.findByIdAndDeletedFalse(id);
    }

    public Optional<Asset> getAssetByTag(String assetTag) {
        return assetRepository.findByAssetTagAndDeletedFalse(assetTag);
    }

    public Page<Asset> getAllAssets(Pageable pageable) {
        return assetRepository.findAllActive(pageable);
    }

    public Page<Asset> searchAssets(String query, Pageable pageable) {
        return assetRepository.search(query, pageable);
    }

    public Page<Asset> getAssetsByStatus(Asset.AssetStatus status, Pageable pageable) {
        return assetRepository.findByStatus(status, pageable);
    }

    public Page<Asset> getAssetsByType(Asset.AssetType type, Pageable pageable) {
        return assetRepository.findByType(type, pageable);
    }

    public Page<Asset> getAssetsByCategory(UUID categoryId, Pageable pageable) {
        return assetRepository.findByCategoryId(categoryId, pageable);
    }

    public Page<Asset> getAssetsByOwner(UUID ownerId, Pageable pageable) {
        return assetRepository.findByOwnerId(ownerId, pageable);
    }

    // ==================== Ticket Linking ====================

    @Transactional
    public void linkAssetToTicket(UUID assetId, UUID ticketId) {
        Asset asset = assetRepository.findByIdAndDeletedFalse(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found: " + assetId));

        Ticket ticket = ticketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));

        asset.getLinkedTickets().add(ticket);
        assetRepository.save(asset);

        addHistoryEntry(asset, "TICKET_LINKED", null, null, ticket.getTicketNumber(),
                "Linked to ticket " + ticket.getTicketNumber());

        log.info("Linked asset {} to ticket {}", asset.getAssetTag(), ticket.getTicketNumber());
    }

    @Transactional
    public void unlinkAssetFromTicket(UUID assetId, UUID ticketId) {
        Asset asset = assetRepository.findByIdAndDeletedFalse(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found: " + assetId));

        asset.getLinkedTickets().removeIf(t -> t.getId().equals(ticketId.toString()));
        assetRepository.save(asset);

        addHistoryEntry(asset, "TICKET_UNLINKED", null, null, null,
                "Unlinked from ticket " + ticketId);
    }

    public List<Asset> getAssetsForTicket(UUID ticketId) {
        return assetRepository.findByTicketId(ticketId);
    }

    // ==================== Maintenance ====================

    @Transactional
    public AssetMaintenance scheduleMaintenance(UUID assetId, AssetDto.MaintenanceRequest request) {
        Asset asset = assetRepository.findByIdAndDeletedFalse(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found: " + assetId));

        AssetMaintenance maintenance = AssetMaintenance.builder()
                .asset(asset)
                .maintenanceType(request.getMaintenanceType())
                .scheduledDate(request.getScheduledDate())
                .status(AssetMaintenance.MaintenanceStatus.SCHEDULED)
                .notes(request.getNotes())
                .cost(request.getCost())
                .build();

        if (request.getAssignedToId() != null) {
            userRepository.findById(UUID.fromString(request.getAssignedToId()))
                    .ifPresent(maintenance::setAssignedTo);
        }

        AssetMaintenance saved = maintenanceRepository.save(maintenance);

        addHistoryEntry(asset, "MAINTENANCE_SCHEDULED", null, null,
                request.getMaintenanceType().name(),
                "Maintenance scheduled for " + request.getScheduledDate());

        return saved;
    }

    @Transactional
    public AssetMaintenance completeMaintenance(UUID maintenanceId, String notes, BigDecimal cost) {
        return maintenanceRepository.findById(maintenanceId)
                .map(m -> {
                    m.setStatus(AssetMaintenance.MaintenanceStatus.COMPLETED);
                    m.setCompletedDate(LocalDate.now());
                    if (notes != null) m.setNotes(notes);
                    if (cost != null) m.setCost(cost);

                    addHistoryEntry(m.getAsset(), "MAINTENANCE_COMPLETED", null, null,
                            m.getMaintenanceType().name(),
                            "Maintenance completed");

                    return maintenanceRepository.save(m);
                })
                .orElseThrow(() -> new RuntimeException("Maintenance not found: " + maintenanceId));
    }

    public List<AssetMaintenance> getMaintenanceForAsset(UUID assetId) {
        return maintenanceRepository.findByAssetId(assetId);
    }

    public List<AssetMaintenance> getOverdueMaintenance() {
        return maintenanceRepository.findOverdueMaintenance(LocalDate.now());
    }

    // ==================== Metrics ====================

    public AssetDto.AssetMetrics getMetrics() {
        long totalAssets = assetRepository.count();
        long activeAssets = assetRepository.countByStatus(Asset.AssetStatus.ACTIVE);
        long inMaintenance = assetRepository.countByStatus(Asset.AssetStatus.IN_MAINTENANCE);
        long retired = assetRepository.countByStatus(Asset.AssetStatus.RETIRED);

        BigDecimal totalPurchaseCost = assetRepository.getTotalPurchaseCost();
        BigDecimal totalCurrentValue = assetRepository.getTotalCurrentValue();

        List<Asset> warrantyExpiring = assetRepository.findWithWarrantyExpiringBefore(
                LocalDate.now().plusDays(30));
        List<Asset> licenseExpiring = assetRepository.findWithLicenseExpiringBefore(
                LocalDate.now().plusDays(30));
        List<AssetMaintenance> overdueMaintenance = getOverdueMaintenance();

        // Count by type
        Map<String, Long> byType = new HashMap<>();
        for (Asset.AssetType type : Asset.AssetType.values()) {
            byType.put(type.name(), assetRepository.countByType(type));
        }

        // Count by status
        Map<String, Long> byStatus = new HashMap<>();
        for (Asset.AssetStatus status : Asset.AssetStatus.values()) {
            byStatus.put(status.name(), assetRepository.countByStatus(status));
        }

        // Count by category
        Map<String, Long> byCategory = new HashMap<>();
        categoryRepository.findAllActive().forEach(cat -> {
            byCategory.put(cat.getName(), assetRepository.countByCategoryId(UUID.fromString(cat.getId())));
        });

        return AssetDto.AssetMetrics.builder()
                .totalAssets(totalAssets)
                .activeAssets(activeAssets)
                .inMaintenanceAssets(inMaintenance)
                .retiredAssets(retired)
                .totalPurchaseCost(totalPurchaseCost)
                .totalCurrentValue(totalCurrentValue)
                .depreciation(totalPurchaseCost.subtract(totalCurrentValue))
                .warrantyExpiringIn30Days((long) warrantyExpiring.size())
                .licenseExpiringIn30Days((long) licenseExpiring.size())
                .overdueMaintenance((long) overdueMaintenance.size())
                .byType(byType)
                .byStatus(byStatus)
                .byCategory(byCategory)
                .build();
    }

    // ==================== Categories ====================

    public List<AssetCategory> getAllCategories() {
        return categoryRepository.findAllActive();
    }

    public List<AssetCategory> getRootCategories() {
        return categoryRepository.findRootCategories();
    }

    @Transactional
    public AssetCategory createCategory(String name, String description, String icon, String color, UUID parentId) {
        AssetCategory category = AssetCategory.builder()
                .name(name)
                .description(description)
                .icon(icon)
                .color(color)
                .build();

        if (parentId != null) {
            categoryRepository.findById(parentId).ifPresent(category::setParent);
        }

        return categoryRepository.save(category);
    }

    // ==================== Helpers ====================

    private String generateAssetTag(Asset.AssetType type) {
        String prefix = switch (type) {
            case HARDWARE -> "HW";
            case SOFTWARE -> "SW";
            case NETWORK -> "NW";
            case PERIPHERAL -> "PR";
            case MOBILE_DEVICE -> "MD";
            case SERVER -> "SV";
            case STORAGE -> "ST";
            case PRINTER -> "PT";
            case VIRTUAL_MACHINE -> "VM";
            case CLOUD_RESOURCE -> "CR";
            default -> "AS";
        };

        return String.format("%s-%06d", prefix, assetCounter.incrementAndGet());
    }

    private void addHistoryEntry(Asset asset, String action, String fieldName,
                                 String oldValue, String newValue, String description) {
        AssetHistory history = AssetHistory.builder()
                .action(action)
                .fieldName(fieldName)
                .oldValue(oldValue)
                .newValue(newValue)
                .description(description)
                .build();
        asset.addHistoryEntry(history);
    }
}
