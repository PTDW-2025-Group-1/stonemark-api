package pt.estga.proposals.dtos;

public record ProposedMonumentDto(
        Long id,
        String name,
        double latitude,
        double longitude
) {
}
