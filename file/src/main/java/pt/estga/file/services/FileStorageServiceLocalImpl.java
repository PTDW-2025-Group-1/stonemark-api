package pt.estga.file.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
@Slf4j
public class FileStorageServiceLocalImpl implements FileStorageService {

    private final Path rootPath;

    public FileStorageServiceLocalImpl(@Value("${storage.local.root-path:uploads}") String rootDir) {
        this.rootPath = Paths.get(rootDir).toAbsolutePath().normalize();

        try {
            log.info("Creating storage directory at: {}", rootPath);
            Files.createDirectories(rootPath);
        } catch (IOException e) {
            log.error("Could not initialize storage directory", e);
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    @Override
    public String storeFile(byte[] fileData, String filename, String directory) {
        log.info("Storing file with filename: {} in directory: {}", filename, directory);
        if (fileData == null || fileData.length == 0) {
            log.error("Cannot store empty file");
            throw new RuntimeException("Cannot store empty file");
        }

        try {
            // Create subdirectory if specified
            Path targetDir = rootPath;
            if (directory != null && !directory.isBlank()) {
                targetDir = rootPath.resolve(directory).normalize();
                log.debug("Creating subdirectory: {}", targetDir);
                Files.createDirectories(targetDir);
            }

            // Generate safe unique filename
            String originalName = filename;
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf('.'));
            }
            String newFileName = UUID.randomUUID() + extension;

            Path targetPath = targetDir.resolve(newFileName).normalize();
            log.debug("Writing file to: {}", targetPath);

            // Save file
            Files.write(targetPath, fileData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // Return relative path (for DB storage)
            String relativePath = rootPath.relativize(targetPath).toString().replace("\\", "/");
            log.info("File stored successfully at: {}", relativePath);
            return relativePath;

        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public Resource loadFile(String path) {
        log.info("Loading file from path: {}", path);
        try {
            Path filePath = rootPath.resolve(path).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                log.error("File not found: {}", path);
                throw new RuntimeException("File not found: " + path);
            }

            log.info("File loaded successfully from path: {}", path);
            return resource;
        } catch (MalformedURLException e) {
            log.error("File not found: {}", path, e);
            throw new RuntimeException("File not found: " + path, e);
        }
    }

    @Override
    public void deleteFile(String path) {
        log.info("Deleting file from path: {}", path);
        try {
            Path filePath = rootPath.resolve(path).normalize();
            Files.deleteIfExists(filePath);
            log.info("File deleted successfully from path: {}", path);
        } catch (IOException e) {
            log.error("Could not delete file: {}", path, e);
            throw new RuntimeException("Could not delete file: " + path, e);
        }
    }
}
