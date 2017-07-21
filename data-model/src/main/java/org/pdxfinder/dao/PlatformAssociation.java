package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * Created by jmason on 21/07/2017.
 */
@NodeEntity
public class PlatformAssociation {

    @GraphId
    Long id;

    private String exon;
    private String seqStartPosition;
    private String seqEndPosition;

    @Relationship(type = "ASSOCIATED_WITH")
    private Marker marker;

    @Relationship(type = "ASSOCIATED_WITH")
    private Platform platform;


    public PlatformAssociation() {
    }

    public String getExon() {
        return exon;
    }

    public void setExon(String exon) {
        this.exon = exon;
    }

    public String getSeqStartPosition() {
        return seqStartPosition;
    }

    public void setSeqStartPosition(String seqStartPosition) {
        this.seqStartPosition = seqStartPosition;
    }

    public String getSeqEndPosition() {
        return seqEndPosition;
    }

    public void setSeqEndPosition(String seqEndPosition) {
        this.seqEndPosition = seqEndPosition;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }
}
