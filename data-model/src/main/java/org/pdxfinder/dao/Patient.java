
package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * Entity class for storing the Patient from whom the Xenograft was derived
 */
@NodeEntity
public class Patient {

    @GraphId
    private Long id;


    private String externalId;
    private String sex;
    private String race;
    private String ethnicity;
    private String dataSource;
    private String cancerRelevantHistory;
    private String firstDiagnosis;
    private String ageAtFirstDiagnosis;

    @Relationship(type = "GROUP", direction = Relationship.INCOMING)
    private List<Group> groups;

    @Relationship(type = "COLLECTION_EVENT")
    private Set<PatientSnapshot> snapshots;

    /**
     * Empty constructor required as of Neo4j API 2.0.5
     */
    private Patient() {

    }

    /**
     * The constructor initializes the Patient's Entity Object
     * @param externalId The external ID property of the Patient node
     * @param sex  This is the patient's gender
     * @param race The Patient's physical characteristics such as skin, hair, or eye color
     * @param ethnicity The Patients origin either by birth e.g German or Spanish ancestry
     * @param group The source of the Patient's data (Provider)
     */
    public Patient(String externalId, String sex, String race, String ethnicity, Group group) {
        this.externalId = externalId;
        this.sex = sex;
        this.race = race;
        this.ethnicity = ethnicity;
        this.dataSource = group.getAbbreviation();
        this.groups = new ArrayList<>();
        this.groups.add(group);
    }


    public void hasSnapshot(PatientSnapshot snapshot) {
        if (snapshots == null) {
            snapshots = new HashSet<>();
        }
        snapshots.add(snapshot);
    }

    /**
     * Retrieves the external ID property of the patient node
     * @return String This returns the external ID
     */
    public String getExternalId() {
        return externalId;
    }

    /**
     *  Assigns value to the external ID of the Patient node
     * @param externalId Must not be empty and must be alphanumeric
     */
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    /**
     *  Retrieves the patients sex attribute from the patient object
     * @return String This returns the sex
     */
    public String getSex() {
        return sex;
    }

    /**
     * Assigns values to the patients sex
     * @param sex Cannot be empty, must either be M or F
     */
    public void setSex(String sex) {
        this.sex = sex;
    }

    /**
     * Retrieves the race from the patient node
     * @return String This returns race (patients physical characteristics)
     */
    public String getRace() {
        return race;
    }

    /**
     * Assigns value to the race attribute of the patients node
     * @param race Patient's physical characteristics such as skin, hair, ...
     */
    public void setRace(String race) {
        this.race = race;
    }

    /**
     * Retrieves the ethnicity attribute from the patient node
     * @return String This returns the ethnicity (patients origin)
     */
    public String getEthnicity() {
        return ethnicity;
    }

    /**
     * Assigns value to the ethnicity attribute of the patients node
     * @param ethnicity This is the patients origin by birth
     */
    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    /**
     * Retrieves a set of Patients snapshots from the patient snapshot node
     * @return Set This returns the patient snapshots
     */
    public Set<PatientSnapshot> getSnapshots() {
        return snapshots;
    }

    /**
     * Assigns value to the PatientSnapshot Object
     * @param snapshots This is a set made up of Patients record, age, and samples attached to the patient
     */
    public void setSnapshots(Set<PatientSnapshot> snapshots) {
        this.snapshots = snapshots;
    }

    /**
     * Retrieves the abbreviation of the external data source from the ExternalDataSource node
     * @return String This returns the source of the Patient's data abbreviated
     */
    public String getDataSource() {
        return dataSource;
    }

    /**
     * Assigns value to the source of the patients data
     * @param dataSource This is the abbreviated version of the source of patient's data
     */
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

        for(Group g : groups){
            if(g != null && g.getType().equals("Provider")) return g;
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

        PatientSnapshot latestPS = null;
        for(PatientSnapshot ps: snapshots){

            if(latestPS == null){
                latestPS = ps;
            }
            else{
                //compare age at collection
                if(latestPS.getAgeAtCollection().compareTo(ps.getAgeAtCollection()) < 0 ){

                    latestPS = ps;
                }
                //compare date collection
                else if(latestPS.getDateAtCollection().compareTo(ps.getDateAtCollection()) < 0){

                    latestPS = ps;
                }
            }

        }

        return latestPS;
    }

}

