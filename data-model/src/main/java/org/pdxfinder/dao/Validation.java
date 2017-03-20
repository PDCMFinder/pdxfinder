package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

/**
 * Created by jmason on 17/03/2017.
 */
@NodeEntity
public class Validation {

    @GraphId
    Long id;

    @Relationship(type = "CHARACTERIZED_BY", direction = Relationship.INCOMING)
    private Set<MolecularCharacterization> molecularCharacterizations;

    public Validation() {
    }

    public Set<MolecularCharacterization> getMolecularCharacterizations() {
        return molecularCharacterizations;
    }

    public void setMolecularCharacterizations(Set<MolecularCharacterization> molecularCharacterizations) {
        this.molecularCharacterizations = molecularCharacterizations;
    }
}
