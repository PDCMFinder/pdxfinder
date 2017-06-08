package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

/**
 * Created by jmason on 06/06/2017.
 */
@NodeEntity
public class Specimen {

    @GraphId
    Long id;

    private String externalId;

    @Relationship(type = "CHARACTERIZED_BY", direction = Relationship.OUTGOING)
    private Set<MolecularCharacterization> molecularCharacterizations;

    @Relationship(type = "HISTOLOGY", direction = Relationship.OUTGOING)
    private Set<Histology> histology;

    @Relationship(type = "PASSAGED_FROM", direction = Relationship.INCOMING)
    private PdxPassage pdxPassage;


    public Specimen(String externalId, Set<MolecularCharacterization> molecularCharacterizations, Set<Histology> histology) {
        this.externalId = externalId;
        this.molecularCharacterizations = molecularCharacterizations;
        this.histology = histology;
    }

    public Specimen() {
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Set<MolecularCharacterization> getMolecularCharacterizations() {
        return molecularCharacterizations;
    }

    public void setMolecularCharacterizations(Set<MolecularCharacterization> molecularCharacterizations) {
        this.molecularCharacterizations = molecularCharacterizations;
    }

    public Set<Histology> getHistology() {
        return histology;
    }

    public void setHistology(Set<Histology> histology) {
        this.histology = histology;
    }

    public PdxPassage getPdxPassage() {
        return pdxPassage;
    }

    public void setPdxPassage(PdxPassage pdxPassage) {
        this.pdxPassage = pdxPassage;
    }
}
