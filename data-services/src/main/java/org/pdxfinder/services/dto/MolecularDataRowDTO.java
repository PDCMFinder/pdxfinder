package org.pdxfinder.services.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.pdxfinder.services.dto.pdxgun.Reference;


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
        "Variant Type",
        "Ensembl Transcript Id",
        "Ensembl Gene Id",
        "Ucsc Gene Id",
        "Ncbi Gene Id",
        "RNAseq Count",
        "Z-Score",
        "Genome Assembly",
        "Result",
        "Illumina HGEA Exp",
        "Rs Id Variant"
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MolecularDataRowDTO {


    private String sampleId;
    private String chromosome;
    private String seqPosition;
    private String refAllele;
    private String altAllele;
    private String consequence;
    private Reference hgncSymbol;
    protected String rnaSeqCount;
    private String zscore;
    private Reference aminoAcidChange;
    private String readDepth;
    private String alleleFrequency;
    private String variantClass;
    private String existingVariation;
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
    private String illuminaHGEAExp;


    public MolecularDataRowDTO() {
    }

    @JsonProperty("Sample Id")
    public String getSampleId() {
        return sampleId;
    }

    @JsonProperty("Sample Id")
    public MolecularDataRowDTO setSampleId(String sampleId) {
        this.sampleId = sampleId;
        return this;
    }

    @JsonProperty("Chromosome")
    public String getChromosome() {
        return chromosome;
    }

    @JsonProperty("Chromosome")
    public MolecularDataRowDTO setChromosome(String chromosome) {
        this.chromosome = chromosome;
        return this;
    }

    @JsonProperty("")
    public String getSeqPosition() {
        return seqPosition;
    }

    @JsonProperty("")
    public MolecularDataRowDTO setSeqPosition(String seqPosition) {
        this.seqPosition = seqPosition;
        return this;
    }

    @JsonProperty("Ref. Allele")
    public String getRefAllele() {
        return refAllele;
    }

    @JsonProperty("Ref. Allele")
    public MolecularDataRowDTO setRefAllele(String refAllele) {
        this.refAllele = refAllele;
        return this;
    }

    @JsonProperty("Alt Allele")
    public String getAltAllele() {
        return altAllele;
    }
    @JsonProperty("Alt Allele")
    public MolecularDataRowDTO setAltAllele(String altAllele) {
        this.altAllele = altAllele;
        return this;
    }

    @JsonProperty("Consequence")
    public String getConsequence() {
        return consequence;
    }

    @JsonProperty("Consequence")
    public MolecularDataRowDTO setConsequence(String consequence) {
        this.consequence = consequence;
        return this;
    }

    @JsonProperty("HGNC Symbol")
    public Reference getHgncSymbol() {
        return hgncSymbol;
    }

    @JsonProperty("HGNC Symbol")
    public MolecularDataRowDTO setHgncSymbol(Reference hgncSymbol) {
        this.hgncSymbol = hgncSymbol;
        return this;
    }

    @JsonProperty("RNAseq Count")
    public String getRnaSeqCount() {
        return rnaSeqCount;
    }

    @JsonProperty("RNAseq Count")
    public MolecularDataRowDTO setRnaSeqCount(String rnaSeqCount) {
        this.rnaSeqCount = rnaSeqCount;
        return this;
    }

    @JsonProperty("Z-Score")
    public String getZscore() {
        return zscore;
    }

    @JsonProperty("Z-Score")
    public MolecularDataRowDTO setZscore(String zscore) {
        this.zscore = zscore;
        return this;
    }

    @JsonProperty("Amino Acid Change")
    public Reference getAminoAcidChange() {
        return aminoAcidChange;
    }

    @JsonProperty("Amino Acid Change")
    public MolecularDataRowDTO setAminoAcidChange(Reference aminoAcidChange) {
        this.aminoAcidChange = aminoAcidChange;
        return this;
    }

    @JsonProperty("Read Depth")
    public String getReadDepth() {
        return readDepth;
    }

    @JsonProperty("Read Depth")
    public MolecularDataRowDTO setReadDepth(String readDepth) {
        this.readDepth = readDepth;
        return this;
    }

    @JsonProperty("Allele Frequency")
    public String getAlleleFrequency() {
        return alleleFrequency;
    }

    @JsonProperty("Allele Frequency")
    public MolecularDataRowDTO setAlleleFrequency(String alleleFrequency) {
        this.alleleFrequency = alleleFrequency;
        return this;
    }

    @JsonProperty("Variant Type")
    public String getVariantClass() {
        return variantClass;
    }

    @JsonProperty("Variant Type")
    public MolecularDataRowDTO setVariantClass(String variantClass) {
        this.variantClass = variantClass;
        return this;
    }

    @JsonProperty("Rs Id Variant")
    public String getExistingVariation() {
        return existingVariation;
    }

    @JsonProperty("Rs Id Variant")
    public MolecularDataRowDTO setExistingVariation(String existingVariation) {
        this.existingVariation = existingVariation;
        return this;
    }

    @JsonProperty("Nucleotide Change")
    public String getNucleotideChange() {
        return nucleotideChange;
    }

    @JsonProperty("Nucleotide Change")
    public MolecularDataRowDTO setNucleotideChange(String nucleotideChange) {
        this.nucleotideChange = nucleotideChange;
        return this;
    }

    @JsonProperty("Genome Assembly")
    public String getGenomeAssembly() {
        return genomeAssembly;
    }

    @JsonProperty("Genome Assembly")
    public MolecularDataRowDTO setGenomeAssembly(String genomeAssembly) {
        this.genomeAssembly = genomeAssembly;
        return this;
    }

    @JsonProperty("Seq. Start Position")
    public String getSeqStartPosition() {
        return seqStartPosition;
    }

    @JsonProperty("Seq. Start Position")
    public MolecularDataRowDTO setSeqStartPosition(String seqStartPosition) {
        this.seqStartPosition = seqStartPosition;
        return this;
    }

    @JsonProperty("Seq. End Position")
    public String getSeqEndPosition() {
        return seqEndPosition;
    }

    @JsonProperty("Seq. End Position")
    public MolecularDataRowDTO setSeqEndPosition(String seqEndPosition) {
        this.seqEndPosition = seqEndPosition;
        return this;
    }

    @JsonProperty("")
    public String getStrand() {
        return strand;
    }

    @JsonProperty("")
    public MolecularDataRowDTO setStrand(String strand) {
        this.strand = strand;
        return this;
    }

    @JsonProperty("Ensembl Transcript Id")
    public String getEnsemblTranscriptId() {
        return ensemblTranscriptId;
    }

    @JsonProperty("Ensembl Transcript Id")
    public MolecularDataRowDTO setEnsemblTranscriptId(String ensemblTranscriptId) {
        this.ensemblTranscriptId = ensemblTranscriptId;
        return this;
    }

    @JsonProperty("")
    public String getUcscTranscriptId() {
        return ucscTranscriptId;
    }

    @JsonProperty("")
    public MolecularDataRowDTO setUcscTranscriptId(String ucscTranscriptId) {
        this.ucscTranscriptId = ucscTranscriptId;
        return this;
    }

    @JsonIgnore
    public String getNcbiTranscriptId() {
        return ncbiTranscriptId;
    }

    @JsonIgnore
    public MolecularDataRowDTO setNcbiTranscriptId(String ncbiTranscriptId) {
        this.ncbiTranscriptId = ncbiTranscriptId;
        return this;
    }

    @JsonProperty("")
    public String getCdsChange() {
        return cdsChange;
    }

    @JsonProperty("")
    public MolecularDataRowDTO setCdsChange(String cdsChange) {
        this.cdsChange = cdsChange;
        return this;
    }

    @JsonProperty("")
    public String getType() {
        return type;
    }

    @JsonProperty("")
    public MolecularDataRowDTO setType(String type) {
        this.type = type;
        return this;
    }

    @JsonProperty("")
    public String getAnnotation() {
        return annotation;
    }

    @JsonProperty("")
    public MolecularDataRowDTO setAnnotation(String annotation) {
        this.annotation = annotation;
        return this;
    }

    @JsonProperty("Result")
    public String getCytogeneticsResult() {
        return cytogeneticsResult;
    }

    @JsonProperty("Result")
    public MolecularDataRowDTO setCytogeneticsResult(String cytogeneticsResult) {
        this.cytogeneticsResult = cytogeneticsResult;
        return this;
    }

    @JsonProperty("")
    public String getMicrosateliteResult() {
        return microsateliteResult;
    }

    @JsonProperty("")
    public MolecularDataRowDTO setMicrosateliteResult(String microsateliteResult) {
        this.microsateliteResult = microsateliteResult;
        return this;
    }

    @JsonProperty("Probe Id Affymetrix")
    public String getProbeIdAffymetrix() {
        return probeIdAffymetrix;
    }

    @JsonProperty("Probe Id Affymetrix")
    public MolecularDataRowDTO setProbeIdAffymetrix(String probeIdAffymetrix) {
        this.probeIdAffymetrix = probeIdAffymetrix;
        return this;
    }

    @JsonProperty("Log2 Rcna")
    public String getCnaLog2rCna() {
        return cnaLog2rCna;
    }

    @JsonProperty("Log2 Rcna")
    public MolecularDataRowDTO setCnaLog2rCna(String cnaLog2rCna) {
        this.cnaLog2rCna = cnaLog2rCna;
        return this;
    }

    @JsonProperty("Log10 Rcna")
    public String getCnaLog10rCna() {
        return cnaLog10rCna;
    }

    @JsonProperty("Log10 Rcna")
    public MolecularDataRowDTO setCnaLog10rCna(String cnaLog10rCna) {
        this.cnaLog10rCna = cnaLog10rCna;
        return this;
    }

    @JsonProperty("Copy Number Status")
    public String getCnaCopyNumberStatus() {
        return cnaCopyNumberStatus;
    }

    @JsonProperty("Copy Number Status")
    public MolecularDataRowDTO setCnaCopyNumberStatus(String cnaCopyNumberStatus) {
        this.cnaCopyNumberStatus = cnaCopyNumberStatus;
        return this;
    }

    @JsonProperty("Gistic Value")
    public String getCnaGisticValue() {
        return cnaGisticValue;
    }

    @JsonProperty("Gistic Value")
    public MolecularDataRowDTO setCnaGisticValue(String cnaGisticValue) {
        this.cnaGisticValue = cnaGisticValue;
        return this;
    }

    @JsonProperty("")
    public String getCnaPicnicValue() {
        return cnaPicnicValue;
    }

    @JsonProperty("")
    public MolecularDataRowDTO setCnaPicnicValue(String cnaPicnicValue) {
        this.cnaPicnicValue = cnaPicnicValue;
        return this;
    }

    @JsonProperty("Illumina HGEA Exp")
    public String getIlluminaHGEAExp(){
        return illuminaHGEAExp;
    }

    @JsonProperty("Illumina HGEA Exp")
    public MolecularDataRowDTO setIlluminaHGEAExp(String value) {
        this.illuminaHGEAExp = value;
        return this;
    }

    public MolecularDataRowDTO build() {
        return this;
    }

}

