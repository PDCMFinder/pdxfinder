package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

/**
 * Created by jmason on 17/03/2017.
 */
@NodeEntity
public class MolecularCharacterization {

    @GraphId
    Long id;

    String technology;

    // NOTE: These are not the only relationship types
    //       How can we model different relationship types
    //       between MolecularCharacterization and Marker
    //       For instance:
    //           MolChar(FISH) -[NEGATIVE]-> Marker(HER2)
    //                         -[NEGATIVE]-> Marker(ER)
    //                         -[NEGATIVE]-> Marker(PR)
    //           MolChar(MSS) -[STABILITY]-> Marker(MSI-L)

    @Relationship(type = "ASSOCIATED_WITH", direction = Relationship.OUTGOING)
    Set<MarkerAssociation> markerAssociations;


    public MolecularCharacterization() {
    }

    public MolecularCharacterization(String technology) {
        this.technology = technology;
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

    public Set<MarkerAssociation> getMarkerAssociations() {
        return markerAssociations;
    }

    public void setMarkerAssociations(Set<MarkerAssociation> markerAssociations) {
        this.markerAssociations = markerAssociations;
    }
}
