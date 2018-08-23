package org.pdxfinder.dao;

import org.neo4j.cypher.internal.frontend.v2_3.SemanticDirection;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by jmason on 16/03/2017.
 */
@NodeEntity
public class PatientSnapshot {

    @GraphId
    private Long id;

    private String ageAtCollection;
    private String dateAtCollection;
    private String treatmentNaive;

    private String collectionEvent;
    private String elapsedTime;
    private String eventType;

    private String virologyStatus;


    @Relationship(type = "COLLECTION_EVENT", direction = Relationship.INCOMING)
    private Patient patient;

    @Relationship(type = "SAMPLED_FROM")
    private Set<Sample> samples;

    @Relationship(type = "SUMMARY_OF_TREATMENT", direction = Relationship.INCOMING)
    private TreatmentSummary treatmentSummary;

    public PatientSnapshot() {
    }

    public PatientSnapshot(Patient patient, String ageAtCollection) {
        this.patient = patient;
        this.ageAtCollection = ageAtCollection;
    }

    public PatientSnapshot(Patient patient, String ageAtCollection, String dateAtCollection, String collectionEvent, String elapsedTime) {
        this.patient = patient;
        this.ageAtCollection = ageAtCollection;
        this.dateAtCollection = dateAtCollection;
        this.collectionEvent = collectionEvent;
        this.elapsedTime = elapsedTime;
    }

    public String getAgeBin() {
        String ageBin;

        try {
            Integer ageInteger = Integer.parseInt(this.ageAtCollection);

            if (ageInteger < 10) {
                ageBin = "0-9";
            } else if (ageInteger < 20) {
                ageBin = "10-19";
            } else if (ageInteger < 30) {
                ageBin = "20-29";
            } else if (ageInteger < 40) {
                ageBin = "30-39";
            } else if (ageInteger < 50) {
                ageBin = "40-49";
            } else if (ageInteger < 60) {
                ageBin = "50-59";
            } else if (ageInteger < 70) {
                ageBin = "60-69";
            } else if (ageInteger < 80) {
                ageBin = "70-79";
            } else if (ageInteger < 90) {
                ageBin = "80-89";
            }
            else {
                ageBin = "90+";
            }

        } catch (Exception e) {
            // probably a parse exception
            ageBin = this.ageAtCollection;
        }

        return ageBin;
    }

    public PatientSnapshot(Patient patient, String ageAtCollection, Set<Sample> samples) {
        this.patient = patient;
        this.ageAtCollection = ageAtCollection;
        this.samples = samples;
    }

    public PatientSnapshot(Patient patient, String ageAtCollection, Set<Sample> samples, TreatmentSummary treatmentSummary) {
        this.patient = patient;
        this.ageAtCollection = ageAtCollection;
        this.samples = samples;
        this.treatmentSummary = treatmentSummary;
    }

    public String getAgeAtCollection() {
        return ageAtCollection;
    }

    public void setAgeAtCollection(String ageAtCollection) {
        this.ageAtCollection = ageAtCollection;
    }

    public Set<Sample> getSamples() {
        return samples;
    }

    public void setSamples(Set<Sample> samples) {
        this.samples = samples;
    }
    
    public void addSample(Sample sample){
        if(this.samples == null){
            this.samples = new HashSet<>();
        }
        this.samples.add(sample);
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getDateAtCollection() {
        return dateAtCollection;
    }

    public void setDateAtCollection(String dateAtCollection) {
        this.dateAtCollection = dateAtCollection;
    }

    public String getTreatmentNaive() {
        return treatmentNaive;
    }

    public void setTreatmentNaive(String treatmentNaive) {
        this.treatmentNaive = treatmentNaive;
    }

    public TreatmentSummary getTreatmentSummary() {
        return treatmentSummary;
    }

    public void setTreatmentSummary(TreatmentSummary treatmentSummary) {
        this.treatmentSummary = treatmentSummary;
    }

    public String getCollectionEvent() {
        return collectionEvent;
    }

    public void setCollectionEvent(String collectionEvent) {
        this.collectionEvent = collectionEvent;
    }

    public String getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(String elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getVirologyStatus() {
        return virologyStatus;
    }

    public void setVirologyStatus(String virologyStatus) {
        this.virologyStatus = virologyStatus;
    }
}
