package pt.estga.shared.dtos.proposals;

import lombok.Data;

@Data
public class ProposedMonumentDto {
    private Long id;
    private String name;
    private double latitude;
    private double longitude;
}
