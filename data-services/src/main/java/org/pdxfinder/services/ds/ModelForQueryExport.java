package org.pdxfinder.services.ds;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({
        "modelId",
        "datasource",
        "externalId",
        "patientAge",
        "patientGender",
        "treatmentHistory",
        "diagnosis",
        "mappedOntologyTerm",
        "sampleOriginTissue",
        "sampleSampleSite",
        "sampleExtractionMethod",
        "sampleClassification",
        "sampleTumorType",
        "modelImplantationSite",
        "modelImplantationType",
        "modelHostStrain",
        "cancerSystem",
        "cancerOrgan",
        "cancerCellType",
        "dataAvailable",
        "mutatedVariants"
})
public class ModelForQueryExport {
    @JsonProperty("PDXFinder Id")
    private Long modelId;

    @JsonProperty("Datasource")
    private String datasource;

    @JsonProperty("Source Id")
    private String externalId;

    @JsonProperty("Patient Age")
    private String patientAge;

    @JsonProperty("Patient Gender")
    private String patientGender;

    @JsonProperty("Origin Tissue")
    private String sampleOriginTissue;

    @JsonProperty("Sample Site")
    private String sampleSampleSite;

    @JsonProperty("Origin Sample Extraction Method")
    private String sampleExtractionMethod;

    @JsonProperty("Classification")
    private String sampleClassification;

    @JsonProperty("Tumor Type")
    private String sampleTumorType;

    @JsonProperty("Cancer Systems")
    private List<String> cancerSystem;

    @JsonProperty("Patient Original Diagnosis")
    private String diagnosis;

    @JsonProperty("NCIT Mapped Ontology Term")
    private String mappedOntologyTerm;

    @JsonProperty("Patient Treatment History")
    private String treatmentHistory;

    @JsonProperty("Other Available Data")
    private List<String> dataAvailable;

    @JsonProperty("Mutations")
    private List<String> mutatedVariants;


    public ModelForQueryExport(ModelForQuery mfq) {
        this.modelId = mfq.getModelId();
        this.datasource = mfq.getDatasource();
        this.externalId = mfq.getExternalId();
        this.patientAge = mfq.getPatientAge();
        this.patientGender = mfq.getPatientGender();
        this.sampleOriginTissue = mfq.getSampleOriginTissue();
        this.sampleSampleSite = mfq.getSampleSampleSite();
        this.sampleExtractionMethod = mfq.getSampleExtractionMethod();
        this.sampleClassification = mfq.getSampleClassification();
        this.sampleTumorType = mfq.getSampleTumorType();
        this.cancerSystem = mfq.getCancerSystem();
        this.diagnosis = mfq.getDiagnosis();
        this.mappedOntologyTerm = mfq.getMappedOntologyTerm();
        this.treatmentHistory = mfq.getTreatmentHistory();
        this.dataAvailable = mfq.getDataAvailable();
        this.mutatedVariants = mfq.getMutatedVariants();
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(String patientAge) {
        this.patientAge = patientAge;
    }


    public String getPatientGender() {
        return patientGender;
    }

    public void setPatientGender(String patientGender) {
        this.patientGender = patientGender;
    }

    public String getSampleOriginTissue() {
        return sampleOriginTissue;
    }

    public void setSampleOriginTissue(String sampleOriginTissue) {
        this.sampleOriginTissue = sampleOriginTissue;
    }

    public String getSampleSampleSite() {
        return sampleSampleSite;
    }

    public void setSampleSampleSite(String sampleSampleSite) {
        this.sampleSampleSite = sampleSampleSite;
    }

    public String getSampleExtractionMethod() {
        return sampleExtractionMethod;
    }

    public void setSampleExtractionMethod(String sampleExtractionMethod) {
        this.sampleExtractionMethod = sampleExtractionMethod;
    }

    public String getSampleClassification() {
        return sampleClassification;
    }

    public void setSampleClassification(String sampleClassification) {
        this.sampleClassification = sampleClassification;
    }

    public String getSampleTumorType() {
        return sampleTumorType;
    }

    public void setSampleTumorType(String sampleTumorType) {
        this.sampleTumorType = sampleTumorType;
    }

    public List<String> getCancerSystem() {
        return cancerSystem;
    }

    public void setCancerSystem(List<String> cancerSystem) {
        this.cancerSystem = cancerSystem;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getMappedOntologyTerm() {
        return mappedOntologyTerm;
    }

    public void setMappedOntologyTerm(String mappedOntologyTerm) {
        this.mappedOntologyTerm = mappedOntologyTerm;
    }

    public String getTreatmentHistory() {
        return treatmentHistory;
    }

    public void setTreatmentHistory(String treatmentHistory) {
        this.treatmentHistory = treatmentHistory;
    }

    public List<String> getDataAvailable() {
        return dataAvailable;
    }

    public void setDataAvailable(List<String> dataAvailable) {
        this.dataAvailable = dataAvailable;
    }

    public List<String> getMutatedVariants() {
        return mutatedVariants;
    }

    public void setMutatedVariants(List<String> mutatedVariants) {
        this.mutatedVariants = mutatedVariants;
    }
}
