package pt.estga.stonemark.entities.content;

import java.time.LocalDateTime;

public class MonumentBuilder {
    private Long id;
    private String name;
    private String description;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MonumentBuilder setId(Long id) {
        this.id = id;
        return this;
    }

    public MonumentBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public MonumentBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public MonumentBuilder setLatitude(Double latitude) {
        this.latitude = latitude;
        return this;
    }

    public MonumentBuilder setLongitude(Double longitude) {
        this.longitude = longitude;
        return this;
    }

    public MonumentBuilder setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public MonumentBuilder setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public Monument createMonument() {
        return new Monument(id, name, description, latitude, longitude, createdAt, updatedAt);
    }
}