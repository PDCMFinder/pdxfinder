package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * Represent the PDX model
 * The model will have at least one PdxPassage to capture the model creation event
 */
@NodeEntity
public class ModelCreation {

    @GraphId
    private Long id;

    private String sourcePdxId;

    @Relationship(type = "IMPLANTATION_SITE")
    private ImplantationSite implantationSite;

    @Relationship(type = "IMPLANTATION_TYPE")
    private ImplantationType implantationType;

    @Relationship(type = "BACKGROUND_STRAIN")
    private BackgroundStrain backgroundStrain;

    @Relationship(type = "QUALITY_ASSURED_BY")
    private QualityAssurance qualityAssurance;

    @Relationship(type = "IMPLANTED_IN", direction = Relationship.INCOMING)
    private Sample sample;

    public ModelCreation(String sourcePdxId, ImplantationSite implantationSite, ImplantationType implantationType, Sample sample, BackgroundStrain backgroundStrain, QualityAssurance qualityAssurance) {
        this.sourcePdxId = sourcePdxId;
        this.implantationSite = implantationSite;
        this.implantationType = implantationType;
        this.sample = sample;
        this.backgroundStrain = backgroundStrain;
        this.qualityAssurance = qualityAssurance;
    }

    public ModelCreation() {
        // Empty constructor required as of Neo4j API 2.0.5
    }

    public String getSourcePdxId() {
        return sourcePdxId;
    }

    public void setSourcePdxId(String sourcePdxId) {
        this.sourcePdxId = sourcePdxId;
    }

    public ImplantationSite getImplantationSite() {
        return implantationSite;
    }

    public void setImplantationSite(ImplantationSite implantationSite) {
        this.implantationSite = implantationSite;
    }

    public ImplantationType getImplantationType() {
        return implantationType;
    }

    public void setImplantationType(ImplantationType implantationType) {
        this.implantationType = implantationType;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public BackgroundStrain getBackgroundStrain() {
        return backgroundStrain;
    }

    public void setBackgroundStrain(BackgroundStrain backgroundStrain) {
        this.backgroundStrain = backgroundStrain;
    }

    public QualityAssurance getQualityAssurance() {
        return qualityAssurance;
    }

    public void setQualityAssurance(QualityAssurance qualityAssurance) {
        this.qualityAssurance = qualityAssurance;
    }
}
