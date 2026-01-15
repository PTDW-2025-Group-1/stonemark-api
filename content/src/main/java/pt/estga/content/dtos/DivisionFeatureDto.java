package pt.estga.content.dtos;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

@Builder
public record DivisionFeatureDto(
    String name,
    String adminLevel,
    JsonNode geometry
) {}
