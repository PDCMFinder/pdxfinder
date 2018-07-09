package org.pdxfinder.transdatamodel;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.persistence.*;
import java.util.List;

/**
 * Created by abayomi on 20/11/2017.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "Model ID",
        "Patient ID",
        "Gender",
        "Age",
        "Race",
        "Ethnicity",
        "Specimen Site",
        "Primary Site",
        "Initial Diagnosis",
        "Clinical Diagnosis",
        "Tumor Type",
        "Stage Classification",
        "Stage Value",
        "Grade Classification",
        "Grade Value",
        "Sample Type",
        "Strain",
        "Mouse Sex",
        "Treatments",
        "Treatment Naive",
        "Engraftment Site",
        "Engraftment Type",
        "Source url",
        "Extraction Method",
        "Date At Collection",
        "Accessibility"
})
public class PdmrPdxInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(unique=true)
    private String modelID;

    private String patientID;
    private String gender;
    private String age;
    private String race;
    private String ethnicity;
    private String specimenSite;
    private String primarySite;
    private String initialDiagnosis;
    private String clinicalDiagnosis;
    private String tumorType;

    private String stageClassification;
    private String stageValue;
    private String gradeClassification;
    private String gradeValue;

    private String sampleType;
    private String strain;
    private String mouseSex;
    private String treatmentNaive;
    private String engraftmentSite;
    private String engraftmentType;
    private String sourceUrl;

    private String extractionMethod;
    private String dateAtCollection;
    private String accessibility;

    @OneToMany(mappedBy = "pdmrPdxInfo")
    private List<Treatment> treatments;


    public PdmrPdxInfo() {
    }


    public PdmrPdxInfo(String modelID, String patientID, String gender, String age, String race, String ethnicity, String specimenSite,
                       String primarySite, String initialDiagnosis, String clinicalDiagnosis, String tumorType, String stageClassification,
                       String stageValue, String gradeClassification, String gradeValue, String sampleType, String strain, String mouseSex,
                       String treatmentNaive, String engraftmentSite, String engraftmentType, String sourceUrl,
                       String extractionMethod, String dateAtCollection, String accessibility, List<Treatment> treatments) {
        this.modelID = modelID;
        this.patientID = patientID;
        this.gender = gender;
        this.age = age;
        this.race = race;
        this.ethnicity = ethnicity;
        this.specimenSite = specimenSite;
        this.primarySite = primarySite;
        this.initialDiagnosis = initialDiagnosis;
        this.clinicalDiagnosis = clinicalDiagnosis;
        this.tumorType = tumorType;
        this.stageClassification = stageClassification;
        this.stageValue = stageValue;
        this.gradeClassification = gradeClassification;
        this.gradeValue = gradeValue;
        this.sampleType = sampleType;
        this.strain = strain;
        this.mouseSex = mouseSex;
        this.treatmentNaive = treatmentNaive;
        this.engraftmentSite = engraftmentSite;
        this.engraftmentType = engraftmentType;
        this.sourceUrl = sourceUrl;
        this.extractionMethod = extractionMethod;
        this.dateAtCollection = dateAtCollection;
        this.accessibility = accessibility;
        this.treatments = treatments;
    }

    @JsonProperty("Model ID")
    public String getModelID() {
        return modelID;
    }

    @JsonProperty("Model ID")
    public void setModelID(String modelID) {
        this.modelID = modelID;
    }

    @JsonProperty("Patient ID")
    public String getPatientID() {
        return patientID;
    }

    @JsonProperty("Patient ID")
    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    @JsonProperty("Gender")
    public String getGender() {
        return gender;
    }

    @JsonProperty("Gender")
    public void setGender(String gender) {
        this.gender = gender;
    }

    @JsonProperty("Age")
    public String getAge() {
        return age;
    }

    @JsonProperty("Age")
    public void setAge(String age) {
        this.age = age;
    }

    @JsonProperty("Race")
    public String getRace() {
        return race;
    }

    @JsonProperty("Race")
    public void setRace(String race) {
        this.race = race;
    }

    @JsonProperty("Ethnicity")
    public String getEthnicity() {
        return ethnicity;
    }

    @JsonProperty("Ethnicity")
    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    @JsonProperty("Specimen Site")
    public String getSpecimenSite() {
        return specimenSite;
    }

    @JsonProperty("Specimen Site")
    public void setSpecimenSite(String specimenSite) {
        this.specimenSite = specimenSite;
    }

    @JsonProperty("Primary Site")
    public String getPrimarySite() {
        return primarySite;
    }

    @JsonProperty("Primary Site")
    public void setPrimarySite(String primarySite) {
        this.primarySite = primarySite;
    }

    @JsonProperty("Initial Diagnosis")
    public String getInitialDiagnosis() {
        return initialDiagnosis;
    }

    @JsonProperty("Initial Diagnosis")
    public void setInitialDiagnosis(String initialDiagnosis) {
        this.initialDiagnosis = initialDiagnosis;
    }

    @JsonProperty("Clinical Diagnosis")
    public String getClinicalDiagnosis() {
        return clinicalDiagnosis;
    }

    @JsonProperty("Clinical Diagnosis")
    public void setClinicalDiagnosis(String clinicalDiagnosis) {
        this.clinicalDiagnosis = clinicalDiagnosis;
    }

    @JsonProperty("Tumor Type")
    public String getTumorType() {
        return tumorType;
    }

    @JsonProperty("Tumor Type")
    public void setTumorType(String tumorType) {
        this.tumorType = tumorType;
    }

    @JsonProperty("Stage Classification")
    public String getStageClassification() {
        return stageClassification;
    }

    @JsonProperty("Stage Classification")
    public void setStageClassification(String stageClassification) {
        this.stageClassification = stageClassification;
    }

    @JsonProperty("Stage Value")
    public String getStageValue() {
        return stageValue;
    }


    @JsonProperty("Stage Value")
    public void setStageValue(String stageValue) {
        this.stageValue = stageValue;
    }

    @JsonProperty("Grade Classification")
    public String getGradeClassification() {
        return gradeClassification;
    }

    @JsonProperty("Grade Classification")
    public void setGradeClassification(String gradeClassification) {
        this.gradeClassification = gradeClassification;
    }

    @JsonProperty("Grade Value")
    public String getGradeValue() {
        return gradeValue;
    }

    @JsonProperty("Grade Value")
    public void setGradeValue(String gradeValue) {
        this.gradeValue = gradeValue;
    }

    @JsonProperty("Sample Type")
    public String getSampleType() {
        return sampleType;
    }

    @JsonProperty("Sample Type")
    public void setSampleType(String sampleType) {
        this.sampleType = sampleType;
    }

    @JsonProperty("Strain")
    public String getStrain() {
        return strain;
    }

    @JsonProperty("Strain")
    public void setStrain(String strain) {
        this.strain = strain;
    }

    @JsonProperty("Mouse Sex")
    public String getMouseSex() {
        return mouseSex;
    }

    @JsonProperty("Mouse Sex")
    public void setMouseSex(String mouseSex) {
        this.mouseSex = mouseSex;
    }

    @JsonProperty("Treatments")
    public void setTreatments(List<Treatment> treatments) {
        this.treatments = treatments;
    }

    @JsonProperty("Treatments")
    public List<Treatment> getTreatments() {
        return treatments;
    }

    @JsonProperty("Treatment Naive")
    public String getTreatmentNaive() {
        return treatmentNaive;
    }

    @JsonProperty("Treatment Naive")
    public void setTreatmentNaive(String treatmentNaive) {
        this.treatmentNaive = treatmentNaive;
    }

    @JsonProperty("Engraftment Site")
    public String getEngraftmentSite() {
        return engraftmentSite;
    }

    @JsonProperty("Engraftment Site")
    public void setEngraftmentSite(String engraftmentSite) {
        this.engraftmentSite = engraftmentSite;
    }

    @JsonProperty("Engraftment Type")
    public String getEngraftmentType() {
        return engraftmentType;
    }

    @JsonProperty("Engraftment Type")
    public void setEngraftmentType(String engraftmentType) {
        this.engraftmentType = engraftmentType;
    }

    @JsonProperty("Source url")
    public String getSourceUrl() {
        return sourceUrl;
    }

    @JsonProperty("Source url")
    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    @JsonProperty("Extraction Method")
    public String getExtractionMethod() {
        return extractionMethod;
    }

    @JsonProperty("Extraction Method")
    public void setExtractionMethod(String extractionMethod) {
        this.extractionMethod = extractionMethod;
    }

    @JsonProperty("Date At Collection")
    public String getDateAtCollection() {
        return dateAtCollection;
    }

    @JsonProperty("Date At Collection")
    public void setDateAtCollection(String dateAtCollection) {
        this.dateAtCollection = dateAtCollection;
    }

    @JsonProperty("Accessibility")
    public String getAccessibility() {
        return accessibility;
    }

    @JsonProperty("Accessibility")
    public void setAccessibility(String accessibility) {
        this.accessibility = accessibility;
    }
}



