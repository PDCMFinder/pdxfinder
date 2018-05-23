package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/*
 * Created by csaba on 23/05/2018.
 */
@NodeEntity
public class TreatmentComponent {

    @GraphId
    private Long id;

    private String dose;

    @Relationship(type = "DRUG")
    private Drug drug;

    public TreatmentComponent() {
    }

    public TreatmentComponent(String dose, Drug drug) {
        this.dose = dose;
        this.drug = drug;
    }

    public String getDose() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public Drug getDrug() {
        return drug;
    }

    public void setDrug(Drug drug) {
        this.drug = drug;
    }
}
