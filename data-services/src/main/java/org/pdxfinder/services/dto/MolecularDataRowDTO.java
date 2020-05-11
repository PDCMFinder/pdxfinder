package org.pdxfinder.services.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@JsonPropertyOrder({
        "Sample Id",
        "HGNC Symbol",
        "Amino Acid Change",
        "Consequence",
        "Nucleotide Change",
        "Read Depth",
        "Allele Frequency",
        "Probe Id Affymetrix",
        "Log10 Rcna",
        "Log2 Rcna",
        "Copy Number Status",
        "Gistic Value",
        "Chromosome",
        "Seq. Start Position",
        "Seq. End Position",
        "Ref. Allele",
        "Alt Allele",
        "Rs Id Variant",
        "Ensembl Transcript Id",
        "Ensembl Gene Id",
        "Ucsc Gene Id",
        "Ncbi Gene Id",
        "Z-Score",
        "Genome Assembly",
        "Result"
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MolecularDataRowDTO {


    private String sampleId;
    private String chromosome;
    private String seqPosition;
    private String refAllele;
    private String altAllele;
    private String consequence;
    private String hgncSymbol;
    private String zscore;
    private String aminoAcidChange;
    private String readDepth;
    private String alleleFrequency;
    private String rsidVariants;
    private String nucleotideChange;
    private String genomeAssembly;
    private String seqStartPosition;
    private String seqEndPosition;
    private String strand;
    private String ensemblTranscriptId;
    private String ucscTranscriptId;
    private String ncbiTranscriptId;
    private String cdsChange;
    private String type;
    private String annotation;
    private String cytogeneticsResult;
    private String microsateliteResult;
    private String probeIdAffymetrix;
    private String cnaLog2rCna;
    private String cnaLog10rCna;
    private String cnaCopyNumberStatus;
    private String cnaGisticValue;
    private String cnaPicnicValue;


    public MolecularDataRowDTO() {
    }

    @JsonProperty("Sample Id")
    public String getSampleId() {
        return sampleId;
    }

    @JsonProperty("Sample Id")
    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    @JsonProperty("Chromosome")
    public String getChromosome() {
        return chromosome;
    }

    @JsonProperty("Chromosome")
    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    @JsonProperty("")
    public String getSeqPosition() {
        return seqPosition;
    }

    @JsonProperty("")
    public void setSeqPosition(String seqPosition) {
        this.seqPosition = seqPosition;
    }

    @JsonProperty("Ref. Allele")
    public String getRefAllele() {
        return refAllele;
    }

    @JsonProperty("Ref. Allele")
    public void setRefAllele(String refAllele) {
        this.refAllele = refAllele;
    }

    @JsonProperty("Alt Allele")
    public String getAltAllele() {
        return altAllele;
    }

    @JsonProperty("Alt Allele")
    public void setAltAllele(String altAllele) {
        this.altAllele = altAllele;
    }

    @JsonProperty("Consequence")
    public String getConsequence() {
        return consequence;
    }

    @JsonProperty("Consequence")
    public void setConsequence(String consequence) {
        this.consequence = consequence;
    }

    @JsonProperty("HGNC Symbol")
    public String getHgncSymbol() {
        return hgncSymbol;
    }

    @JsonProperty("HGNC Symbol")
    public void setHgncSymbol(String hgncSymbol) {
        this.hgncSymbol = hgncSymbol;
    }

    @JsonProperty("Z-Score")
    public String getZscore() {
        return zscore;
    }

    @JsonProperty("Z-Score")
    public void setZscore(String zscore) {
        this.zscore = zscore;
    }

    @JsonProperty("Amino Acid Change")
    public String getAminoAcidChange() {
        return aminoAcidChange;
    }

    @JsonProperty("Amino Acid Change")
    public void setAminoAcidChange(String aminoAcidChange) {
        this.aminoAcidChange = aminoAcidChange;
    }

    @JsonProperty("Read Depth")
    public String getReadDepth() {
        return readDepth;
    }

    @JsonProperty("Read Depth")
    public void setReadDepth(String readDepth) {
        this.readDepth = readDepth;
    }

    @JsonProperty("Allele Frequency")
    public String getAlleleFrequency() {
        return alleleFrequency;
    }

    @JsonProperty("Allele Frequency")
    public void setAlleleFrequency(String alleleFrequency) {
        this.alleleFrequency = alleleFrequency;
    }

    @JsonProperty("Rs Id Variant")
    public String getRsidVariants() {
        return rsidVariants;
    }

    @JsonProperty("Rs Id Variant")
    public void setRsidVariants(String rsidVariants) {
        this.rsidVariants = rsidVariants;
    }

    @JsonProperty("Nucleotide Change")
    public String getNucleotideChange() {
        return nucleotideChange;
    }

    @JsonProperty("Nucleotide Change")
    public void setNucleotideChange(String nucleotideChange) {
        this.nucleotideChange = nucleotideChange;
    }

    @JsonProperty("Genome Assembly")
    public String getGenomeAssembly() {
        return genomeAssembly;
    }

    @JsonProperty("Genome Assembly")
    public void setGenomeAssembly(String genomeAssembly) {
        this.genomeAssembly = genomeAssembly;
    }

    @JsonProperty("Seq. Start Position")
    public String getSeqStartPosition() {
        return seqStartPosition;
    }

    @JsonProperty("Seq. Start Position")
    public void setSeqStartPosition(String seqStartPosition) {
        this.seqStartPosition = seqStartPosition;
    }

    @JsonProperty("Seq. End Position")
    public String getSeqEndPosition() {
        return seqEndPosition;
    }

    @JsonProperty("Seq. End Position")
    public void setSeqEndPosition(String seqEndPosition) {
        this.seqEndPosition = seqEndPosition;
    }

    @JsonProperty("")
    public String getStrand() {
        return strand;
    }

    @JsonProperty("")
    public void setStrand(String strand) {
        this.strand = strand;
    }

    @JsonProperty("Ensembl Transcript Id")
    public String getEnsemblTranscriptId() {
        return ensemblTranscriptId;
    }

    @JsonProperty("Ensembl Transcript Id")
    public void setEnsemblTranscriptId(String ensemblTranscriptId) {
        this.ensemblTranscriptId = ensemblTranscriptId;
    }

    @JsonProperty("")
    public String getUcscTranscriptId() {
        return ucscTranscriptId;
    }

    @JsonProperty("")
    public void setUcscTranscriptId(String ucscTranscriptId) {
        this.ucscTranscriptId = ucscTranscriptId;
    }

    @JsonIgnore
    public String getNcbiTranscriptId() {
        return ncbiTranscriptId;
    }

    @JsonIgnore
    public void setNcbiTranscriptId(String ncbiTranscriptId) {
        this.ncbiTranscriptId = ncbiTranscriptId;
    }

    @JsonProperty("")
    public String getCdsChange() {
        return cdsChange;
    }

    @JsonProperty("")
    public void setCdsChange(String cdsChange) {
        this.cdsChange = cdsChange;
    }

    @JsonProperty("")
    public String getType() {
        return type;
    }

    @JsonProperty("")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("")
    public String getAnnotation() {
        return annotation;
    }

    @JsonProperty("")
    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    @JsonProperty("Result")
    public String getCytogeneticsResult() {
        return cytogeneticsResult;
    }

    @JsonProperty("Result")
    public void setCytogeneticsResult(String cytogeneticsResult) {
        this.cytogeneticsResult = cytogeneticsResult;
    }

    @JsonProperty("")
    public String getMicrosateliteResult() {
        return microsateliteResult;
    }

    @JsonProperty("")
    public void setMicrosateliteResult(String microsateliteResult) {
        this.microsateliteResult = microsateliteResult;
    }

    @JsonProperty("Probe Id Affymetrix")
    public String getProbeIdAffymetrix() {
        return probeIdAffymetrix;
    }

    @JsonProperty("Probe Id Affymetrix")
    public void setProbeIdAffymetrix(String probeIdAffymetrix) {
        this.probeIdAffymetrix = probeIdAffymetrix;
    }

    @JsonProperty("Log2 Rcna")
    public String getCnaLog2rCna() {
        return cnaLog2rCna;
    }

    @JsonProperty("Log2 Rcna")
    public void setCnaLog2rCna(String cnaLog2rCna) {
        this.cnaLog2rCna = cnaLog2rCna;
    }

    @JsonProperty("Log10 Rcna")
    public String getCnaLog10rCna() {
        return cnaLog10rCna;
    }

    @JsonProperty("Log10 Rcna")
    public void setCnaLog10rCna(String cnaLog10rCna) {
        this.cnaLog10rCna = cnaLog10rCna;
    }

    @JsonProperty("Copy Number Status")
    public String getCnaCopyNumberStatus() {
        return cnaCopyNumberStatus;
    }

    @JsonProperty("Copy Number Status")
    public void setCnaCopyNumberStatus(String cnaCopyNumberStatus) {
        this.cnaCopyNumberStatus = cnaCopyNumberStatus;
    }

    @JsonProperty("Gistic Value")
    public String getCnaGisticValue() {
        return cnaGisticValue;
    }

    @JsonProperty("Gistic Value")
    public void setCnaGisticValue(String cnaGisticValue) {
        this.cnaGisticValue = cnaGisticValue;
    }

    @JsonProperty("")
    public String getCnaPicnicValue() {
        return cnaPicnicValue;
    }

    @JsonProperty("")
    public void setCnaPicnicValue(String cnaPicnicValue) {
        this.cnaPicnicValue = cnaPicnicValue;
    }
}

