package org.pdxfinder.services.dto;

import org.neo4j.ogm.json.JSONArray;
import org.pdxfinder.graph.dao.*;

import java.util.HashMap;
import java.util.List;

public class LoaderDTO {

    private String modelID;
    private String sampleID;
    private String diagnosis;
    private String patientId;
    private String ethnicity;
    private String stage;
    private String grade;
    private String classification;
    private String age;
    private String gender;
    private String tumorType;
    private String sampleSite;
    private String primarySite;
    private String extractionMethod;

    private String implantationtypeStr;
    private String implantationSiteStr;
    private String strain;

    private String markerPlatform;
    private String markerStr;
    private String qaPassage;
    private String fingerprinting;
    private String modelTag;
    private String sourceURL;

    private JSONArray specimens;
    private JSONArray patientTreatments;
    private JSONArray modelDosingStudies;
    private JSONArray samplesArr;
    private JSONArray ValidationsArr;

    private Sample patientSample;
    private PatientSnapshot patientSnapshot;
    private QualityAssurance qualityAssurance;
    private List<ExternalUrl> externalUrls;

    private EngraftmentSite engraftmentSite;
    private EngraftmentType engraftmentType;

    private HostStrain nodScidGamma;
    private HostStrain nodScid;

    private Group projectGroup;
    private Group providerGroup;

    private ModelCreation modelCreation;
    private Patient patient;

    private HashMap<String, Image> histologyMap;

    private boolean skipModel;


    public LoaderDTO() {
    }

    public String getModelID() {
        return modelID;
    }

    public void setModelID(String modelID) {
        this.modelID = modelID;
    }

    public String getSampleID() {
        return sampleID;
    }

    public void setSampleID(String sampleID) {
        this.sampleID = sampleID;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getTumorType() {
        return tumorType;
    }

    public void setTumorType(String tumorType) {
        this.tumorType = tumorType;
    }

    public String getSampleSite() {
        return sampleSite;
    }

    public void setSampleSite(String sampleSite) {
        this.sampleSite = sampleSite;
    }

    public String getPrimarySite() {
        return primarySite;
    }

    public void setPrimarySite(String primarySite) {
        this.primarySite = primarySite;
    }

    public String getExtractionMethod() {
        return extractionMethod;
    }

    public void setExtractionMethod(String extractionMethod) {
        this.extractionMethod = extractionMethod;
    }

    public String getImplantationtypeStr() {
        return implantationtypeStr;
    }

    public void setImplantationtypeStr(String implantationtypeStr) {
        this.implantationtypeStr = implantationtypeStr;
    }

    public String getImplantationSiteStr() {
        return implantationSiteStr;
    }

    public void setImplantationSiteStr(String implantationSiteStr) {
        this.implantationSiteStr = implantationSiteStr;
    }

    public String getStrain() {
        return strain;
    }

    public void setStrain(String strain) {
        this.strain = strain;
    }

    public String getMarkerPlatform() {
        return markerPlatform;
    }

    public void setMarkerPlatform(String markerPlatform) {
        this.markerPlatform = markerPlatform;
    }

    public String getMarkerStr() {
        return markerStr;
    }

    public void setMarkerStr(String markerStr) {
        this.markerStr = markerStr;
    }

    public String getQaPassage() {
        return qaPassage;
    }

    public void setQaPassage(String qaPassage) {
        this.qaPassage = qaPassage;
    }

    public String getFingerprinting() {
        return fingerprinting;
    }

    public void setFingerprinting(String fingerprinting) {
        this.fingerprinting = fingerprinting;
    }

    public JSONArray getPatientTreatments() {
        return patientTreatments;
    }

    public void setPatientTreatments(JSONArray patientTreatments) {
        this.patientTreatments = patientTreatments;
    }

    public Sample getPatientSample() {
        return patientSample;
    }

    public void setPatientSample(Sample patientSample) {
        this.patientSample = patientSample;
    }

    public PatientSnapshot getPatientSnapshot() {
        return patientSnapshot;
    }

    public void setPatientSnapshot(PatientSnapshot patientSnapshot) {
        this.patientSnapshot = patientSnapshot;
    }

    public QualityAssurance getQualityAssurance() {
        return qualityAssurance;
    }

    public void setQualityAssurance(QualityAssurance qualityAssurance) {
        this.qualityAssurance = qualityAssurance;
    }

    public List<ExternalUrl> getExternalUrls() {
        return externalUrls;
    }

    public void setExternalUrls(List<ExternalUrl> externalUrls) {
        this.externalUrls = externalUrls;
    }

    public EngraftmentSite getEngraftmentSite() {
        return engraftmentSite;
    }

    public void setEngraftmentSite(EngraftmentSite engraftmentSite) {
        this.engraftmentSite = engraftmentSite;
    }

    public EngraftmentType getEngraftmentType() {
        return engraftmentType;
    }

    public void setEngraftmentType(EngraftmentType engraftmentType) {
        this.engraftmentType = engraftmentType;
    }

    public HostStrain getNodScidGamma() {
        return nodScidGamma;
    }

    public void setNodScidGamma(HostStrain nodScidGamma) {
        this.nodScidGamma = nodScidGamma;
    }

    public HostStrain getNodScid() {
        return nodScid;
    }

    public void setNodScid(HostStrain nodScid) {
        this.nodScid = nodScid;
    }

    public Group getProjectGroup() {
        return projectGroup;
    }

    public void setProjectGroup(Group projectGroup) {
        this.projectGroup = projectGroup;
    }

    public Group getProviderGroup() {
        return providerGroup;
    }

    public void setProviderGroup(Group providerGroup) {
        this.providerGroup = providerGroup;
    }

    public ModelCreation getModelCreation() {
        return modelCreation;
    }

    public void setModelCreation(ModelCreation modelCreation) {
        this.modelCreation = modelCreation;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public JSONArray getSpecimens() {
        return specimens;
    }

    public void setSpecimens(JSONArray specimens) {
        this.specimens = specimens;
    }

    public HashMap<String, Image> getHistologyMap() {
        return histologyMap;
    }

    public void setHistologyMap(HashMap<String, Image> histologyMap) {
        this.histologyMap = histologyMap;
    }

    public String getModelTag() {
        return modelTag;
    }

    public void setModelTag(String modelTag) {
        this.modelTag = modelTag;
    }

    public String getSourceURL() {
        return sourceURL;
    }

    public void setSourceURL(String sourceURL) {
        this.sourceURL = sourceURL;
    }

    public JSONArray getSamplesArr() {
        return samplesArr;
    }

    public void setSamplesArr(JSONArray samplesArr) {
        this.samplesArr = samplesArr;
    }

    public JSONArray getValidationsArr() {
        return ValidationsArr;
    }

    public void setValidationsArr(JSONArray validationsArr) {
        ValidationsArr = validationsArr;
    }

    public JSONArray getModelDosingStudies() {
        return modelDosingStudies;
    }

    public void setModelDosingStudies(JSONArray modelDosingStudies) {
        this.modelDosingStudies = modelDosingStudies;
    }

    public boolean isSkipModel() {
        return skipModel;
    }

    public void setSkipModel(boolean skipModel) {
        this.skipModel = skipModel;
    }
}
