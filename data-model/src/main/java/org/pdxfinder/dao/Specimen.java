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

    @Relationship(type = "HISTOLOGY")
    private Set<Histology> histology;

    @Relationship(type = "PASSAGED_FROM", direction = Relationship.INCOMING)
    private PdxPassage pdxPassage;

    @Relationship(type = "SAMPLED_FROM")
    private Sample sample;


    public Specimen(String externalId, Set<Histology> histology) {
        this.externalId = externalId;
        this.histology = histology;
    }

    public Specimen(String externalId, Set<Histology> histology, Sample sample) {
        this.externalId = externalId;
        this.histology = histology;
        this.sample = sample;
    }

    public Specimen() {
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setHistology(Set<Histology> histology){
        this.histology = histology;
    }
    
    public void addHistology(Histology histology){
        if(this.histology == null){
            this.histology = new HashSet<>();
            this.histology.add(histology);
        }else{
            this.histology.add(histology);
        }
    }
    
    public Set<Histology> getHistology(){
        return this.histology;
    }

    public PdxPassage getPdxPassage() {
        return pdxPassage;
    }

    public void setPdxPassage(PdxPassage pdxPassage) {
        this.pdxPassage = pdxPassage;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }
}
