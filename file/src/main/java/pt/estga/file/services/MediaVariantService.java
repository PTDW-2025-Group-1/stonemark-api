package pt.estga.file.services;

import org.springframework.core.io.Resource;
import pt.estga.file.enums.MediaVariantType;

public interface MediaVariantService {
    Resource loadVariant(Long mediaId, MediaVariantType type);
}
