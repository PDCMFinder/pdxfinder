package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * Created by jmason on 06/06/2017.
 */
@NodeEntity
public class Specimen {

    @GraphId
    Long id;

    private String externalId;

    private String passage;


    @Relationship(type = "SAMPLED_FROM")
    private Sample sample;

    @Relationship(type = "TREATED_WITH", direction = Relationship.INCOMING)
    private TreatmentProtocol treatmentProtocol;

    @Relationship(type = "IMPLANTATION_SITE")
    private ImplantationSite implantationSite;

    @Relationship(type = "IMPLANTATION_TYPE")
    private ImplantationType implantationType;

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

    public HostStrain getHostStrain() {
        return hostStrain;
    }

    public void setHostStrain(HostStrain hostStrain) {
        this.hostStrain = hostStrain;
    }
}
