package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/*
 * Created by csaba on 20/10/2017.
 */
@NodeEntity
public class Response {

    @GraphId
    private Long id;

    private String description;

    @Relationship(type = "RESPONSE", direction = Relationship.INCOMING)
    private Treatment treatment;

    //empty constructor
    public Response() {
    }

    public Response(String description, Treatment treatment) {
        this.description = description;
        this.treatment = treatment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Treatment getTreatment() {
        return treatment;
    }

    public void setTreatment(Treatment treatment) {
        this.treatment = treatment;
    }
}
