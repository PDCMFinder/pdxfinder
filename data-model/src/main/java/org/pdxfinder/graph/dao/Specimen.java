package org.pdxfinder.graph.dao;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by jmason on 06/06/2017.
 */
@NodeEntity
public class Specimen {

    @Id
    @GeneratedValue
    private Long id;

    private String externalId;

    private String passage;


    @Relationship(type = "SAMPLED_FROM")
    private Sample sample;

    @Relationship(type = "TREATED_WITH", direction = Relationship.INCOMING)
    private TreatmentProtocol treatmentProtocol;

    @Relationship(type = "ENGRAFTMENT_SITE")
    private EngraftmentSite engraftmentSite;

    @Relationship(type = "ENGRAFTMENT_TYPE")
    private EngraftmentType engraftmentType;

    @Relationship(type = "ENGRAFTMENT_MATERIAL")
    private EngraftmentMaterial engraftmentMaterial;

    @Relationship(type = "HOST_STRAIN")
    private HostStrain hostStrain;


    public Specimen() {
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }


    public String getPassage() {
        return passage;
    }

    public void setPassage(String passage) {
        this.passage = passage;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public TreatmentProtocol getTreatment() {
        return treatmentProtocol;
    }

    public void setTreatment(TreatmentProtocol treatment) {
        this.treatmentProtocol = treatment;
    }

    public EngraftmentSite getEngraftmentSite() {
        return engraftmentSite;
    }

    public void setEngraftmentSite(EngraftmentSite engraftmentSite) {
        this.engraftmentSite = engraftmentSite;
    }

    public EngraftmentType getEngraftmentType() {
        return engraftmentType;
    }

    public void setEngraftmentType(EngraftmentType engraftmentType) {
        this.engraftmentType = engraftmentType;
    }

    public EngraftmentMaterial getEngraftmentMaterial() {
        return engraftmentMaterial;
    }

    public void setEngraftmentMaterial(EngraftmentMaterial engraftmentMaterial) {
        this.engraftmentMaterial = engraftmentMaterial;
    }

    public HostStrain getHostStrain() {
        return hostStrain;
    }

    public void setHostStrain(HostStrain hostStrain) {
        this.hostStrain = hostStrain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Specimen specimen = (Specimen) o;

        return new EqualsBuilder()
            .append(getPassage(), specimen.getPassage())
            .append(getHostStrain(), specimen.getHostStrain())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getPassage())
            .append(getHostStrain())
            .toHashCode();
    }

    @Override
    public String toString() {
        return String.format("[%s - %s]", getPassage(), getHostStrain().getSymbol());
    }

}
