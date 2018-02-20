package org.pdxfinder.services.dto;

import org.pdxfinder.dao.MarkerAssociation;
import org.pdxfinder.dao.MolecularCharacterization;
import org.pdxfinder.dao.Platform;
import org.pdxfinder.dao.Specimen;

import java.util.List;
import java.util.Set;

/**
 * Created by csaba on 22/05/2017.
 */
public class DetailsDTO {

    private String modelId;
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

    private String externalUrl;
    private String externalUrlText;
    private String mappedOntology;

    // Quality control information
    private String technology;
    private String description;
    private String passages;


    private List<Specimen> specimens;
    private Set<Platform> platforms;
    private Set<MolecularCharacterization>  molecularCharacterizations;
    private Set< Set<MarkerAssociation> > markerAssociations;
    private int totalPages;
    private int variationDataCount;

    public DetailsDTO() {
        this.modelId = "";
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
        this.externalUrl = "";
        this.externalUrlText = "";
        this.mappedOntology = "";

        this.technology = "";
        this.description = "";
        this.passages = "";

        this.totalPages = 0;
    }



    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
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

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public String getExternalUrlText() {
        return externalUrlText;
    }

    public void setExternalUrlText(String externalUrlText) {
        this.externalUrlText = externalUrlText;
    }

    public List<Specimen> getSpecimens() {
        return specimens;
    }

    public void setMappedOntology(String mappedOntology) {
        this.mappedOntology = mappedOntology;
    }

    public String getMappedOntology() {
        return mappedOntology;
    }

    public void setSpecimens(List<Specimen> specimens) {
        this.specimens = specimens;
    }

    public Set<Platform> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(Set<Platform> platforms) {
        this.platforms = platforms;
    }

    public Set< Set<MarkerAssociation> > getMarkerAssociations() {
        return markerAssociations;
    }

    public void setMarkerAssociations(Set< Set<MarkerAssociation> > markerAssociations) {
        this.markerAssociations = markerAssociations;
    }

    public Set<MolecularCharacterization>  getMolecularCharacterizations() {

        return molecularCharacterizations;
    }

    public void setMolecularCharacterizations(Set<MolecularCharacterization> molecularCharacterizations) {
        this.molecularCharacterizations = molecularCharacterizations;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getVariationDataCount() {
        return variationDataCount;
    }

    public void setVariationDataCount(int variationDataCount) {
        this.variationDataCount = variationDataCount;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPassages() {
        return passages;
    }

    public void setPassages(String passages) {
        this.passages = passages;
    }
}
