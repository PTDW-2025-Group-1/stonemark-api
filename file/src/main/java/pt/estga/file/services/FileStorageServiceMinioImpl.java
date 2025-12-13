package pt.estga.file.services;

import io.minio.*;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "minio")
@Slf4j
public class FileStorageServiceMinioImpl implements FileStorageService {

    private final MinioClient minioClient;
    private final String bucketName;

    public FileStorageServiceMinioImpl(
            MinioClient minioClient,
            @Value("${minio.bucket-name}") String bucketName
    ) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    @Override
    public String storeFile(byte[] fileData, String filename, String directory) {
        log.info("Storing file with filename: {} in directory: {}", filename, directory);
        if (fileData == null || fileData.length == 0) {
            log.error("Cannot store empty file");
            throw new RuntimeException("Cannot store empty file");
        }

        try {
            String originalName = filename;
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf('.'));
            }
            String newFileName = UUID.randomUUID() + extension;
            String objectName = (directory != null && !directory.isBlank() ? directory + "/" : "") + newFileName;

            log.debug("Putting object with name: {}", objectName);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(fileData), fileData.length, -1)
                            .build()
            );

            log.info("File stored successfully with object name: {}", objectName);
            return objectName;
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            log.error("Failed to store file in MinIO", e);
            throw new RuntimeException("Failed to store file in MinIO", e);
        }
    }

    @Override
    public Resource loadFile(String path) {
        log.info("Loading file from path: {}", path);
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()
            );
            log.info("File loaded successfully from path: {}", path);
            return new InputStreamResource(stream);
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            log.error("Failed to load file from MinIO with path: {}", path, e);
            throw new RuntimeException("Failed to load file from MinIO", e);
        }
    }

    @Override
    public void deleteFile(String path) {
        log.info("Deleting file from path: {}", path);
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()
            );
            log.info("File deleted successfully from path: {}", path);
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            log.error("Could not delete file from MinIO with path: {}", path, e);
            throw new RuntimeException("Could not delete file from MinIO", e);
        }
    }
}
