package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by csaba on 19/06/2017.
 */
@RelationshipEntity(type="MAPPED_TO")
public class SampleToDiseaseOntologyRelationship {

        @GraphId
        private Long relationshipId;

    @Property
        private String type;

    @Property
        private String justification;

    @StartNode
        private Sample sample;

    @EndNode
        private OntologyTerm ontologyTerm;


    public SampleToDiseaseOntologyRelationship() {
    }
    @Autowired
    public SampleToDiseaseOntologyRelationship(Sample sample, OntologyTerm ontologyTerm, String type, String justification) {
        this.type = type;
        this.justification = justification;
        this.sample = sample;
        this.ontologyTerm = ontologyTerm;

    }

    public Long getRelationshipId() {
        return relationshipId;
    }

    public void setRelationshipId(Long relationshipId) {
        this.relationshipId = relationshipId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public OntologyTerm getOntologyTerm() {
        return ontologyTerm;
    }

    public void setOntologyTerm(OntologyTerm ontologyTerm) {
        this.ontologyTerm = ontologyTerm;
    }
}
