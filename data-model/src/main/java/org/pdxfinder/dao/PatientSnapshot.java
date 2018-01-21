package org.pdxfinder.dao;

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

    private Patient patient;
    private String age;
    private String dateAtCollection;
    private Boolean treatmentNaive;

    @Relationship(type = "SAMPLED_FROM")
    private Set<Sample> samples;

    @Relationship(type = "TREATED_WITH", direction = Relationship.INCOMING)
    private Set<Treatment> treatments;

    public PatientSnapshot() {
    }

    public PatientSnapshot(Patient patient, String age) {
        this.patient = patient;
        this.age = age;
    }

    public String getAgeBin() {
        String ageBin;

        try {
            Integer ageInteger = Integer.parseInt(this.age);

            if (ageInteger < 11) {
                ageBin = "Child";
            } else if (ageInteger < 21) {
                ageBin = "11-20";
            } else if (ageInteger < 31) {
                ageBin = "21-30";
            } else if (ageInteger < 41) {
                ageBin = "31-40";
            } else if (ageInteger < 51) {
                ageBin = "41-50";
            } else if (ageInteger < 61) {
                ageBin = "51-60";
            } else if (ageInteger < 71) {
                ageBin = "61-70";
            } else if (ageInteger < 81) {
                ageBin = "71-80";
            } else if (ageInteger < 91) {
                ageBin = "81-90";
            } else if (ageInteger < 101) {
                ageBin = "91-100";
            } else {
                ageBin = "> 101";
            }

        } catch (Exception e) {
            // probably a parse exception
            ageBin = this.age;
        }

        return ageBin;
    }

    public PatientSnapshot(Patient patient, String age, Set<Sample> samples) {
        this.patient = patient;
        this.age = age;
        this.samples = samples;
    }

    public PatientSnapshot(Patient patient, String age, Set<Sample> samples, Set<Treatment> treatments) {
        this.patient = patient;
        this.age = age;
        this.samples = samples;
        this.treatments = treatments;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
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

    public Set<Treatment> getTreatments() {
        return treatments;
    }

    public void setTreatments(Set<Treatment> treatments) {
        this.treatments = treatments;
    }

    public String getDateAtCollection() {
        return dateAtCollection;
    }

    public void setDateAtCollection(String dateAtCollection) {
        this.dateAtCollection = dateAtCollection;
    }

    public Boolean getTreatmentNaive() {
        return treatmentNaive;
    }

    public void setTreatmentNaive(Boolean treatmentNaive) {
        this.treatmentNaive = treatmentNaive;
    }
}
