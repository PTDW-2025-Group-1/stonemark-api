package pt.estga.file.services;

import pt.estga.file.entities.MediaFile;

import java.util.Optional;

/**
 * Service interface for managing the metadata of media files.
 * Handles database operations for MediaFile entities.
 */
public interface MediaMetadataService {

    /**
     * Saves the metadata of a media file.
     *
     * @param mediaFile the entity to save
     * @return the saved entity
     */
    MediaFile saveMetadata(MediaFile mediaFile);

    /**
     * Finds a media file entity by its ID.
     *
     * @param id the id of the media file
     * @return an Optional containing the MediaFile if found
     */
    Optional<MediaFile> findById(Long id);
}
