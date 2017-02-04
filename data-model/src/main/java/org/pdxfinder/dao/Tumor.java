package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class Tumor {

    @GraphId
    private Long id;

    private String sourceTumorId;
    private String diagnosis;
    private Tissue originTissue;
    private Tissue tumorSite;
    private String classification;
    private String dataSource;
    private ExternalDataSource externalDataSource;

    @Relationship(type = "OF_TYPE", direction = Relationship.OUTGOING)
    private TumorType type;

    public Tumor() {
        // Empty constructor required as of Neo4j API 2.0.5
    }

    public Tumor(String sourceTumorId, TumorType type, String diagnosis, Tissue originTissue, Tissue tumorSite, String classification, ExternalDataSource externalDataSource) {
        this.sourceTumorId = sourceTumorId;
        this.type = type;
        this.diagnosis = diagnosis;
        this.originTissue = originTissue;
        this.tumorSite = tumorSite;
        this.classification = classification;
        this.dataSource = externalDataSource.getAbbreviation();
        this.externalDataSource = externalDataSource;
    }

    public String getSourceTumorId() {
        return sourceTumorId;
    }

    public void setSourceTumorId(String sourceTumorId) {
        this.sourceTumorId = sourceTumorId;
    }

    public TumorType getType() {
        return type;
    }

    public void setType(TumorType type) {
        this.type = type;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public Tissue getOriginTissue() {
        return originTissue;
    }

    public void setOriginTissue(Tissue originTissue) {
        this.originTissue = originTissue;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public Tissue getTumorSite() {
        return tumorSite;
    }

    public void setTumorSite(Tissue tumorSite) {
        this.tumorSite = tumorSite;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public ExternalDataSource getExternalDataSource() {
        return externalDataSource;
    }

    public void setExternalDataSource(ExternalDataSource externalDataSource) {
        this.externalDataSource = externalDataSource;
    }
}
