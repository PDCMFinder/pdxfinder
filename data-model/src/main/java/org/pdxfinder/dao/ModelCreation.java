package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * Represent the PDX model
 * The model will have at least one PdxPassage to capture the model creation event
 */
@NodeEntity
public class ModelCreation {

    @GraphId
    private Long id;

    private String sourcePdxId;
    private String dataSource;


    @Relationship(type = "QUALITY_ASSURED_BY")
    private QualityAssurance qualityAssurance;

    @Relationship(type = "IMPLANTED_IN", direction = Relationship.INCOMING)
    private Sample sample;

    @Relationship(type= "MODEL_SAMPLE_RELATION", direction = Relationship.INCOMING)
    private Set<Sample> relatedSamples;

    @Relationship(type = "SPECIMENS", direction = Relationship.INCOMING)
    private Set<Specimen> specimens;

    @Relationship(type = "SUMMARY_OF_TREATMENT", direction = Relationship.INCOMING)
    private TreatmentSummary treatmentSummary;


    public ModelCreation(String sourcePdxId, String dataSource, Sample sample, QualityAssurance qualityAssurance) {
        this.sourcePdxId = sourcePdxId;
        this.dataSource = dataSource;
        this.sample = sample;
        this.qualityAssurance = qualityAssurance;
    }

    public ModelCreation() {
        // Empty constructor required as of Neo4j API 2.0.5
    }


    public Long getId() {
        return id;
    }

    public String getSourcePdxId() {
        return sourcePdxId;
    }

    public void setSourcePdxId(String sourcePdxId) {
        this.sourcePdxId = sourcePdxId;
    }

    public QualityAssurance getQualityAssurance() {
        return qualityAssurance;
    }

    public void setQualityAssurance(QualityAssurance qualityAssurance) {
        this.qualityAssurance = qualityAssurance;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public Set<Sample> getRelatedSamples() {
        return relatedSamples;
    }

    public void setRelatedSamples(Set<Sample> relatedSamples) {
        this.relatedSamples = relatedSamples;
    }

    public void addRelatedSample(Sample sample){

        if(this.relatedSamples == null){
            this.relatedSamples = new HashSet<>();
        }

        this.relatedSamples.add(sample);
    }

    public Set<Specimen> getSpecimens() {
        return specimens;
    }

    public void setSpecimens(Set<Specimen> specimens) {
        this.specimens = specimens;
    }

    public void addSpecimen(Specimen specimen){

        if(this.specimens == null){
            this.specimens = new HashSet<>();
        }

        this.specimens.add(specimen);
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public TreatmentSummary getTreatmentSummary() {
        return treatmentSummary;
    }

    public void setTreatmentSummary(TreatmentSummary treatmentSummary) {
        this.treatmentSummary = treatmentSummary;
    }
}
