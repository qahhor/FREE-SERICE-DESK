package com.servicedesk.marketplace.plugin;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Storage service interface for modules to persist data
 */
public interface ModuleStorageService {

    /**
     * Store a JSON document
     */
    void storeDocument(String collection, String id, Map<String, Object> document);

    /**
     * Get a JSON document by ID
     */
    Optional<Map<String, Object>> getDocument(String collection, String id);

    /**
     * Delete a document
     */
    void deleteDocument(String collection, String id);

    /**
     * Find documents by query
     */
    List<Map<String, Object>> findDocuments(String collection, Map<String, Object> query);

    /**
     * Find documents with pagination
     */
    List<Map<String, Object>> findDocuments(String collection, Map<String, Object> query, int offset, int limit);

    /**
     * Count documents matching query
     */
    long countDocuments(String collection, Map<String, Object> query);

    /**
     * Store a file
     */
    String storeFile(String fileName, InputStream content, String contentType);

    /**
     * Get a file
     */
    Optional<InputStream> getFile(String fileId);

    /**
     * Delete a file
     */
    void deleteFile(String fileId);

    /**
     * Get file metadata
     */
    Optional<FileMetadata> getFileMetadata(String fileId);

    record FileMetadata(
            String id,
            String fileName,
            String contentType,
            long size,
            java.time.Instant createdAt
    ) {}
}
