package pt.estga.stonemark.entities.content;

import pt.estga.stonemark.enums.MarkCategory;
import pt.estga.stonemark.enums.MarkShape;

public class MarkBuilder {
    private Long id;
    private String title;
    private MarkCategory category;
    private MarkShape shape;

    public MarkBuilder setId(Long id) {
        this.id = id;
        return this;
    }

    public MarkBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public MarkBuilder setCategory(MarkCategory category) {
        this.category = category;
        return this;
    }

    public MarkBuilder setShape(MarkShape shape) {
        this.shape = shape;
        return this;
    }

    public Mark createMark() {
        return new Mark(id, title, category, shape);
    }
}