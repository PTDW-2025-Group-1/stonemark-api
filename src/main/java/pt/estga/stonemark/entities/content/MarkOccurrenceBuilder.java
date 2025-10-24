package pt.estga.stonemark.entities.content;

public class MarkOccurrenceBuilder {
    private Long id;
    private Long markId;
    private Long monumentId;

    public MarkOccurrenceBuilder setId(Long id) {
        this.id = id;
        return this;
    }

    public MarkOccurrenceBuilder setMarkId(Long markId) {
        this.markId = markId;
        return this;
    }

    public MarkOccurrenceBuilder setMonumentId(Long monumentId) {
        this.monumentId = monumentId;
        return this;
    }

    public MarkOccurrence createMarkOccurrence() {
        return new MarkOccurrence(id, markId, monumentId);
    }
}