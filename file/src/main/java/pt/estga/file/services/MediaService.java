package pt.estga.file.services;

import pt.estga.stonemark.entities.MediaFile;
import pt.estga.stonemark.enums.TargetType;

import java.io.IOException;

public interface MediaService {

    MediaFile save(byte[] fileData, String filename, TargetType targetType) throws IOException;

    void delete(Long mediaId);

}
