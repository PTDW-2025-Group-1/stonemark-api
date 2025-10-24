package pt.estga.stonemark.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pt.estga.stonemark.entities.MediaFile;
import pt.estga.stonemark.entities.MediaFileBuilder;
import pt.estga.stonemark.entities.content.Mark;
import pt.estga.stonemark.enums.StorageProvider;
import pt.estga.stonemark.enums.TargetType;
import pt.estga.stonemark.respositories.MarkRepository;
import pt.estga.stonemark.respositories.MediaRepository;

import java.util.List;

@Service
public class MediaServiceSpringImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final MarkRepository markRepository;
    private final FileStorageService fileStorageService;

    public MediaServiceSpringImpl(MediaRepository mediaRepository,
                                  MarkRepository markRepository,
                                  FileStorageService fileStorageService) {
        this.mediaRepository = mediaRepository;
        this.markRepository = markRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public MediaFile attachMediaToMark(Long markId, MultipartFile file, boolean primaryImage, int sortOrder) {
        Mark mark = markRepository.findById(markId)
                .orElseThrow(() -> new RuntimeException("MarkOccurrence not found with id: " + markId));

        // Store the file (in local filesystem, S3, etc.)
        String directory = "marks/" + markId;
        String storagePath = fileStorageService.storeFile(file, directory);

        MediaFile media = new MediaFileBuilder().createMediaFile();
        media.setFileName(file.getOriginalFilename());
        media.setOriginalFileName(file.getOriginalFilename());
        media.setContentType(file.getContentType());
        media.setSize(file.getSize());
        media.setStorageProvider(StorageProvider.LOCAL); // or AZURE if using cloud
        media.setStoragePath(storagePath);
        media.setProviderPublicId(null); // can be set by cloud storage (e.g., Azure blob ID)
        media.setTargetType(TargetType.MARK);
        media.setTargetId(mark.getId());
        media.setPrimaryImage(primaryImage);
        media.setSortOrder(sortOrder);

        return mediaRepository.save(media);
    }

    @Override
    @Transactional
    public List<MediaFile> getMediaForMark(Long markId) {
        return mediaRepository.findByTargetTypeAndTargetId(TargetType.MARK, markId);
    }

    @Override
    public void deleteMedia(Long mediaId) {

        // TODO verify if user is moderator or admin

        MediaFile media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found with id: " + mediaId));
        fileStorageService.deleteFile(media.getStoragePath());
        mediaRepository.delete(media);
    }
}
