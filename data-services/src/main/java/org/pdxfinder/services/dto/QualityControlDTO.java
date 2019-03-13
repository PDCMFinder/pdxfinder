package org.pdxfinder.services.dto;

/*
 * Created by csaba on 13/03/2019.
 */
public class QualityControlDTO {

    private String technique;
    private String description;
    private String passage;

    public QualityControlDTO(String technique, String description, String passage) {
        this.technique = technique;
        this.description = description;
        this.passage = passage;
    }

    public String getTechnique() {
        return technique;
    }

    public void setTechnique(String technique) {
        this.technique = technique;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPassage() {
        return passage;
    }

    public void setPassage(String passage) {
        this.passage = passage;
    }
}
