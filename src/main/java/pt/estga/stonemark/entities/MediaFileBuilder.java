package pt.estga.stonemark.entities;

import pt.estga.stonemark.enums.StorageProvider;
import pt.estga.stonemark.enums.TargetType;

public class MediaFileBuilder {
    private Long id;
    private String fileName;
    private String originalFileName;
    private String contentType;
    private Long size;
    private StorageProvider storageProvider;
    private String storagePath;
    private String providerPublicId;
    private TargetType targetType;
    private Long targetId;
    private boolean primaryImage;
    private int sortOrder;

    public MediaFileBuilder setId(Long id) {
        this.id = id;
        return this;
    }

    public MediaFileBuilder setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public MediaFileBuilder setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
        return this;
    }

    public MediaFileBuilder setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public MediaFileBuilder setSize(Long size) {
        this.size = size;
        return this;
    }

    public MediaFileBuilder setStorageProvider(StorageProvider storageProvider) {
        this.storageProvider = storageProvider;
        return this;
    }

    public MediaFileBuilder setStoragePath(String storagePath) {
        this.storagePath = storagePath;
        return this;
    }

    public MediaFileBuilder setProviderPublicId(String providerPublicId) {
        this.providerPublicId = providerPublicId;
        return this;
    }

    public MediaFileBuilder setTargetType(TargetType targetType) {
        this.targetType = targetType;
        return this;
    }

    public MediaFileBuilder setTargetId(Long targetId) {
        this.targetId = targetId;
        return this;
    }

    public MediaFileBuilder setPrimaryImage(boolean primaryImage) {
        this.primaryImage = primaryImage;
        return this;
    }

    public MediaFileBuilder setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    public MediaFile createMediaFile() {
        return new MediaFile(id, fileName, originalFileName, contentType, size, storageProvider, storagePath, providerPublicId, targetType, targetId, primaryImage, sortOrder);
    }
}