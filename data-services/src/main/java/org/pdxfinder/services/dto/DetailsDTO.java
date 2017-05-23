package org.pdxfinder.services.dto;

import java.util.List;

/**
 * Created by csaba on 22/05/2017.
 */
public class DetailsDTO {

    private String externalId;
    private String dataSource;
    private String patientId;
    private String gender;
    private String age;
    private String race;
    private String ethnicity;

    private String diagnosis;
    private String tumorType;
    private String originTissue;
    private String sampleSite;
    private String classification;
    private List<String> cancerGenomics;

    private String sampleType;
    private String strain;
    private String mouseSex;
    private String engraftmentSite;

    public DetailsDTO() {
        this.externalId = "";
        this.dataSource = "";
        this.patientId = "";
        this.gender = "";
        this.age = "";
        this.race = "";
        this.ethnicity = "";

        this.diagnosis = "";
        this.tumorType = "";
        this.classification = "";
        this.originTissue = "";
        this.sampleSite = "";

        this.sampleType = "";
        this.strain = "";
        this.mouseSex = "";
        this.engraftmentSite = "";

    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTumorType() {
        return tumorType;
    }

    public void setTumorType(String tumorType) {
        this.tumorType = tumorType;
    }

    public String getOriginTissue() {
        return originTissue;
    }

    public void setOriginTissue(String originTissue) {
        this.originTissue = originTissue;
    }

    public String getSampleSite() {
        return sampleSite;
    }

    public void setSampleSite(String sampleSite) {
        this.sampleSite = sampleSite;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public List<String> getCancerGenomics() {
        return cancerGenomics;
    }

    public void setCancerGenomics(List<String> cancerGenomics) {
        this.cancerGenomics = cancerGenomics;
    }

    public String getSampleType() {
        return sampleType;
    }

    public void setSampleType(String sampleType) {
        this.sampleType = sampleType;
    }

    public String getStrain() {
        return strain;
    }

    public void setStrain(String strain) {
        this.strain = strain;
    }

    public String getMouseSex() {
        return mouseSex;
    }

    public void setMouseSex(String mouseSex) {
        this.mouseSex = mouseSex;
    }

    public String getEngraftmentSite() {
        return engraftmentSite;
    }

    public void setEngraftmentSite(String engraftmentSite) {
        this.engraftmentSite = engraftmentSite;
    }
}
