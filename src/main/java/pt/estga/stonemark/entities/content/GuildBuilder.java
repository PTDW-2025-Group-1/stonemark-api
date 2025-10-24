package pt.estga.stonemark.entities.content;

import java.util.Date;

public class GuildBuilder {
    private Long id;
    private String name;
    private String description;
    private Date foundedDate;
    private Date dissolvedDate;

    public GuildBuilder setId(Long id) {
        this.id = id;
        return this;
    }

    public GuildBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public GuildBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public GuildBuilder setFoundedDate(Date foundedDate) {
        this.foundedDate = foundedDate;
        return this;
    }

    public GuildBuilder setDissolvedDate(Date dissolvedDate) {
        this.dissolvedDate = dissolvedDate;
        return this;
    }

    public Guild createGuild() {
        return new Guild(id, name, description, foundedDate, dissolvedDate);
    }
}