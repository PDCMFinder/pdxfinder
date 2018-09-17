package org.pdxfinder.services.dto;

import java.util.List;

public class PatientDTO {

    // Initial Diagnosis data
    private String age;
    private String ageAtDiagnosis;
    private String diseaseBodyLocation;
    private String CTEPSDCCode;
    private String diagnosisSubtype;
    private String raceAndEthnicity;

    // Additional Patient History
    private List<String> knownGeneticMutations;

    // Collection Events
    List<CollectionEventsDTO> collectionEvents;






}
