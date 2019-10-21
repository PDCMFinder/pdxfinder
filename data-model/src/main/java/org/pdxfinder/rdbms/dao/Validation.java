

package org.pdxfinder.rdbms.dao;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.persistence.*;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "Technique",
        "Description",
        "Passage"
})
public class Validation {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String technique;
    private String description;
    private String passage;

    @ManyToOne
    @JoinColumn(name="pdxinfo_id")
    private PdmrPdxInfo pdmrPdxInfo;

    public Validation() {
    }

    public Validation(String technique, String description, String passage) {
        this.technique = technique;
        this.description = description;
        this.passage = passage;
    }


    @JsonProperty("Technique")
    public String getTechnique() {
        return technique;
    }

    @JsonProperty("Technique")
    public void setTechnique(String technique) {
        this.technique = technique;
    }

    @JsonProperty("Description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("Description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("Passage")
    public String getPassage() {
        return passage;
    }

    @JsonProperty("Passage")
    public void setPassage(String passage) {
        this.passage = passage;
    }

    public void setPdmrPdxInfo(PdmrPdxInfo pdmrPdxInfo) {
        this.pdmrPdxInfo = pdmrPdxInfo;
    }
}
