package org.pdxfinder.graph.dao;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

@NodeEntity
public class Patient {

    @Id
    @GeneratedValue
    private Long id;

    private String externalId;
    private String sex;
    private String race;
    private String ethnicity;
    private String ethnicityAssessment;
    private String dataSource;
    private String cancerRelevantHistory;
    private String firstDiagnosis;
    private String ageAtFirstDiagnosis;

    @Relationship(type = "GROUP", direction = Relationship.INCOMING)
    private List<Group> groups;

    @Relationship(type = "COLLECTION_EVENT")
    private Set<PatientSnapshot> snapshots;

    public Patient() {
        // Empty constructor required as of Neo4j API 2.0.5
    }

    public Patient(String externalId, Group providerGroup) {
        this.externalId = externalId;
        this.groups = Collections.singletonList(providerGroup);
    }

    public Patient(String externalId, String sex, String race, String ethnicity, Group group) {
        this.externalId = externalId;
        this.sex = sex;
        this.race = race;
        this.ethnicity = ethnicity;
        this.dataSource = group.getAbbreviation();
        this.groups = new ArrayList<>();
        this.groups.add(group);
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    @Deprecated
    public String getRace() {
        return race;
    }

    @Deprecated
    public void setRace(String race) {
        this.race = race;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public Set<PatientSnapshot> getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(Set<PatientSnapshot> snapshots) {
        this.snapshots = snapshots;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public Group getProviderGroup(){

        if(groups == null) return null;

        for(Group g : groups){
            if(g.getType().equals("Provider")) return g;
        }

        return null;
    }

    public String getCancerRelevantHistory() {
        return cancerRelevantHistory;
    }

    public void setCancerRelevantHistory(String cancerRelevantHistory) {
        this.cancerRelevantHistory = cancerRelevantHistory;
    }

    public String getFirstDiagnosis() {
        return firstDiagnosis;
    }

    public void setFirstDiagnosis(String firstDiagnosis) {
        this.firstDiagnosis = firstDiagnosis;
    }

    public String getAgeAtFirstDiagnosis() {
        return ageAtFirstDiagnosis;
    }

    public void setAgeAtFirstDiagnosis(String ageAtFirstDiagnosis) {
        this.ageAtFirstDiagnosis = ageAtFirstDiagnosis;
    }

    public PatientSnapshot getSnapshotByDate(String date){

        if(snapshots != null){

            for(PatientSnapshot psnap : snapshots){

                if(psnap.getDateAtCollection().equals(date)) return psnap;
            }

        }

        return null;
    }

    public PatientSnapshot getSnapShotByCollection(String age, String collectionDate, String collectionEvent, String ellapsedTime){

        if(snapshots != null){

            for(PatientSnapshot psnap : snapshots){

                if(psnap.getAgeAtCollection().equals(age) &&
                        psnap.getDateAtCollection().equals(collectionDate) &&
                        psnap.getCollectionEvent().equals(collectionEvent) &&
                        psnap.getElapsedTime().equals(ellapsedTime)) return psnap;
            }

        }

        return null;

    }


    public PatientSnapshot getLastSnapshot(){

        if(snapshots == null) return null;

        PatientSnapshot latestPSByAge = getLastSnapshotByAge();
        PatientSnapshot latestPSByDate = getLastSnapshotByDate();

        if(latestPSByAge != null ) {

            return latestPSByAge;
        }
        else return latestPSByDate;

    }


    private PatientSnapshot getLastSnapshotByAge(){

        PatientSnapshot latestPSByAge = null;

        for(PatientSnapshot ps: snapshots){

            if(latestPSByAge == null){
                latestPSByAge = ps;
            }
            else{
                if(latestPSByAge.getAgeAtCollection() != null && ps.getAgeAtCollection() != null && latestPSByAge.getAgeAtCollection().compareTo(ps.getAgeAtCollection()) < 0){

                    latestPSByAge = ps;
                }
            }
        }

        return latestPSByAge;
    }

    private PatientSnapshot getLastSnapshotByDate(){

        PatientSnapshot latestPSByDate = null;

        for(PatientSnapshot ps: snapshots){

            if(latestPSByDate == null){
                latestPSByDate = ps;
            }
            else{
                if(latestPSByDate.getDateAtCollection() != null && ps.getDateAtCollection() != null && latestPSByDate.getDateAtCollection().compareTo(ps.getDateAtCollection()) < 0){

                    latestPSByDate = ps;
                }
            }
        }

        return latestPSByDate;
    }



    public void addSnapshot(PatientSnapshot ps){

        if(snapshots == null) snapshots = new HashSet<>();
        snapshots.add(ps);
    }

    public String getEthnicityAssessment() {
        return ethnicityAssessment;
    }

    public void setEthnicityAssessment(String ethnicityAssessment) {
        this.ethnicityAssessment = ethnicityAssessment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Patient patient = (Patient) o;

        return new EqualsBuilder()
            .append(getExternalId(), patient.getExternalId())
            .append(getGroups(), patient.getGroups())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getExternalId())
            .append(getGroups())
            .toHashCode();
    }

    @Override
    public String toString() {
        return String.format("[%s]", externalId);
    }

}
