package pt.estga.shared.dtos.proposals;

import lombok.Data;
import pt.estga.shared.dtos.content.MarkDto;
import pt.estga.shared.dtos.content.MonumentDto;
import pt.estga.shared.dtos.file.MediaFileDto;
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
