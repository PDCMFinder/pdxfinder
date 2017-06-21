package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class PdxPassage {

    @GraphId
    Long id;

    Integer passage;

    @Relationship(type = "INSTANCE_OF")
    private ModelCreation modelCreation;

    @Relationship(type = "PASSAGED_FROM")
    private PdxPassage pdxPassage;
    
    public PdxPassage(){
        
    }

    // When linking to a model creation
    public PdxPassage(ModelCreation modelCreation, Integer passage) {
        this.modelCreation = modelCreation;
        this.passage = passage;
    }

    // When linking to another passage
    public PdxPassage(PdxPassage pdxPassage, Integer passage) {
        this.pdxPassage = pdxPassage;
        this.passage = passage;
    }


    public ModelCreation getModelCreation() {
        return modelCreation;
    }

    public void setModelCreation(ModelCreation modelCreation) {
        this.modelCreation = modelCreation;
    }

    public Integer getPassage() {
        return passage;
    }

    public void setPassage(Integer passage) {
        this.passage = passage;
    }

    public PdxPassage getPdxPassage() {
        return pdxPassage;
    }

    public void setPdxPassage(PdxPassage pdxPassage) {
        this.pdxPassage = pdxPassage;
    }
}
