package pt.estga.shared.dtos.proposals;

import lombok.Data;
import pt.estga.shared.dtos.file.MediaFileDto;

@Data
public class ProposedMarkDto {
    private Long id;
    private String name;
    private String description;
    private MediaFileDto mediaFile;
}
