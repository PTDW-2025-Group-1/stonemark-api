package pt.estga.stonemark.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String storeFile(MultipartFile file, String directory);

    Resource loadFile(String path);

    void deleteFile(String path);
}
