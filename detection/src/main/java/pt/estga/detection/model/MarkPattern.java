package pt.estga.detection.model;

import lombok.Data;

@Data
public class MarkPattern {
    private String id;
    private String name;
    private float[] vector;
}
