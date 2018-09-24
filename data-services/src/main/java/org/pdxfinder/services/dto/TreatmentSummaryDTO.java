package org.pdxfinder.services.dto;

import java.util.List;

/*
 * Created by abayomi on 21/09/2018.
 */
public class TreatmentSummaryDTO {

    private String treatmentDate;
    private List<DrugSummaryDTO> drugSummaries;
    private Boolean current = false;

    public TreatmentSummaryDTO() {
    }

    public TreatmentSummaryDTO(String treatmentDate, List<DrugSummaryDTO> drugSummaries, Boolean current) {
        this.treatmentDate = treatmentDate;
        this.drugSummaries = drugSummaries;
        this.current = current;
    }

    public String getTreatmentDate() {
        return treatmentDate;
    }

    public void setTreatmentDate(String treatmentDate) {
        this.treatmentDate = treatmentDate;
    }

    public List<DrugSummaryDTO> getDrugSummaries() {
        return drugSummaries;
    }

    public void setDrugSummaries(List<DrugSummaryDTO> drugSummaries) {
        this.drugSummaries = drugSummaries;
    }

    public Boolean getCurrent() {
        return current;
    }

    public void setCurrent(Boolean current) {
        this.current = current;
    }
}


