package pt.estga.file.services;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Service interface for managing the physical content of media files.
 * Handles storage, retrieval, and processing of file bytes.
 */
public interface MediaContentService {

    /**
     * Saves the content of a media file to the storage provider.
     *
     * @param fileStream the input stream of the file content
     * @param filename   the target filename (including path)
     * @return the storage path where the file was saved
     * @throws IOException if an I/O error occurs
     */
    String saveContent(InputStream fileStream, String filename) throws IOException;

    /**
     * Loads the content of a media file as a Resource.
     *
     * @param storagePath the path where the file is stored
     * @return the file as a Resource
     */
    Resource loadContent(String storagePath);

    /**
     * Gets the content of a media file as a byte array.
     *
     * @param storagePath the path where the file is stored
     * @return the file content as a byte array
     */
    byte[] getContentBytes(String storagePath);
}
