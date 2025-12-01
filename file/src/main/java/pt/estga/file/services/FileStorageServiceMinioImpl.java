package pt.estga.file.services;

import io.minio.*;
import io.minio.errors.MinioException;
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
public class FileStorageServiceMinioImpl implements FileStorageService {

    private final MinioClient minioClient;
    private final String bucketName;

    public FileStorageServiceMinioImpl(
            MinioClient minioClient,
            @Value("${minio.bucket-name}") String bucketName
    ) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
        createBucketIfNotExists();
    }

    private void createBucketIfNotExists() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("Could not initialize MinIO bucket", e);
        }
    }

    @Override
    public String storeFile(byte[] fileData, String filename, String directory) {
        if (fileData == null || fileData.length == 0) {
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

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(fileData), fileData.length, -1)
                            .build()
            );

            return objectName;
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("Failed to store file in MinIO", e);
        }
    }

    @Override
    public Resource loadFile(String path) {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()
            );
            return new InputStreamResource(stream);
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("Failed to load file from MinIO", e);
        }
    }

    @Override
    public void deleteFile(String path) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()
            );
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("Could not delete file from MinIO", e);
        }
    }
}
