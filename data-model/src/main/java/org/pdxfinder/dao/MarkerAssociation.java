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
    /*
    * IRCC NGS mapping (class attribute = file headers):
    * chromosome = Chrom
    * seqPosition = Pos
    * refAllele = Ref
    * altAllele = Alt
    * consequence = Effect
    * aminoAcidChange = Protein
    * type = type
    * alleleFrequency = TBD
    * annotation = Drivers_v1.4 (0=NO, 1=YES)
    *
    *
    * */


    private String chromosome;
    private String seqPosition; //in jax use seqStartPosition instead of this
    private String refAllele;
    private String altAllele;
    private String consequence; // variant_classification in ircc
    private String aminoAcidChange; //use hgvsp short, remove p. in ircc
    private String rsVariants;
    private String readDepth;
    private String alleleFrequency;
    private String refAssembly; //NCBI_build in ircc

    private String seqStartPosition;
    private String seqEndPosition;
    private String strand;

    private String cdsChange;
    private String type; //Substitution
    private String annotation;

    //maybe use marker status in the future to have a general approach?
    private String immunoHistoChemistryResult;
    private String microsatelliteResult;


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

    /**
     * @return the altAllele
     */
    public String getAltAllele() {
        return altAllele;
    }

    /**
     * @param altAllele the altAllele to set
     */
    public void setAltAllele(String altAllele) {
        this.altAllele = altAllele;
    }

    /**
     * @return the refAssembly
     */
    public String getRefAssembly() {
        return refAssembly;
    }

    /**
     * @param refAssembly the refAssembly to set
     */
    public void setRefAssembly(String refAssembly) {
        this.refAssembly = refAssembly;
    }

    public String getSeqStartPosition() {
        return seqStartPosition;
    }

    public void setSeqStartPosition(String seqStartPosition) {
        this.seqStartPosition = seqStartPosition;
    }

    public String getSeqEndPosition() {
        return seqEndPosition;
    }

    public void setSeqEndPosition(String seqEndPosition) {
        this.seqEndPosition = seqEndPosition;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public String getCdsChange() {
        return cdsChange;
    }

    public void setCdsChange(String cdsChange) {
        this.cdsChange = cdsChange;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public String getImmunoHistoChemistryResult() {
        return immunoHistoChemistryResult;
    }

    public void setImmunoHistoChemistryResult(String immunoHistoChemistryResult) {
        this.immunoHistoChemistryResult = immunoHistoChemistryResult;
    }

    public String getMicrosatelliteResult() {
        return microsatelliteResult;
    }

    public void setMicrosatelliteResult(String microsatelliteResult) {
        this.microsatelliteResult = microsatelliteResult;
    }
}
