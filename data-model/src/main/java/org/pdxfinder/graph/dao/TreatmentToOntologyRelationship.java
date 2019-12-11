package org.pdxfinder.graph.dao;

import org.neo4j.ogm.annotation.*;

/*
 * Created by csaba on 08/05/2019.
 */
@RelationshipEntity(type="MAPPED_TO")
public class TreatmentToOntologyRelationship {

    @GraphId
    private Long relationshipId;

    @Property
    private String type;

    @Property
    private String justification;

    @StartNode
    private Treatment treatment;

    @EndNode
    private OntologyTerm ontologyTerm;


    public TreatmentToOntologyRelationship() {
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

    public Treatment getTreatment() {
        return treatment;
    }

    public void setTreatment(Treatment treatment) {
        this.treatment = treatment;
    }

    public OntologyTerm getOntologyTerm() {
        return ontologyTerm;
    }

    public void setOntologyTerm(OntologyTerm ontologyTerm) {
        this.ontologyTerm = ontologyTerm;
    }
}
