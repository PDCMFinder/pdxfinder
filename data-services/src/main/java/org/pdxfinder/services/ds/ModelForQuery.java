package org.pdxfinder.services.ds;

/*
 * Created by csaba on 19/01/2018.
 */




/*

Patient/tumour

    cancer categories/classification – higher category  - by organ (breast, lung, colon), system (digestive system) or cell type (adenocarcinoma)
    cancer histology - granular
    type- met primary other
    age – bin?
    gender
    Patient treatment naïve, pretreated, unknown

if not naive – being more granular on treatment if possible
PDX model

    host strain
    mouse humanization
    site of graft - back, right flank
    type of graft - ortho or SC
    validation : yes/no, method

 */

/*
FACET OPTIONS:

patientAge
patientTreatmentStatus
patientGender

sampleOriginTissue
sampleSampleSite
sampleExtractionMethod
sampleClassification
sampleTumorType

modelImplantationSite
modelImplantationType
modelHostStrain

*/


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@JsonPropertyOrder({
        "modelId",
        "datasource",
        "externalId",
        "patientAge",
        "patientGender",
        "patientTreatmentStatus",
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
        "dataAvailable"
})
public class ModelForQuery {

    @JsonProperty("PDXFinder Id")
    private Long modelId;

    @JsonProperty("Datasource")
    private String datasource;

    @JsonProperty("Source Id")
    private String externalId;

    @JsonProperty("Patient Age")
    private String patientAge;

    @JsonProperty("Patient Treatment Status")
    private String patientTreatmentStatus;

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

    @JsonProperty("Model Implantation Site")
    private String modelImplantationSite;

    @JsonProperty("Model Implantation Type")
    private String modelImplantationType;

    @JsonProperty("Model Host Strain")
    private Set<String> modelHostStrain;

    @JsonProperty("Cancer Systems")
    private List<String> cancerSystem;

    @JsonProperty("Cancer Organ")
    private String cancerOrgan;

    @JsonProperty("Cancer Histology")
    private String cancerCellType;

    @JsonProperty("Patient Original Diagnosis")
    private String diagnosis;

    @JsonProperty("NCIT Mapped Ontology Term")
    private String mappedOntologyTerm;

    @JsonProperty("Patient Treatment History")
    private String treatmentHistory;

    @JsonProperty("Other Available Data")
    private String dataAvailable;

    @JsonIgnore
    private Set<String> allOntologyTermAncestors;

    @JsonIgnore
    private Set<String> queryMatch;


    public ModelForQuery() {
    }


    public String getBy(SearchFacetName facet) {
        String s;
        switch (facet) {
            case datasource:
                s = datasource;
                break;
            case patient_age:
                s = patientAge;
                break;
            case patient_treatment_status:
                s = patientTreatmentStatus;
                break;
            case patient_gender:
                s = patientGender;
                break;
            case sample_origin_tissue:
                s = sampleOriginTissue;
                break;
            case sample_classification:
                s = sampleClassification;
                break;
            case sample_tumor_type:
                s = sampleTumorType;
                break;
            case model_implantation_site:
                s = modelImplantationSite;
                break;
            case model_implantation_type:
                s = modelImplantationType;
                break;
            case model_host_strain:
                s = modelHostStrain.stream().collect(Collectors.joining("::"));;
                break;
            case organ:
                s = cancerOrgan;
                break;
            case cancer_system:
                // Pass back the list of top level ontology systems delimited by "::"
                s = cancerSystem.stream().collect(Collectors.joining("::"));
                break;
            case cell_type:
                s = cancerCellType;
                break;
            default:
                s = null;
                break;
        }
        return s;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
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

    public String getPatientTreatmentStatus() {
        return patientTreatmentStatus;
    }

    public void setPatientTreatmentStatus(String patientTreatmentStatus) {
        this.patientTreatmentStatus = patientTreatmentStatus;
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

    public String getModelImplantationSite() {
        return modelImplantationSite;
    }

    public void setModelImplantationSite(String modelImplantationSite) {
        this.modelImplantationSite = modelImplantationSite;
    }

    public String getModelImplantationType() {
        return modelImplantationType;
    }

    public void setModelImplantationType(String modelImplantationType) {
        this.modelImplantationType = modelImplantationType;
    }

    public Set<String> getModelHostStrain() {
        return modelHostStrain;
    }

    public void setModelHostStrain(Set<String> modelHostStrain) {
        this.modelHostStrain = modelHostStrain;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public List<String> getCancerSystem() {
        return cancerSystem;
    }

    public void setCancerSystem(List<String> cancerSystem) {
        this.cancerSystem = cancerSystem;
    }

    public String getCancerOrgan() {
        return cancerOrgan;
    }

    public void setCancerOrgan(String cancerOrgan) {
        this.cancerOrgan = cancerOrgan;
    }

    public String getCancerCellType() {
        return cancerCellType;
    }

    public void setCancerCellType(String cancerCellType) {
        this.cancerCellType = cancerCellType;
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

    public String getDataAvailable() {
        return dataAvailable;
    }

    public void setDataAvailable(String dataAvailable) {
        this.dataAvailable = dataAvailable;
    }

    public Set<String> getAllOntologyTermAncestors() {
        return allOntologyTermAncestors;
    }

    public void setAllOntologyTermAncestors(Set<String> allOntologyTermAncestors) {
        this.allOntologyTermAncestors = allOntologyTermAncestors;
    }

    public Set<String> getQueryMatch() {
        return queryMatch;
    }

    public void setQueryMatch(Set<String> queryMatch) {
        this.queryMatch = queryMatch;
    }

    public String getFormattedQueryMatch(String query) {

        // Return nothing if there is no query
        if (query == null || query.length() < 1) {
            return null;
        }

        // Replace URL encoded spaces in the query string
        String normQuery = query.replaceAll("%20", " ").replaceAll("\\+", " ");

        // Return nothing if the query (case insensitively) matches the ontology term directly
        if (this.mappedOntologyTerm.toLowerCase().contains(normQuery.toLowerCase())) {
            return null;
        }

        // Return a string indicating which ontology term ancestors matches and highlight the match for the tooltip
        if (this.queryMatch != null && this.queryMatch.size() > 0) {
            List<String> s = new ArrayList<>(this.queryMatch);
            List<String> replaced = new ArrayList<>();
            for (String r : s) {
                // Find the case insensitive matched part of the term searched,
                // replace with original string surrounded with bold tags,
                // and do not allow wrapping at spaces
                replaced.add(r.replaceAll("(?i)(" + normQuery + ")", "<b>$1</b>").replaceAll(" ", "&nbsp;"));
            }
            return "Matches:<hr />" + StringUtils.join(replaced, "<br />");
        }
        return null;
    }

}
