package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * Sample represents a piece of tissue taken from a specimen (human or mouse)
 * <p>
 * A sample could be cancerous or not (tissue used to compare to cancer sampled from a health tissue)
 */
@NodeEntity
public class Sample {

    @GraphId
    private Long id;

    private String sourceSampleId;
    private String diagnosis;
    private Tissue originTissue;
    private Tissue sampleSite;
    private String classification;
    private String dataSource;
    private ExternalDataSource externalDataSource;
    public Boolean normalTissue;

    @Relationship(type = "OF_TYPE", direction = Relationship.OUTGOING)
    private TumorType type;

    public Sample() {
        // Empty constructor required as of Neo4j API 2.0.5
    }

    public Sample(String sourceSampleId, TumorType type, String diagnosis, Tissue originTissue, Tissue sampleSite, String classification, ExternalDataSource externalDataSource) {
        this.sourceSampleId = sourceSampleId;
        this.type = type;
        this.diagnosis = diagnosis;
        this.originTissue = originTissue;
        this.sampleSite = sampleSite;
        this.classification = classification;
        this.dataSource = externalDataSource.getAbbreviation();
        this.externalDataSource = externalDataSource;
    }

}
