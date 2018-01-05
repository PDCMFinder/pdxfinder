package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

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

    @Relationship(type = "TREATED_WITH")
    private Treatment treatment;

    @Relationship(type = "IMPLANTATION_SITE")
    private ImplantationSite implantationSite;

    @Relationship(type = "IMPLANTATION_TYPE")
    private ImplantationType implantationType;

    @Relationship(type = "BACKGROUND_STRAIN")
    private BackgroundStrain backgroundStrain;


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

    public Treatment getTreatment() {
        return treatment;
    }

    public void setTreatment(Treatment treatment) {
        this.treatment = treatment;
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

    public BackgroundStrain getBackgroundStrain() {
        return backgroundStrain;
    }

    public void setBackgroundStrain(BackgroundStrain backgroundStrain) {
        this.backgroundStrain = backgroundStrain;
    }
}
