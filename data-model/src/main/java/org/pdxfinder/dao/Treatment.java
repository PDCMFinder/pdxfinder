package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/*
 * Created by csaba on 20/10/2017.
 */
@NodeEntity
public class Treatment {

    @GraphId
    private Long id;

    private String therapy;

    //TODO: Add therapy details

    @Relationship(type = "TREATED_WITH")
    private PatientSnapshot snapshot;

    @Relationship(type = "TREATED_WITH")
    private Specimen specimen;

    @Relationship(type = "RESPONSE")
    private Response response;

    //empty constructor
    public Treatment() {
    }

    public Treatment(String therapy, PatientSnapshot snapshot) {
        this.therapy = therapy;
        this.snapshot = snapshot;
    }

    public Treatment(String therapy, Specimen specimen) {
        this.therapy = therapy;
        this.specimen = specimen;
    }

    public String getTherapy() {
        return therapy;
    }

    public void setTherapy(String therapy) {
        this.therapy = therapy;
    }

    public PatientSnapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(PatientSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public Specimen getSpecimen() {
        return specimen;
    }

    public void setSpecimen(Specimen specimen) {
        this.specimen = specimen;
    }
}
