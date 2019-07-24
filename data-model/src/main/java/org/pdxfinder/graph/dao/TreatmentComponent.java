package org.pdxfinder.graph.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/*
 * Created by csaba on 23/05/2018.
 */
@NodeEntity
public class TreatmentComponent {

    /**
     * @param type                      Type of the treatment, ie drug/control
     * @param duration                  For how long was the treatment administered
     * @param frequency                 How often the treatment was administered
     */


    @GraphId
    private Long id;

    private String dose;

    private String type;
    private String duration;
    private String frequency;

    @Relationship(type = "TREATMENT")
    private Treatment treatment;

    public TreatmentComponent() {
    }

    public TreatmentComponent(String dose, Treatment treatment) {
        this.dose = dose;
        this.treatment = treatment;
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
