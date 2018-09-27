package org.pdxfinder.services.dto;

/*
 * Created by abayomi on 21/09/2018.
 */
public class TreatmentSummaryDTO {

    private String treatmentDate;
    private String drugNames;
    private String dose;
    private String response;
    private String duration;
    private Boolean current = false;

    public TreatmentSummaryDTO() {
    }

    public TreatmentSummaryDTO(String treatmentDate, String drugNames, String dose, String response, String duration, Boolean current) {
        this.treatmentDate = treatmentDate;
        this.drugNames = drugNames;
        this.dose = dose;
        this.response = response;
        this.duration = duration;
        this.current = current;
    }


    public String getTreatmentDate() {
        return treatmentDate;
    }

    public void setTreatmentDate(String treatmentDate) {
        this.treatmentDate = treatmentDate;
    }

    public String getDrugNames() {
        return drugNames;
    }

    public void setDrugNames(String drugNames) {
        this.drugNames = drugNames;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getDose() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Boolean getCurrent() {
        return current;
    }

    public void setCurrent(Boolean current) {
        this.current = current;
    }
}


