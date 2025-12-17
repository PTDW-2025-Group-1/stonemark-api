package pt.estga.content.seeders;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pt.estga.content.entities.Mark;
import pt.estga.content.repositories.MarkRepository;
import pt.estga.file.entities.MediaFile;
import pt.estga.shared.enums.TargetType;
import pt.estga.file.services.MediaService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MarkSeeder implements CommandLineRunner {

    private static final boolean ENABLED = false;

    private final MarkRepository markRepository;
    private final MediaService mediaService;

    @Override
    public void run(String... args) throws IOException {
        if (!ENABLED) {
            return;
        }

        File marksDir = new File("seed/marks");
        if (marksDir.exists() && marksDir.isDirectory()) {
            List<Mark> existingMarks = markRepository.findAll();
            File[] files = marksDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (existingMarks.stream().noneMatch(mark -> mark.getTitle().equals(file.getName()))) {
                        byte[] fileData = Files.readAllBytes(file.toPath());
                        MediaFile mediaFile = mediaService.save(fileData, file.getName(), TargetType.MARK);

                        Mark mark = new Mark();
                        mark.setTitle(file.getName());
                        mark.setCover(mediaFile);
                        markRepository.save(mark);
                    }
                }
            }
        }
    }
}
