package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class PdxPassage {

    @GraphId
    Long id;

    Integer passage;

    @Relationship(type = "PASSAGED_FROM", direction = Relationship.OUTGOING)
    private PdxStrain pdxStrain;


    public PdxPassage(PdxStrain pdxStrain, Integer passage) {
        this.pdxStrain = pdxStrain;
        this.passage = passage;
    }

    public PdxStrain getPdxStrain() {
        return pdxStrain;
    }

    public void setPdxStrain(PdxStrain pdxStrain) {
        this.pdxStrain = pdxStrain;
    }

    public Integer getPassage() {
        return passage;
    }

    public void setPassage(Integer passage) {
        this.passage = passage;
    }
}
