package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * Created by csaba on 25/04/2017.
 */
@NodeEntity
public class MarkerAssociation {

    @GraphId
    Long id;

    String description;
    Marker marker;
    
    private String chromosome;
    private String seqPosition;
    private String refAllele;
    private String consequence;
    private String aminoAcidChange;
    private String rsVariants;
    private String readDepth;
    private String alleleFrequency;

    public MarkerAssociation() {
    }

    public MarkerAssociation(String name, Marker marker) {
        this.description = name;
        this.marker = marker;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    /**
     * @return the chromosome
     */
    public String getChromosome() {
        return chromosome;
    }

    /**
     * @param chromosome the chromosome to set
     */
    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    /**
     * @return the seqPosition
     */
    public String getSeqPosition() {
        return seqPosition;
    }

    /**
     * @param seqPosition the seqPosition to set
     */
    public void setSeqPosition(String seqPosition) {
        this.seqPosition = seqPosition;
    }

    /**
     * @return the refAllele
     */
    public String getRefAllele() {
        return refAllele;
    }

    /**
     * @param refAllele the refAllele to set
     */
    public void setRefAllele(String refAllele) {
        this.refAllele = refAllele;
    }

    /**
     * @return the consequence
     */
    public String getConsequence() {
        return consequence;
    }

    /**
     * @param consequence the consequence to set
     */
    public void setConsequence(String consequence) {
        this.consequence = consequence;
    }

    /**
     * @return the aminoAcidChange
     */
    public String getAminoAcidChange() {
        return aminoAcidChange;
    }

    /**
     * @param aminoAcidChange the aminoAcidChange to set
     */
    public void setAminoAcidChange(String aminoAcidChange) {
        this.aminoAcidChange = aminoAcidChange;
    }

    /**
     * @return the rsVariants
     */
    public String getRsVariants() {
        return rsVariants;
    }

    /**
     * @param rsVariants the rsVariants to set
     */
    public void setRsVariants(String rsVariants) {
        this.rsVariants = rsVariants;
    }

    /**
     * @return the readDepth
     */
    public String getReadDepth() {
        return readDepth;
    }

    /**
     * @param readDepth the readDepth to set
     */
    public void setReadDepth(String readDepth) {
        this.readDepth = readDepth;
    }

    /**
     * @return the alleleFrequency
     */
    public String getAlleleFrequency() {
        return alleleFrequency;
    }

    /**
     * @param alleleFrequency the alleleFrequency to set
     */
    public void setAlleleFrequency(String alleleFrequency) {
        this.alleleFrequency = alleleFrequency;
    }


}
