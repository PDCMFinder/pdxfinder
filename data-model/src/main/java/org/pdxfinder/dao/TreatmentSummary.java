package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * TreatmentSummary represents a summary of the application of a treatment protocol to a sample (either Human or Xenograft)
 */

@NodeEntity
public class TreatmentSummary {

    @GraphId
    Long id;
    //url to a page that describes the treatment
    String url;

    @Relationship(type = "TREATMENT_PROTOCOL")
    Set<TreatmentProtocol> treatmentProtocols;

    @Relationship(type = "SUMMARY_OF_TREATMENT")
    ModelCreation modelCreation;

    @Relationship(type = "SUMMARY_OF_TREATMENT")
    PatientSnapshot patientSnapshot;

    public TreatmentSummary() {
    }

    public Set<TreatmentProtocol> getTreatmentProtocols() {
        return treatmentProtocols;
    }

    public void setTreatmentProtocols(Set<TreatmentProtocol> treatmentProtocols) {
        this.treatmentProtocols = treatmentProtocols;
    }

    public ModelCreation getModelCreation() {
        return modelCreation;
    }

    public void setModelCreation(ModelCreation modelCreation) {
        this.modelCreation = modelCreation;
    }

    public PatientSnapshot getPatientSnapshot() {
        return patientSnapshot;
    }

    public void setPatientSnapshot(PatientSnapshot patientSnapshot) {
        this.patientSnapshot = patientSnapshot;
    }

    public void addTreatmentProtocol(TreatmentProtocol tp){
        if(this.treatmentProtocols == null){
            this.treatmentProtocols = new HashSet<>();
        }
        this.treatmentProtocols.add(tp);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
