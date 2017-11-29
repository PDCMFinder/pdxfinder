package org.pdxfinder.services.dto;

import java.util.List;

/**
 * Represents an object with search results
 */
public class SearchDTO {


    private String dataSource;
    private String modelId;
    private String tumorId;
    private String diagnosis;
    private String tissueOfOrigin;
    private String tumorType;
    private String classification;
    private List<String> cancerGenomics;

    private String searchParameter;
    private String searchDepth;
    private String mappedOntology;


    public SearchDTO() {
        this.dataSource = "";
        this.modelId = "";
        this.tumorId = "";
        this.diagnosis = "";
        this.tissueOfOrigin = "";
        this.tumorType = "";
        this.classification = "";
        this.searchParameter = "";
        this.searchDepth = "";
        this.mappedOntology = "";
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getTumorId() {
        return tumorId;
    }

    public void setTumorId(String tumorId) {
        this.tumorId = tumorId;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTissueOfOrigin() {
        return tissueOfOrigin;
    }

    public void setTissueOfOrigin(String tissueOfOrigin) {
        this.tissueOfOrigin = tissueOfOrigin;
    }

    public String getTumorType() {
        return tumorType;
    }

    public void setTumorType(String tumorType) {
        this.tumorType = tumorType;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getSearchParameter() { return searchParameter; }

    public void setSearchParameter(String searchParameter) { this.searchParameter = searchParameter; }

    public String getSearchDepth() {  return searchDepth; }

    public void setSearchDepth(String searchDepth) { this.searchDepth = searchDepth; }

    public List<String> getCancerGenomics() {
        return cancerGenomics;
    }

    public void setCancerGenomics(List<String> cancerGenomics) {
        this.cancerGenomics = cancerGenomics;
    }

    public String getMappedOntology() {
        return mappedOntology;
    }

    public void setMappedOntology(String mappedOntology) {
        this.mappedOntology = mappedOntology;
    }
}
