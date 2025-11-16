package pt.estga.stonemark.services.file;

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
public class FileStorageServiceLocalImpl implements FileStorageService {

    private final Path rootPath;

    public FileStorageServiceLocalImpl(@Value("${storage.local.root-path:uploads}") String rootDir) {
        this.rootPath = Paths.get(rootDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(rootPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    @Override
    public String storeFile(byte[] fileData, String filename, String directory) {
        if (fileData == null || fileData.length == 0) {
            throw new RuntimeException("Cannot store empty file");
        }

        try {
            // Create subdirectory if specified
            Path targetDir = rootPath;
            if (directory != null && !directory.isBlank()) {
                targetDir = rootPath.resolve(directory).normalize();
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

            // Save file
            Files.write(targetPath, fileData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // Return relative path (for DB storage)
            return rootPath.relativize(targetPath).toString().replace("\\", "/");

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public Resource loadFile(String path) {
        try {
            Path filePath = rootPath.resolve(path).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new RuntimeException("File not found: " + path);
            }

            return resource;
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found: " + path, e);
        }
    }

    @Override
    public void deleteFile(String path) {
        try {
            Path filePath = rootPath.resolve(path).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not delete file: " + path, e);
        }
    }
}
