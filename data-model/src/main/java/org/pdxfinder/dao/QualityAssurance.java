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
    private String passages;

    public QualityAssurance() {
    }

    public QualityAssurance(String technology, String description, String passages) {
        this.technology = technology;
        this.description = description;

        this.passages = passages;
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
}
