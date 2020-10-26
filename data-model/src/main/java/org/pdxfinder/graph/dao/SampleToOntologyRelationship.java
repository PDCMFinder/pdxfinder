package org.pdxfinder.graph.dao;

import org.neo4j.ogm.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/*
 * Created by csaba on 16/11/2017.
 */
@RelationshipEntity(type="MAPPED_TO")
public class SampleToOntologyRelationship {

    @Id
    @GeneratedValue
    private Long id;

    @Property
    private String type;

    @Property
    private String justification;

    @StartNode
    private Sample sample;

    @EndNode
    private OntologyTerm ontologyTerm;

    public SampleToOntologyRelationship() {
    }

    @Autowired
    public SampleToOntologyRelationship(String type, String justification, Sample sample, OntologyTerm ontologyTerm) {
        this.type = type;
        this.justification = justification;
        this.sample = sample;
        this.ontologyTerm = ontologyTerm;
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
