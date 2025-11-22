package pt.estga.stonemark.dtos.proposals;

import lombok.Data;
import pt.estga.stonemark.dtos.content.MarkDto;
import pt.estga.stonemark.dtos.content.MonumentDto;
import pt.estga.stonemark.dtos.file.MediaFileDto;
import pt.estga.stonemark.enums.ProposalStatus;

@Data
public class MarkOccurrenceProposalDto {
    private Long id;
    private ProposalStatus status;
    private MediaFileDto originalMediaFile;
    private MonumentDto existingMonument;
    private ProposedMonumentDto proposedMonument;
    private MarkDto existingMark;
    private ProposedMarkDto proposedMark;
}
