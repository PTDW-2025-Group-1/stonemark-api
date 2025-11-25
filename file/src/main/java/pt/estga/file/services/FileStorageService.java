package pt.estga.file.services;

import org.springframework.core.io.Resource;

public interface FileStorageService {

    String storeFile(byte[] fileData, String filename, String directory);

    Resource loadFile(String path);

    void deleteFile(String path);
}
