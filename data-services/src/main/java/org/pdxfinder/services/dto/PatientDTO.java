package org.pdxfinder.services.dto;

import java.util.List;

public class PatientDTO {

    // Initial Diagnosis data
    private String gender;
    private String ageAtDiagnosis;
    private String diseaseBodyLocation;
    private String CTEPSDCCode;
    private String diagnosisSubtype;
    private String ethnicity;
    private List<TreatmentSummaryDTO> treatmentSummaries;

    private Boolean treatmentExists = false;
    private Boolean currentTreatmentExists = false;

    // Additional Patient History
    private List<String> knownGeneticMutations;

    // Collection Events
    List<CollectionEventsDTO> collectionEvents;

    public PatientDTO() {
    }

    public PatientDTO(String gender,
                      String ageAtDiagnosis,
                      String diseaseBodyLocation,
                      String CTEPSDCCode,
                      String diagnosisSubtype,
                      String ethnicity,
                      List<TreatmentSummaryDTO> treatmentSummaries,
                      Boolean treatmentExists,
                      Boolean currentTreatmentExists,
                      List<String> knownGeneticMutations,
                      List<CollectionEventsDTO> collectionEvents) {
        this.gender = gender;
        this.ageAtDiagnosis = ageAtDiagnosis;
        this.diseaseBodyLocation = diseaseBodyLocation;
        this.CTEPSDCCode = CTEPSDCCode;
        this.diagnosisSubtype = diagnosisSubtype;
        this.ethnicity = ethnicity;
        this.treatmentSummaries = treatmentSummaries;
        this.treatmentExists = treatmentExists;
        this.currentTreatmentExists = currentTreatmentExists;
        this.knownGeneticMutations = knownGeneticMutations;
        this.collectionEvents = collectionEvents;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAgeAtDiagnosis() {
        return ageAtDiagnosis;
    }

    public void setAgeAtDiagnosis(String ageAtDiagnosis) {
        this.ageAtDiagnosis = ageAtDiagnosis;
    }

    public String getDiseaseBodyLocation() {
        return diseaseBodyLocation;
    }

    public void setDiseaseBodyLocation(String diseaseBodyLocation) {
        this.diseaseBodyLocation = diseaseBodyLocation;
    }

    public String getCTEPSDCCode() {
        return CTEPSDCCode;
    }

    public void setCTEPSDCCode(String CTEPSDCCode) {
        this.CTEPSDCCode = CTEPSDCCode;
    }

    public String getDiagnosisSubtype() {
        return diagnosisSubtype;
    }

    public void setDiagnosisSubtype(String diagnosisSubtype) {
        this.diagnosisSubtype = diagnosisSubtype;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public List<String> getKnownGeneticMutations() {
        return knownGeneticMutations;
    }

    public void setKnownGeneticMutations(List<String> knownGeneticMutations) {
        this.knownGeneticMutations = knownGeneticMutations;
    }

    public List<CollectionEventsDTO> getCollectionEvents() {
        return collectionEvents;
    }

    public void setCollectionEvents(List<CollectionEventsDTO> collectionEvents) {
        this.collectionEvents = collectionEvents;
    }

    public void setTreatmentSummaries(List<TreatmentSummaryDTO> treatmentSummaries) {
        this.treatmentSummaries = treatmentSummaries;
    }

    public List<TreatmentSummaryDTO> getTreatmentSummaries() {
        return treatmentSummaries;
    }

    public void setCurrentTreatmentExists(Boolean currentTreatmentExists) {
        this.currentTreatmentExists = currentTreatmentExists;
    }

    public Boolean getCurrentTreatmentExists() {
        return currentTreatmentExists;
    }

    public void setTreatmentExists(Boolean treatmentExists) {
        this.treatmentExists = treatmentExists;
    }

    public Boolean getTreatmentExists() {
        return treatmentExists;
    }
}
