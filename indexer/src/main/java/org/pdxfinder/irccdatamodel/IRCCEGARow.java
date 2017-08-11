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
}
