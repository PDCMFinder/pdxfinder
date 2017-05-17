package org.pdxfinder.irccdatamodel;

/**
 * Created by csaba on 21/04/2017.
 */
public class IRCCSample {

    String sampleId;
    String collectionDate;
    String ageAtCollection;
    String diagnosis;
    String tumorType;
    String sampleSite;
    //Markers
    String msiStatus;
    String krasStatus;
    String brafStatus;
    String nrasStatus;
    String pik3caStatus;

    String modelId;
    String implantSite;
    String strain;
    String drugData;
    String implantType;


    public IRCCSample(String sampleId, String collectionDate, String ageAtCollection, String diagnosis,
                      String tumorType, String sampleSite, String msiStatus, String krasStatus, String brafStatus,
                      String nrasStatus, String pik3caStatus, String modelId, String implantSite, String strain,
                      String drugData, String implantType) {
        this.sampleId = sampleId;
        this.collectionDate = collectionDate;

        if(ageAtCollection == "" || ageAtCollection == null){
            this.ageAtCollection = "0";
        }
        else{
            this.ageAtCollection = ageAtCollection;
        }

        this.diagnosis = diagnosis;
        this.tumorType = tumorType;
        this.sampleSite = sampleSite;
        this.msiStatus = msiStatus;
        this.krasStatus = krasStatus;
        this.brafStatus = brafStatus;
        this.nrasStatus = nrasStatus;
        this.pik3caStatus = pik3caStatus;
        this.modelId = modelId;
        this.implantSite = implantSite;
        this.strain = strain;
        this.drugData = drugData;
        this.implantType = implantType;
    }

    public String getSampleId() {
        return sampleId;
    }

    public String getCollectionDate() {
        return collectionDate;
    }

    public String getAgeAtCollection() {
        return ageAtCollection;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public String getTumorType() {
        return tumorType;
    }

    public String getSampleSite() {
        return sampleSite;
    }

    public String getMsiStatus() {
        return msiStatus;
    }

    public String getKrasStatus() {
        return krasStatus;
    }

    public String getBrafStatus() {
        return brafStatus;
    }

    public String getNrasStatus() {
        return nrasStatus;
    }

    public String getPik3caStatus() {
        return pik3caStatus;
    }

    public String getModelId() {
        return modelId;
    }

    public String getImplantSite() {
        return implantSite;
    }

    public String getStrain() {
        return strain;
    }

    public String getDrugData() {
        return drugData;
    }

    public String getImplantType() {
        return implantType;
    }
}
