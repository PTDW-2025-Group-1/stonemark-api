package pt.estga.stonemark.dtos.proposals;

import lombok.Data;
import pt.estga.stonemark.dtos.file.MediaFileDto;

@Data
public class ProposedMarkDto {
    private Long id;
    private String name;
    private String description;
    private MediaFileDto mediaFile;
}
