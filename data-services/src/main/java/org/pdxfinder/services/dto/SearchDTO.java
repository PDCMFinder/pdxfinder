package org.pdxfinder.services.dto;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.List;

/**
 * Represents an object with search results
 */
public class SearchDTO {


    private String dataSource;
    private String tumorId;
    private String diagnosis;
    private String tissueOfOrigin;
    private String tumorType;
    private String classification;
    private List<String> cancerGenomics;


    public SearchDTO() {
    }

    public String getDataSource() {
        return dataSource;
    }

    public String getTumorId() {
        return tumorId;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public String getTissueOfOrigin() {
        return tissueOfOrigin;
    }

    public String getTumorType() {
        return tumorType;
    }

    public String getClassification() {
        return classification;
    }

    public List<String> getCancerGenomics() {
        return cancerGenomics;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public void setTumorId(String tumorId) {
        this.tumorId = tumorId;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public void setTissueOfOrigin(String tissueOfOrigin) {
        this.tissueOfOrigin = tissueOfOrigin;
    }

    public void setTumorType(String tumorType) {
        this.tumorType = tumorType;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public void setCancerGenomics(List cancerGenomics) {
        this.cancerGenomics = cancerGenomics;
    }

}
