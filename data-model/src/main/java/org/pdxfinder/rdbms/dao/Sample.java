package org.pdxfinder.rdbms.dao;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.persistence.*;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "Sample ID",
        "Tumor Type",
        "Passage",
        "WES-VCF-File",
        "WES-Fasta-File",
        "NCI-Gene-Panel",
        "RNASeq-Fasta-File",
        "RNASeq-RSEM-File"
})
public class Sample {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String sampleID;
    private String tumorType;
    private String passage;
    private String wESVCFFile;
    private String wESFastaFile;
    private String nCIGenePanel;
    private String rNASeqFastaFile;
    private String rNASeqRSEMFile;

    @ManyToOne
    @JoinColumn(name="pdxinfo_id")
    private PdmrPdxInfo pdmrPdxInfo;

    public Sample() {
    }

    public Sample(String sampleID, String tumorType, String passage, String wESVCFFile, String wESFastaFile,
                  String nCIGenePanel, String rNASeqFastaFile, String rNASeqRSEMFile) {
        this.sampleID = sampleID;
        this.tumorType = tumorType;
        this.passage = passage;
        this.wESVCFFile = wESVCFFile;
        this.wESFastaFile = wESFastaFile;
        this.nCIGenePanel = nCIGenePanel;
        this.rNASeqFastaFile = rNASeqFastaFile;
        this.rNASeqRSEMFile = rNASeqRSEMFile;
    }


    @JsonProperty("Sample ID")
    public String getSampleID() {
        return sampleID;
    }

    @JsonProperty("Sample ID")
    public void setSampleID(String sampleID) {
        this.sampleID = sampleID;
    }

    @JsonProperty("Tumor Type")
    public String getTumorType() {
        return tumorType;
    }

    @JsonProperty("Tumor Type")
    public void setTumorType(String tumorType) {
        this.tumorType = tumorType;
    }

    @JsonProperty("Passage")
    public String getPassage() {
        return passage;
    }

    @JsonProperty("Passage")
    public void setPassage(String passage) {
        this.passage = passage;
    }

    @JsonProperty("WES-VCF-File")
    public String getWESVCFFile() {
        return wESVCFFile;
    }

    @JsonProperty("WES-VCF-File")
    public void setWESVCFFile(String wESVCFFile) {
        this.wESVCFFile = wESVCFFile;
    }

    @JsonProperty("WES-Fastq-File")
    public String getWESFastaFile() {
        return wESFastaFile;
    }

    @JsonProperty("WES-Fastq-File")
    public void setWESFastaFile(String wESFastaFile) {
        this.wESFastaFile = wESFastaFile;
    }

    @JsonProperty("NCI-Gene-Panel")
    public String getNCIGenePanel() {
        return nCIGenePanel;
    }

    @JsonProperty("NCI-Gene-Panel")
    public void setNCIGenePanel(String nCIGenePanel) {
        this.nCIGenePanel = nCIGenePanel;
    }

    @JsonProperty("RNASeq-Fastq-File")
    public String getRNASeqFastaFile() {
        return rNASeqFastaFile;
    }

    @JsonProperty("RNASeq-Fastq-File")
    public void setRNASeqFastaFile(String rNASeqFastaFile) {
        this.rNASeqFastaFile = rNASeqFastaFile;
    }

    @JsonProperty("RNASeq-RSEM-File")
    public String getRNASeqRSEMFile() {
        return rNASeqRSEMFile;
    }

    @JsonProperty("RNASeq-RSEM-File")
    public void setRNASeqRSEMFile(String rNASeqRSEMFile) {
        this.rNASeqRSEMFile = rNASeqRSEMFile;
    }

    public void setPdmrPdxInfo(PdmrPdxInfo pdmrPdxInfo) {
        this.pdmrPdxInfo = pdmrPdxInfo;
    }
}
