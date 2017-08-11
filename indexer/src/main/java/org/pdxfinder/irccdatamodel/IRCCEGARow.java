package org.pdxfinder.irccdatamodel;

/**
 * Created by csaba on 08/08/2017.
 */
public class IRCCEGARow {

    String egaId;
    String sameId;
    String internalId;
    String torinoId;
    String bamFileName;
    String md5Sum;
    String encrMd5;

    String specimenId;
    String sampleId;
    String sampleType;
    String sampleOrigin;
    String sequencingPlatform;

    //data from Neo4j
    private String externalId;
    private String sex;
    private String race;
    private String ethnicity;
    private String dataSource;
    private String age;
    private String sourceSampleId;
    private String diagnosis;
    private String originTissue;
    private String sampleSite;
    private String classification;



    public IRCCEGARow(String egaId, String sameId, String internalId, String torinoId, String bamFileName, String md5Sum, String encrMd5) {
        this.egaId = egaId;
        this.sameId = sameId;
        this.internalId = internalId;
        this.torinoId = torinoId;
        this.bamFileName = bamFileName;
        this.md5Sum = md5Sum;
        this.encrMd5 = encrMd5;
    }


    public String getEgaId() {
        return egaId;
    }

    public void setEgaId(String egaId) {
        this.egaId = egaId;
    }

    public String getSameId() {
        return sameId;
    }

    public void setSameId(String sameId) {
        this.sameId = sameId;
    }

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public String getTorinoId() {
        return torinoId;
    }

    public void setTorinoId(String torinoId) {
        this.torinoId = torinoId;
    }

    public String getBamFileName() {
        return bamFileName;
    }

    public void setBamFileName(String bamFileName) {
        this.bamFileName = bamFileName;
    }

    public String getMd5Sum() {
        return md5Sum;
    }

    public void setMd5Sum(String md5Sum) {
        this.md5Sum = md5Sum;
    }

    public String getEncrMd5() {
        return encrMd5;
    }

    public void setEncrMd5(String encrMd5) {
        this.encrMd5 = encrMd5;
    }

    public String getSpecimenId() {
        return specimenId;
    }

    public void setSpecimenId(String specimenId) {
        this.specimenId = specimenId;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public String getSampleType() {
        return sampleType;
    }

    public void setSampleType(String sampleType) {
        this.sampleType = sampleType;
    }

    public String getSampleOrigin() {
        return sampleOrigin;
    }

    public void setSampleOrigin(String sampleOrigin) {
        this.sampleOrigin = sampleOrigin;
    }

    public String getSequencingPlatform() {
        return sequencingPlatform;
    }

    public void setSequencingPlatform(String sequencingPlatform) {
        this.sequencingPlatform = sequencingPlatform;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getSourceSampleId() {
        return sourceSampleId;
    }

    public void setSourceSampleId(String sourceSampleId) {
        this.sourceSampleId = sourceSampleId;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getOriginTissue() {
        return originTissue;
    }

    public void setOriginTissue(String originTissue) {
        this.originTissue = originTissue;
    }

    public String getSampleSite() {
        return sampleSite;
    }

    public void setSampleSite(String sampleSite) {
        this.sampleSite = sampleSite;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }
}
