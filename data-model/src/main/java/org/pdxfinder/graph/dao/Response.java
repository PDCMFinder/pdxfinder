package org.pdxfinder.graph.dao;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/*
 * Created by csaba on 20/10/2017.
 */
@NodeEntity
public class Response {

    @Id
    @GeneratedValue
    private Long id;

    private String description;

    private String descriptionClassification;

    @Relationship(type = "RESPONSE", direction = Relationship.INCOMING)
    private TreatmentProtocol treatment;

    //empty constructor
    public Response() {
    }

    public Response(String description, TreatmentProtocol treatment) {
        this.description = description;
        this.treatment = treatment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TreatmentProtocol getTreatment() {
        return treatment;
    }

    public void setTreatment(TreatmentProtocol treatment) {
        this.treatment = treatment;
    }

    public String getDescriptionClassification() {
        return descriptionClassification;
    }

    public void setDescriptionClassification(String descriptionClassification) {
        this.descriptionClassification = descriptionClassification;
    }
}
