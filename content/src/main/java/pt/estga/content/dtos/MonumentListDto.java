package pt.estga.content.dtos;

public record MonumentListDto(
        Long id,
        String name,
        Long parishId,
        Long municipalityId,
        Long districtId,
        Long coverId
) { }