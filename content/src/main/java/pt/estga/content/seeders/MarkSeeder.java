package pt.estga.stonemark.seeders;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pt.estga.content.entities.Mark;
import pt.estga.stonemark.repositories.content.MarkRepository;

import java.io.File;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MarkSeeder implements CommandLineRunner {

    private final MarkRepository markRepository;
    private final MediaFileRepository mediaFileRepository;

    @Override
    public void run(String... args) {
        File marksDir = new File("src/main/resources/seed/marks");
        if (marksDir.exists() && marksDir.isDirectory()) {
            List<Mark> existingMarks = markRepository.findAll();
            File[] files = marksDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (existingMarks.stream().noneMatch(mark -> mark.getTitle().equals(file.getName()))) {
                        MediaFile mediaFile = new MediaFile();
                        mediaFile.setFileName(file.getName());
                        mediaFile.setOriginalFileName(file.getName());
                        mediaFile.setSize(file.length());
                        mediaFile.setStorageProvider(StorageProvider.LOCAL);
                        mediaFile.setStoragePath(file.getAbsolutePath());
                        mediaFile.setTargetType(TargetType.MARK);
                        mediaFile.setUploadedAt(Instant.now());
                        mediaFileRepository.save(mediaFile);

                        Mark mark = new Mark();
                        mark.setTitle(file.getName());
                        mark.setPhoto(mediaFile);
                        markRepository.save(mark);
                    }
                }
            }
        }
    }
}
