package pt.estga.proposal.dtos;

public record ProposedMonumentDto(
        Long id,
        String name,
        double latitude,
        double longitude
) {
}
