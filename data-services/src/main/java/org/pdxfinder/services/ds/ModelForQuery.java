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
modelBackgroundStrain

*/


public class ModelForQuery {

    private Long modelId;

    private String datasource;

    private String patientAge;
    private String patientTreatmentStatus;
    private String patientGender;

    private String sampleOriginTissue;
    private String sampleSampleSite;
    private String sampleExtractionMethod;
    private String sampleClassification;
    private String sampleTumorType;

    private String modelImplantationSite;
    private String modelImplantationType;
    private String modelBackgroundStrain;

    public ModelForQuery() {
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
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

    public String getModelBackgroundStrain() {
        return modelBackgroundStrain;
    }

    public void setModelBackgroundStrain(String modelBackgroundStrain) {
        this.modelBackgroundStrain = modelBackgroundStrain;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }
}
