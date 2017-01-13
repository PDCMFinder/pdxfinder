package org.pdxi.dao;

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

    @Relationship(type = "TYPE", direction = Relationship.INCOMING)
    private TumorType type;

    public Tumor(String sourceTumorId, TumorType type, String diagnosis, Tissue originTissue, Tissue tumorSite, String classification) {
        this.sourceTumorId = sourceTumorId;
        this.type = type;
        this.diagnosis = diagnosis;
        this.originTissue = originTissue;
        this.tumorSite = tumorSite;
        this.classification = classification;
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
}
