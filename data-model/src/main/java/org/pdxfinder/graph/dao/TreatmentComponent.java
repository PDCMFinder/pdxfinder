package org.pdxfinder.graph.dao;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/*
 * Created by csaba on 23/05/2018.
 */
@NodeEntity
public class TreatmentComponent {

    @Id
    @GeneratedValue
    private Long id;


    private String dose;

    private String type;
    //on mouse this is the treatment length
    private String duration;
    //on mouse this is the treatment schedule
    private String frequency;

    @Relationship(type = "TREATMENT")
    private Treatment treatment;

    public TreatmentComponent() {
        this.type = "Drug";
    }

    public TreatmentComponent(String dose, Treatment treatment) {
        this.dose = dose;
        this.treatment = treatment;
        this.type = "Drug";
    }

    public String getDose() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Treatment getTreatment() {
        return treatment;
    }

    public void setTreatment(Treatment treatment) {
        this.treatment = treatment;
    }
}
