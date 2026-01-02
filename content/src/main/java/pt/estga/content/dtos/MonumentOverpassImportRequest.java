package pt.estga.content.dtos;

/**
 * Request DTO for importing monuments from Overpass API.
 *
 * @param monumentData Raw Overpass JSON with monument data (center + tags)
 */
public record MonumentOverpassImportRequest(
    String monumentData
) {}
