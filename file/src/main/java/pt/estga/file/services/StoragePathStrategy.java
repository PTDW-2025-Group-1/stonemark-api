package pt.estga.file.services;

import pt.estga.file.entities.MediaFile;

/**
 * Strategy interface for generating storage paths for media files.
 * Allows for different directory structures (e.g., by date, by type, flat).
 */
public interface StoragePathStrategy {

    /**
     * Generates a storage path for the given media file.
     *
     * @param mediaFile The media file entity (containing ID, filename, etc.)
     * @return The relative storage path (e.g., "2023/10/15/stonemark-123.jpg")
     */
    String generatePath(MediaFile mediaFile);
}
