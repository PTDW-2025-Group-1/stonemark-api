package pt.estga.file.services;

import org.springframework.core.io.Resource;
import pt.estga.file.entities.MediaFile;
import pt.estga.file.enums.TargetType;

import java.io.IOException;

public interface MediaService {

    MediaFile save(byte[] fileData, String filename, TargetType targetType) throws IOException;

    Resource loadFile(String filePath);

}
