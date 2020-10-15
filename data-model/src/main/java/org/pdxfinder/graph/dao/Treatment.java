package org.pdxfinder.graph.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/*
 * Created by csaba on 08/05/2019.
 */
@NodeEntity
public class Treatment {


    @GraphId
    private Long id;

    private String name;

    @Relationship(type="MAPPED_TO")
    private TreatmentToOntologyRelationship treatmentToOntologyRelationship;

    public Treatment(String name) {
        this.name = name;
    }

    public Treatment() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TreatmentToOntologyRelationship getTreatmentToOntologyRelationship() {
        return treatmentToOntologyRelationship;
    }

    public void setTreatmentToOntologyRelationship(TreatmentToOntologyRelationship treatmentToOntologyRelationship) {
        this.treatmentToOntologyRelationship = treatmentToOntologyRelationship;
    }

    @Override
    public String toString() {
        return "Treatment{" +
                "name='" + name + '\'' +
                ", treatmentToOntologyRelationship=" + treatmentToOntologyRelationship +
                '}';
    }
}
