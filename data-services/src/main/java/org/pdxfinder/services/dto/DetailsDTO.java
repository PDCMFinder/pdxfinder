package org.pdxfinder.services.dto;

import org.pdxfinder.services.europepmc.Publication;

import java.util.List;
import java.util.Set;

/**
 * Created by csaba on 22/05/2017.
 */
public class DetailsDTO {


    /************************
     * DETAILS PAGE HEADER  *
     ************************/


    private String modelId;
    private String dataSource;
    private String mappedOntologyTermLabel;
    private String providerName;
    private String contactProviderLabel;
    private String contactProviderUrl;
    private String viewDataAtLabel;
    private String viewDataAtUrl;


    /************************
     * PDX MODEL TAB        *
     ************************/

    //PATIENT
    private String patientSex;
    private String ageAtTimeOfCollection;
    private String race;
    private String ethnicity;
    private List<String> relatedModels;

    //PATIENT TUMOR
    //private String histology; Use mappedOntologyTermLabel
    private String primaryTissue;
    private String collectionSite;
    private String tumorType;
    private String stage;
    private String stageClassification;
    private String grade;
    private String gradeClassification;

    //PDX MODEL ENGRAFTMENT
    private Set<EngraftmentDataDTO> pdxModelList;

    //MODEL QUALITY CONTROL
    private List<QualityControlDTO> modelQualityControl;


    /************************
     * PATIENT TAB          *
     ************************/
    private PatientDTO patient;


    /************************
     * MOLECULAR DATA TAB   *
     ************************/
    private List<MolecularDataEntryDTO> molecularDataRows;
    private int molecularDataEntrySize;
    private Set<String> dataTypes;




    /************************
     * DOSING STUDY TAB     *
     ************************/
    private String dosingStudyProtocolUrl;
    private List<DrugSummaryDTO> dosingStudy;
    private int dosingStudyNumbers;

    private List<Publication> publications;

    public DetailsDTO() {
        // Empty Constructor
    }


    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getMappedOntologyTermLabel() {
        return mappedOntologyTermLabel;
    }

    public void setMappedOntologyTermLabel(String mappedOntologyTermLabel) {
        this.mappedOntologyTermLabel = mappedOntologyTermLabel;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getContactProviderLabel() {
        return contactProviderLabel;
    }

    public void setContactProviderLabel(String contactProviderLabel) {
        this.contactProviderLabel = contactProviderLabel;
    }

    public String getContactProviderUrl() {
        return contactProviderUrl;
    }

    public void setContactProviderUrl(String contactProviderUrl) {
        this.contactProviderUrl = contactProviderUrl;
    }

    public String getViewDataAtLabel() {
        return viewDataAtLabel;
    }

    public void setViewDataAtLabel(String viewDataAtLabel) {
        this.viewDataAtLabel = viewDataAtLabel;
    }

    public String getViewDataAtUrl() {
        return viewDataAtUrl;
    }

    public void setViewDataAtUrl(String viewDataAtUrl) {
        this.viewDataAtUrl = viewDataAtUrl;
    }

    public String getPatientSex() {
        return patientSex;
    }

    public void setPatientSex(String patientSex) {
        this.patientSex = patientSex;
    }

    public String getAgeAtTimeOfCollection() {
        return ageAtTimeOfCollection;
    }

    public void setAgeAtTimeOfCollection(String ageAtTimeOfCollection) {
        this.ageAtTimeOfCollection = ageAtTimeOfCollection;
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

    public List<String> getRelatedModels() {
        return relatedModels;
    }

    public void setRelatedModels(List<String> relatedModels) {
        this.relatedModels = relatedModels;
    }

    public String getPrimaryTissue() {
        return primaryTissue;
    }

    public void setPrimaryTissue(String primaryTissue) {
        this.primaryTissue = primaryTissue;
    }

    public String getCollectionSite() {
        return collectionSite;
    }

    public void setCollectionSite(String collectionSite) {
        this.collectionSite = collectionSite;
    }

    public String getTumorType() {
        return tumorType;
    }

    public void setTumorType(String tumorType) {
        this.tumorType = tumorType;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getStageClassification() {
        return stageClassification;
    }

    public void setStageClassification(String stageClassification) {
        this.stageClassification = stageClassification;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getGradeClassification() {
        return gradeClassification;
    }

    public void setGradeClassification(String gradeClassification) {
        this.gradeClassification = gradeClassification;
    }

    public Set<EngraftmentDataDTO> getPdxModelList() {
        return pdxModelList;
    }

    public void setPdxModelList(Set<EngraftmentDataDTO> pdxModelList) {
        this.pdxModelList = pdxModelList;
    }

    public List<QualityControlDTO> getModelQualityControl() {
        return modelQualityControl;
    }

    public void setModelQualityControl(List<QualityControlDTO> modelQualityControl) {
        this.modelQualityControl = modelQualityControl;
    }

    public PatientDTO getPatient() {
        return patient;
    }

    public void setPatient(PatientDTO patient) {
        this.patient = patient;
    }

    public List<MolecularDataEntryDTO> getMolecularDataRows() {
        return molecularDataRows;
    }

    public void setMolecularDataRows(List<MolecularDataEntryDTO> molecularDataRows) {
        this.molecularDataRows = molecularDataRows;
    }

    public String getDosingStudyProtocolUrl() {
        return dosingStudyProtocolUrl;
    }

    public void setDosingStudyProtocolUrl(String dosingStudyProtocolUrl) {
        this.dosingStudyProtocolUrl = dosingStudyProtocolUrl;
    }

    public List<DrugSummaryDTO> getDosingStudy() {
        return dosingStudy;
    }

    public void setDosingStudy(List<DrugSummaryDTO> dosingStudy) {
        this.dosingStudy = dosingStudy;
    }

    public int getDosingStudyNumbers() {
        return dosingStudyNumbers;
    }

    public void setDosingStudyNumbers(int dosingStudyNumbers) {
        this.dosingStudyNumbers = dosingStudyNumbers;
    }

    public int getMolecularDataEntrySize() {
        return molecularDataEntrySize;
    }

    public void setMolecularDataEntrySize(int molecularDataEntrySize) {
        this.molecularDataEntrySize = molecularDataEntrySize;
    }

    public Set<String> getDataTypes() {
        return dataTypes;
    }

    public void setDataTypes(Set<String> dataTypes) {
        this.dataTypes = dataTypes;
    }

    public List<Publication> getPublications() {
        return publications;
    }

    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }
}
