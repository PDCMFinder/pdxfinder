package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jmason on 17/03/2017.
 */
@NodeEntity
public class MolecularCharacterization {

    @GraphId
    Long id;

    @Deprecated
    private String technology;

    private String type;

    @Relationship(type = "PLATFORM_USED")
    Platform platform;

    @Relationship(type = "ASSOCIATED_WITH")
    List<MarkerAssociation> markerAssociations;

    public MolecularCharacterization() {
    }

    public MolecularCharacterization(String technology) {
        this.technology = technology;
    }

    public MolecularCharacterization(Platform platform) {
        this.platform = platform;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public List<MarkerAssociation> getMarkerAssociations() {
        return markerAssociations;
    }

    public void setMarkerAssociations(List<MarkerAssociation> markerAssociations) {
        this.markerAssociations = markerAssociations;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addMarkerAssociation(MarkerAssociation ma){

        if(this.markerAssociations == null){
            this.markerAssociations = new ArrayList<>();
        }
        this.markerAssociations.add(ma);

    }
}
