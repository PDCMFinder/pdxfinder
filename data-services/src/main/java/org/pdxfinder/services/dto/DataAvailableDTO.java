package org.pdxfinder.services.dto;

/*
 * Created by csaba on 29/03/2018.
 */
public class DataAvailableDTO {


    String dataType;
    String platformName;
    String modelNumbers;
    String platformUrl;


    public DataAvailableDTO() {
    }

    public DataAvailableDTO(String dataType, String platformName, String modelNumbers) {
        this.dataType = dataType;
        this.platformName = platformName;
        this.modelNumbers = modelNumbers;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getModelNumbers() {
        return modelNumbers;
    }

    public void setModelNumbers(String modelNumbers) {
        this.modelNumbers = modelNumbers;
    }

    public String getPlatformUrl() {
        return platformUrl;
    }

    public void setPlatformUrl(String platformUrl) {
        this.platformUrl = platformUrl;
    }
}
