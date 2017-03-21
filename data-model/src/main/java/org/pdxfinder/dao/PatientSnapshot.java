package org.pdxfinder.dao;

import java.util.HashSet;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

/**
 * Created by jmason on 16/03/2017.
 */
@NodeEntity
public class PatientSnapshot {

    @GraphId
    Long id;

    Patient patient;
    String age;

    @Relationship(type = "SAMPLED_FROM", direction = Relationship.OUTGOING)
    Set<Sample> samples;

    public PatientSnapshot() {
    }

    public PatientSnapshot(Patient patient, String age) {
        this.patient = patient;
        this.age = age;
    }

    public PatientSnapshot(Patient patient, String age, Set<Sample> samples) {
        this.patient = patient;
        this.age = age;
        this.samples = samples;
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
            this.samples = new HashSet<Sample>();
        }
        this.samples.add(sample);
    }
}
