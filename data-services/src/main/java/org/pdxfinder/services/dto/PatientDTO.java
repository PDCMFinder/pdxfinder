package org.pdxfinder.services.dto;

import java.util.List;

public class PatientDTO {

    // Initial Diagnosis data
    private String gender;
    private String ageAtDiagnosis;
    private String diseaseBodyLocation;
    private String CTEPSDCCode;
    private String diagnosisSubtype;
    private String raceAndEthnicity;
    private List<TreatmentSummaryDTO> treatmentSummaries;

    // Additional Patient History
    private List<String> knownGeneticMutations;

    // Collection Events
    List<CollectionEventsDTO> collectionEvents;

    public PatientDTO() {
    }

    public PatientDTO(String gender, String ageAtDiagnosis, String diseaseBodyLocation,
                      String CTEPSDCCode, String diagnosisSubtype, String raceAndEthnicity,
                      List<String> knownGeneticMutations, List<CollectionEventsDTO> collectionEvents) {
        this.gender = gender;
        this.ageAtDiagnosis = ageAtDiagnosis;
        this.diseaseBodyLocation = diseaseBodyLocation;
        this.CTEPSDCCode = CTEPSDCCode;
        this.diagnosisSubtype = diagnosisSubtype;
        this.raceAndEthnicity = raceAndEthnicity;
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

    public String getRaceAndEthnicity() {
        return raceAndEthnicity;
    }

    public void setRaceAndEthnicity(String raceAndEthnicity) {
        this.raceAndEthnicity = raceAndEthnicity;
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
}
