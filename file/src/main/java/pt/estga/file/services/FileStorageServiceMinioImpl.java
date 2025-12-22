package pt.estga.file.services;

import io.minio.*;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

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
    public String storeFile(InputStream fileStream, String filename) {
        log.info("Storing file with filename: {}", filename);
        if (fileStream == null) {
            log.error("Cannot store empty file stream");
            throw new RuntimeException("Cannot store empty file stream");
        }

        try {

            log.debug("Putting object with name: {}", filename);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .stream(fileStream, -1, 10485760) // Let Minio handle stream size and multipart
                            .build()
            );

            log.info("File stored successfully with object name: {}", filename);
            return filename;
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
