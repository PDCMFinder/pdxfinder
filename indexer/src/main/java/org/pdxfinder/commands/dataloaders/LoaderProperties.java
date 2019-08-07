package org.pdxfinder.commands.dataloaders;

import java.util.Map;

/*
 * Created by abayomi on 06/03/2019.
 */
public class LoaderProperties {

    //  DATA SOURCE PROPERTIES
    protected String jsonKey;
    protected String dataSourceAbbreviation;
    protected String dataSourceName;
    protected String dataSourceDescription;
    protected String dataSourceContact;
    protected String dataSourceURL;
    protected String providerType;
    protected String accessibility;
    protected String projectGroup;

    protected String nsgBsName;
    protected String nsgBsSymbol;
    protected String nsgbsURL;
    protected String nsBsName;
    protected String nsBsSymbol;
    protected String nsBsURL;

    protected String dosingStudyURL;
    protected String sourceURL;
    protected String normalTissueFalse;
    protected String tech;
    protected String fingerPrintDescription;
    protected String histologyNote;
    protected Map<String, String> platformURL;
    protected String finderRootDirectory;


    // OMIC DATA PROPERTIES
    protected String omicDataSource;
    protected String omicModelID;
    protected String omicSampleID;
    protected String omicSampleOrigin;
    protected String omicPassage;
    protected String omicHostStrainName;
    protected String omicHgncSymbol;
    protected String omicAminoAcidChange;
    protected String omicNucleotideChange;
    protected String omicConsequence;
    protected String omicReadDepth;
    protected String omicAlleleFrequency;
    protected String omicChromosome;
    protected String omicSeqStartPosition;
    protected String omicRefAllele;
    protected String omicAltAllele;
    protected String omicUcscGeneId;
    protected String omicNcbiGeneId;
    protected String omicEnsemblGeneId;
    protected String omicEnsemblTranscriptId;
    protected String omicRsIdVariants;
    protected String omicGenomeAssembly;
    protected String omicPlatform;

    protected String omicSeqEndPosition;
    protected String omicCnaLog10RCNA;
    protected String omicCnaLog2RCNA;
    protected String omicCnaCopyNumberStatus;
    protected String omicCnaGisticvalue;
    protected String omicCnaPicnicValue;

    protected String omicDataFilesType;
    protected String omicFileExtension;

    protected String rnaSeqCoverage;
    protected String rnaSeqFPKM;
    protected String rnaSeqTPM;
    protected String rnaSeqCount;
    protected String affyHGEAProbeId;
    protected String affyHGEAExpressionValue;
    protected String illuminaHGEAProbeId;
    protected String illuminaHGEAExpressionValue;



    public String getJsonKey() {
        return jsonKey;
    }

    public void setJsonKey(String jsonKey) {
        this.jsonKey = jsonKey.trim();
    }

    public String getDataSourceAbbreviation() {
        return dataSourceAbbreviation;
    }

    public void setDataSourceAbbreviation(String dataSourceAbbreviation) {
        this.dataSourceAbbreviation = dataSourceAbbreviation.trim();
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName.trim();
    }

    public String getDataSourceDescription() {
        return dataSourceDescription;
    }

    public void setDataSourceDescription(String dataSourceDescription) {
        this.dataSourceDescription = dataSourceDescription.trim();
    }

    public String getDataSourceContact() {
        return dataSourceContact;
    }

    public void setDataSourceContact(String dataSourceContact) {
        this.dataSourceContact = dataSourceContact.trim();
    }

    public String getDataSourceURL() {
        return dataSourceURL;
    }

    public void setDataSourceURL(String dataSourceURL) {
        this.dataSourceURL = dataSourceURL.trim();
    }

    public String getProviderType() {
        return providerType;
    }

    public void setProviderType(String providerType) {
        this.providerType = providerType.trim();
    }

    public String getAccessibility() {
        return accessibility;
    }

    public void setAccessibility(String accessibility) {
        this.accessibility = accessibility.trim();
    }

    public String getProjectGroup() {
        return projectGroup;
    }

    public void setProjectGroup(String projectGroup) {
        this.projectGroup = projectGroup.trim().trim();
    }

    public String getNsgBsName() {
        return nsgBsName;
    }

    public void setNsgBsName(String nsgBsName) {
        this.nsgBsName = nsgBsName.trim();
    }

    public String getNsgBsSymbol() {
        return nsgBsSymbol;
    }

    public void setNsgBsSymbol(String nsgBsSymbol) {
        this.nsgBsSymbol = nsgBsSymbol.trim();
    }

    public String getNsgbsURL() {
        return nsgbsURL;
    }

    public void setNsgbsURL(String nsgbsURL) {
        this.nsgbsURL = nsgbsURL.trim();
    }

    public String getNsBsName() {
        return nsBsName;
    }

    public void setNsBsName(String nsBsName) {
        this.nsBsName = nsBsName.trim();
    }

    public String getNsBsSymbol() {
        return nsBsSymbol;
    }

    public void setNsBsSymbol(String nsBsSymbol) {
        this.nsBsSymbol = nsBsSymbol.trim();
    }

    public String getNsBsURL() {
        return nsBsURL;
    }

    public void setNsBsURL(String nsBsURL) {
        this.nsBsURL = nsBsURL.trim();
    }

    public String getDosingStudyURL() {
        return dosingStudyURL;
    }

    public void setDosingStudyURL(String dosingStudyURL) {
        this.dosingStudyURL = dosingStudyURL.trim();
    }

    public String getSourceURL() {
        return sourceURL;
    }

    public void setSourceURL(String sourceURL) {
        this.sourceURL = sourceURL.trim();
    }

    public String getNormalTissueFalse() {
        return normalTissueFalse;
    }

    public void setNormalTissueFalse(String normalTissueFalse) {
        this.normalTissueFalse = normalTissueFalse.trim();
    }

    public String getTech() {
        return tech;
    }

    public void setTech(String tech) {
        this.tech = tech.trim();
    }

    public String getFingerPrintDescription() {
        return fingerPrintDescription;
    }

    public void setFingerPrintDescription(String fingerPrintDescription) {
        this.fingerPrintDescription = fingerPrintDescription.trim();
    }

    public String getHistologyNote() {
        return histologyNote;
    }

    public void setHistologyNote(String histologyNote) {
        this.histologyNote = histologyNote.trim();
    }

    public Map<String, String> getPlatformURL() {
        return platformURL;
    }

    public void setPlatformURL(Map<String, String> platformURL) {
        this.platformURL = platformURL;
    }

    public String getFinderRootDirectory() {
        return finderRootDirectory;
    }

    public void setFinderRootDirectory(String dataRootDirectory) {
        this.finderRootDirectory = dataRootDirectory.trim();
    }

    public String getOmicDataSource() {
        return omicDataSource;
    }

    public void setOmicDataSource(String omicDataSource) {
        this.omicDataSource = omicDataSource.trim();
    }

    public String getOmicModelID() {
        return omicModelID;
    }

    public void setOmicModelID(String omicModelID) {
        this.omicModelID = omicModelID.trim();
    }

    public String getOmicSampleID() {
        return omicSampleID;
    }

    public void setOmicSampleID(String omicSampleID) {
        this.omicSampleID = omicSampleID.trim();
    }

    public String getOmicSampleOrigin() {
        return omicSampleOrigin;
    }

    public void setOmicSampleOrigin(String omicSampleOrigin) {
        this.omicSampleOrigin = omicSampleOrigin.trim();
    }

    public String getOmicPassage() {
        return omicPassage;
    }

    public void setOmicPassage(String omicPassage) {
        this.omicPassage = omicPassage.trim();
    }

    public String getOmicHostStrainName() {
        return omicHostStrainName;
    }

    public void setOmicHostStrainName(String omicHostStrainName) {
        this.omicHostStrainName = omicHostStrainName.trim();
    }

    public String getOmicHgncSymbol() {
        return omicHgncSymbol;
    }

    public void setOmicHgncSymbol(String omicHgncSymbol) {
        this.omicHgncSymbol = omicHgncSymbol.trim();
    }

    public String getOmicAminoAcidChange() {
        return omicAminoAcidChange;
    }

    public void setOmicAminoAcidChange(String omicAminoAcidChange) {
        this.omicAminoAcidChange = omicAminoAcidChange.trim();
    }

    public String getOmicNucleotideChange() {
        return omicNucleotideChange;
    }

    public void setOmicNucleotideChange(String omicNucleotideChange) {
        this.omicNucleotideChange = omicNucleotideChange.trim();
    }

    public String getOmicConsequence() {
        return omicConsequence;
    }

    public void setOmicConsequence(String omicConsequence) {
        this.omicConsequence = omicConsequence.trim();
    }

    public String getOmicReadDepth() {
        return omicReadDepth;
    }

    public void setOmicReadDepth(String omicReadDepth) {
        this.omicReadDepth = omicReadDepth.trim();
    }

    public String getOmicAlleleFrequency() {
        return omicAlleleFrequency;
    }

    public void setOmicAlleleFrequency(String omicAlleleFrequency) {
        this.omicAlleleFrequency = omicAlleleFrequency.trim();
    }

    public String getOmicChromosome() {
        return omicChromosome;
    }

    public void setOmicChromosome(String omicChromosome) {
        this.omicChromosome = omicChromosome.trim();
    }

    public String getOmicSeqStartPosition() {
        return omicSeqStartPosition;
    }

    public void setOmicSeqStartPosition(String omicSeqStartPosition) {
        this.omicSeqStartPosition = omicSeqStartPosition.trim();
    }

    public String getOmicRefAllele() {
        return omicRefAllele;
    }

    public void setOmicRefAllele(String omicRefAllele) {
        this.omicRefAllele = omicRefAllele.trim();
    }

    public String getOmicAltAllele() {
        return omicAltAllele;
    }

    public void setOmicAltAllele(String omicAltAllele) {
        this.omicAltAllele = omicAltAllele.trim();
    }

    public String getOmicUcscGeneId() {
        return omicUcscGeneId;
    }

    public void setOmicUcscGeneId(String omicUcscGeneId) {
        this.omicUcscGeneId = omicUcscGeneId.trim();
    }

    public String getOmicNcbiGeneId() {
        return omicNcbiGeneId;
    }

    public void setOmicNcbiGeneId(String omicNcbiGeneId) {
        this.omicNcbiGeneId = omicNcbiGeneId.trim();
    }

    public String getOmicEnsemblGeneId() {
        return omicEnsemblGeneId;
    }

    public void setOmicEnsemblGeneId(String omicEnsemblGeneId) {
        this.omicEnsemblGeneId = omicEnsemblGeneId.trim();
    }

    public String getOmicEnsemblTranscriptId() {
        return omicEnsemblTranscriptId;
    }

    public void setOmicEnsemblTranscriptId(String omicEnsemblTranscriptId) {
        this.omicEnsemblTranscriptId = omicEnsemblTranscriptId.trim();
    }

    public String getOmicRsIdVariants() {
        return omicRsIdVariants;
    }

    public void setOmicRsIdVariants(String omicRsIdVariants) {
        this.omicRsIdVariants = omicRsIdVariants.trim();
    }

    public String getOmicGenomeAssembly() {
        return omicGenomeAssembly;
    }

    public void setOmicGenomeAssembly(String omicGenomeAssembly) {
        this.omicGenomeAssembly = omicGenomeAssembly.trim();
    }

    public String getOmicPlatform() {
        return omicPlatform;
    }

    public void setOmicPlatform(String omicPlatform) {
        this.omicPlatform = omicPlatform.trim();
    }


    public String getOmicSeqEndPosition() {
        return omicSeqEndPosition;
    }

    public void setOmicSeqEndPosition(String omicSeqEndPosition) {
        this.omicSeqEndPosition = omicSeqEndPosition.trim();
    }

    public String getOmicCnaLog10RCNA() {
        return omicCnaLog10RCNA;
    }

    public void setOmicCnaLog10RCNA(String omicCnaLog10RCNA) {
        this.omicCnaLog10RCNA = omicCnaLog10RCNA.trim();
    }

    public String getOmicCnaLog2RCNA() {
        return omicCnaLog2RCNA;
    }

    public void setOmicCnaLog2RCNA(String omicCnaLog2RCNA) {
        this.omicCnaLog2RCNA = omicCnaLog2RCNA.trim();
    }

    public String getOmicCnaCopyNumberStatus() {
        return omicCnaCopyNumberStatus;
    }

    public void setOmicCnaCopyNumberStatus(String omicCnaCopyNumberStatus) {
        this.omicCnaCopyNumberStatus = omicCnaCopyNumberStatus.trim();
    }

    public String getOmicCnaGisticvalue() {
        return omicCnaGisticvalue;
    }

    public void setOmicCnaGisticvalue(String omicCnaGisticvalue) {
        this.omicCnaGisticvalue = omicCnaGisticvalue.trim();
    }

    public String getOmicCnaPicnicValue() {
        return omicCnaPicnicValue;
    }

    public void setOmicCnaPicnicValue(String omicCnaPicnicValue) {
        this.omicCnaPicnicValue = omicCnaPicnicValue.trim();
    }

    public String getOmicDataFilesType() {
        return omicDataFilesType;
    }

    public void setOmicDataFilesType(String omicDataFilesType) {
        this.omicDataFilesType = omicDataFilesType.trim();
    }

    public String getOmicFileExtension() {
        return omicFileExtension;
    }

    public void setOmicFileExtension(String omicFileExtension) {
        this.omicFileExtension = omicFileExtension.trim();
    }

    public String getRnaSeqCoverage() {
        return rnaSeqCoverage;
    }

    public void setRnaSeqCoverage(String rnaSeqCoverage) {
        this.rnaSeqCoverage = rnaSeqCoverage;
    }

    public String getRnaSeqFPKM() {
        return rnaSeqFPKM;
    }

    public void setRnaSeqFPKM(String rnaSeqFPKM) {
        this.rnaSeqFPKM = rnaSeqFPKM;
    }

    public String getRnaSeqTPM() {
        return rnaSeqTPM;
    }

    public void setRnaSeqTPM(String rnaSeqTPM) {
        this.rnaSeqTPM = rnaSeqTPM;
    }

    public String getRnaSeqCount() {
        return rnaSeqCount;
    }

    public void setRnaSeqCount(String rnaSeqCount) {
        this.rnaSeqCount = rnaSeqCount;
    }

    public String getAffyHGEAProbeId() {
        return affyHGEAProbeId;
    }

    public void setAffyHGEAProbeId(String affyHGEAProbeId) {
        this.affyHGEAProbeId = affyHGEAProbeId;
    }

    public String getAffyHGEAExpressionValue() {
        return affyHGEAExpressionValue;
    }

    public void setAffyHGEAExpressionValue(String affyHGEAExpressionValue) {
        this.affyHGEAExpressionValue = affyHGEAExpressionValue;
    }

    public String getIlluminaHGEAProbeId() {
        return illuminaHGEAProbeId;
    }

    public void setIlluminaHGEAProbeId(String illuminaHGEAProbeId) {
        this.illuminaHGEAProbeId = illuminaHGEAProbeId;
    }

    public String getIlluminaHGEAExpressionValue() {
        return illuminaHGEAExpressionValue;
    }

    public void setIlluminaHGEAExpressionValue(String illuminaHGEAExpressionValue) {
        this.illuminaHGEAExpressionValue = illuminaHGEAExpressionValue;
    }
}
