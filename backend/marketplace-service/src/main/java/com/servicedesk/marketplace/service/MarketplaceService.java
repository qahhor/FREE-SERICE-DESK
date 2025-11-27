package com.servicedesk.marketplace.service;

import com.servicedesk.marketplace.dto.ModuleDto;
import com.servicedesk.marketplace.dto.ModuleSearchRequest;
import com.servicedesk.marketplace.entity.MarketplaceModule;
import com.servicedesk.marketplace.entity.ModuleInstallation;
import com.servicedesk.marketplace.plugin.ModuleCategory;
import com.servicedesk.marketplace.repository.MarketplaceModuleRepository;
import com.servicedesk.marketplace.repository.ModuleInstallationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketplaceService {

    private final MarketplaceModuleRepository moduleRepository;
    private final ModuleInstallationRepository installationRepository;

    @Transactional(readOnly = true)
    public Page<ModuleDto> searchModules(ModuleSearchRequest request, UUID tenantId) {
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? Math.min(request.getSize(), 100) : 20;

        Sort sort = getSort(request.getSortBy(), request.getSortOrder());
        Pageable pageable = PageRequest.of(page, size, sort);

        ModuleCategory category = request.getCategories() != null && !request.getCategories().isEmpty()
                ? request.getCategories().get(0)
                : null;

        Page<MarketplaceModule> modules = moduleRepository.searchModules(
                request.getQuery(),
                category,
                Boolean.TRUE.equals(request.getFreeOnly()),
                Boolean.TRUE.equals(request.getVerifiedOnly()),
                request.getMinRating(),
                pageable
        );

        // Get tenant's installations to enrich DTOs
        Map<String, ModuleInstallation> installations = getInstallationMap(tenantId);

        return modules.map(module -> enrichWithInstallation(ModuleDto.fromEntity(module), installations));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "featured-modules", key = "#tenantId")
    public List<ModuleDto> getFeaturedModules(UUID tenantId) {
        List<MarketplaceModule> modules = moduleRepository.findFeaturedModules(PageRequest.of(0, 12));
        Map<String, ModuleInstallation> installations = getInstallationMap(tenantId);

        return modules.stream()
                .map(ModuleDto::fromEntity)
                .map(dto -> enrichWithInstallation(dto, installations))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ModuleDto> getNewestModules(UUID tenantId) {
        List<MarketplaceModule> modules = moduleRepository.findNewestModules(PageRequest.of(0, 12));
        Map<String, ModuleInstallation> installations = getInstallationMap(tenantId);

        return modules.stream()
                .map(ModuleDto::fromEntity)
                .map(dto -> enrichWithInstallation(dto, installations))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ModuleDto> getPopularModules(UUID tenantId) {
        List<MarketplaceModule> modules = moduleRepository.findPopularModules(PageRequest.of(0, 12));
        Map<String, ModuleInstallation> installations = getInstallationMap(tenantId);

        return modules.stream()
                .map(ModuleDto::fromEntity)
                .map(dto -> enrichWithInstallation(dto, installations))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ModuleDto> getModuleDetails(String moduleId, UUID tenantId) {
        return moduleRepository.findByModuleId(moduleId)
                .map(module -> {
                    ModuleDto dto = ModuleDto.fromEntity(module);
                    installationRepository.findByTenantIdAndModuleId(tenantId, moduleId)
                            .ifPresent(installation -> {
                                dto.setInstalled(true);
                                dto.setInstalledVersion(installation.getInstalledVersion());
                                dto.setEnabled(installation.isEnabled());
                                dto.setUpdateAvailable(!installation.getInstalledVersion().equals(module.getLatestVersion()));
                            });
                    return dto;
                });
    }

    @Transactional(readOnly = true)
    public Page<ModuleDto> getModulesByCategory(ModuleCategory category, UUID tenantId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MarketplaceModule> modules = moduleRepository.findByCategory(category, pageable);
        Map<String, ModuleInstallation> installations = getInstallationMap(tenantId);

        return modules.map(module -> enrichWithInstallation(ModuleDto.fromEntity(module), installations));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "official-modules")
    public List<ModuleDto> getOfficialModules() {
        return moduleRepository.findOfficialModules().stream()
                .map(ModuleDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "categories")
    public List<CategoryInfo> getCategories() {
        return moduleRepository.findActiveCategories().stream()
                .map(category -> new CategoryInfo(
                        category,
                        category.getDisplayName(),
                        category.getDescription(),
                        moduleRepository.countByCategory(category)
                ))
                .sorted(Comparator.comparing(CategoryInfo::moduleCount).reversed())
                .collect(Collectors.toList());
    }

    private Map<String, ModuleInstallation> getInstallationMap(UUID tenantId) {
        if (tenantId == null) {
            return Collections.emptyMap();
        }
        return installationRepository.findByTenantId(tenantId).stream()
                .collect(Collectors.toMap(ModuleInstallation::getModuleId, i -> i));
    }

    private ModuleDto enrichWithInstallation(ModuleDto dto, Map<String, ModuleInstallation> installations) {
        ModuleInstallation installation = installations.get(dto.getModuleId());
        if (installation != null) {
            dto.setInstalled(true);
            dto.setInstalledVersion(installation.getInstalledVersion());
            dto.setEnabled(installation.isEnabled());
            dto.setUpdateAvailable(!installation.getInstalledVersion().equals(dto.getLatestVersion()));
        }
        return dto;
    }

    private Sort getSort(ModuleSearchRequest.SortBy sortBy, ModuleSearchRequest.SortOrder sortOrder) {
        if (sortBy == null) {
            sortBy = ModuleSearchRequest.SortBy.POPULARITY;
        }
        Sort.Direction direction = sortOrder == ModuleSearchRequest.SortOrder.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return switch (sortBy) {
            case RELEVANCE -> Sort.by(direction, "installCount");
            case POPULARITY -> Sort.by(direction, "installCount");
            case RATING -> Sort.by(direction, "averageRating");
            case NEWEST -> Sort.by(direction, "publishedAt");
            case NAME -> Sort.by(direction, "name");
            case PRICE -> Sort.by(direction, "price");
        };
    }

    public record CategoryInfo(
            ModuleCategory category,
            String displayName,
            String description,
            long moduleCount
    ) {}
}
