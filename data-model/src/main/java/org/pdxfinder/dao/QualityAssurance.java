package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * Created by jmason on 17/03/2017.
 */
@NodeEntity
public class QualityAssurance {

    @GraphId
    Long id;

    private String technology;
    private String description;
    private ValidationTechniques validationTechniques;

    public QualityAssurance() {
    }

    public QualityAssurance(String technology, String description, ValidationTechniques validationTechniques) {
        this.technology = technology;
        this.description = description;
        this.validationTechniques = validationTechniques;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public ValidationTechniques getValidationTechniques() {
        return validationTechniques;
    }

    public void setValidationTechniques(ValidationTechniques validationTechniques) {
        this.validationTechniques = validationTechniques;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
