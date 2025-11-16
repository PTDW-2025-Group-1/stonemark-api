package pt.estga.stonemark.dtos.proposals;

public record ProposedMonumentDto(
        Long id,
        String name,
        double latitude,
        double longitude
) { }
