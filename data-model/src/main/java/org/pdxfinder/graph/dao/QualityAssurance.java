package org.pdxfinder.graph.dao;

import org.neo4j.ogm.annotation.NodeEntity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by jmason on 17/03/2017.
 */
@NodeEntity
public class QualityAssurance {

    @Id
    @GeneratedValue
    Long id;

    private String technology;
    private String description;
    private String passages;
    private String validationHostStrain;

    public QualityAssurance() {
    }

    public QualityAssurance(String technology, String description, String passages) {
        this.technology = technology;
        this.description = description;

        this.passages = passages;
    }

    public QualityAssurance(String technology, String description, String passages, String validationHostStrain) {
        this.technology = technology;
        this.description = description;
        this.passages = passages;
        this.validationHostStrain = validationHostStrain;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPassages() {
        return passages;
    }

    public void setPassages(String passages) {
        this.passages = passages;
    }

    public String getValidationHostStrain() {
        return validationHostStrain;
    }

    public void setValidationHostStrain(String validationHostStrain) {
        this.validationHostStrain = validationHostStrain;
    }
}
