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

    @Relationship(type = "POSITIVE", direction = Relationship.OUTGOING)
    Set<Marker> positiveMarkers;

    @Relationship(type = "NEGATIVE", direction = Relationship.OUTGOING)
    Set<Marker> negativeMarkers;

    public MolecularCharacterization() {
    }

    public MolecularCharacterization(String technology) {
        this.technology = technology;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public Set<Marker> getPositiveMarkers() {
        return positiveMarkers;
    }

    public void setPositiveMarkers(Set<Marker> positiveMarkers) {
        this.positiveMarkers = positiveMarkers;
    }

    public Set<Marker> getNegativeMarkers() {
        return negativeMarkers;
    }

    public void setNegativeMarkers(Set<Marker> negativeMarkers) {
        this.negativeMarkers = negativeMarkers;
    }
}
