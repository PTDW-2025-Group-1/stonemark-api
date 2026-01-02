package pt.estga.content.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GeocodingResultDto {
    private String name;
    private String address;
    private String city;
}
