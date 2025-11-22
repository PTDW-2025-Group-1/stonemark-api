package pt.estga.stonemark.dtos.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkDto {
    private Long id;
    private String title;
    private String description;
    private Long coverId;
}
