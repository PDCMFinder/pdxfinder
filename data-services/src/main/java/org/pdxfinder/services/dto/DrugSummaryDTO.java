package org.pdxfinder.services.dto;

/*
 * Created by csaba on 21/03/2018.
 */
public class DrugSummaryDTO {

    private String drugName;
    private String dose;
    private String response;
    private String duration;

    public DrugSummaryDTO() {
    }

    public DrugSummaryDTO(String drugName, String response) {
        this.drugName = drugName;
        this.response = response;
    }

    public DrugSummaryDTO(String drugName, String dose, String response, String duration) {
        this.drugName = drugName;
        this.dose = dose;
        this.response = response;
        this.duration = duration;
    }

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public String getDose() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {

        if(response.isEmpty() || response == null){
            this.response = "Not Specified";
        }
        else{
            this.response = response;
        }

    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}


