package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * TreatmentSummary represents a summary of the application of a treatment protocol to a sample (either Human or Xenograft)
 */

@NodeEntity
public class TreatmentSummary {

    @GraphId
    Long id;

    String drug;
    String drugManufacturer;
    String dose;
    String duration;
    String frequency;
    String armSize;
    String response;
    String responseCalculationMethod;
    String passages;

    @Relationship(type = "SUMMARY_OF_TREATMENT")
    ModelCreation modelCreation;

    public TreatmentSummary() {
    }

    /**
     * TreatmentSummary represents a summary of the application of a treatment protocol to a sample (either Human or Xenograft)
     *
     * @param drug                      Which drug(s) were used in the treatment
     * @param drugManufacturer          What company manufactured the drug
     * @param dose                      What was the concentration of the drug used
     * @param duration                  For how long was the treatment administered
     * @param frequency                 How often the treatment was administered
     * @param armSize                   The number of animals used in the study arm
     * @param response                  A recist classification of the response to the treatment
     * @param responseCalculationMethod The method used to determine the response classification
     * @param passages                  The list of passages at which this treatment was applied
     */
    public TreatmentSummary(String drug, String drugManufacturer, String dose, String duration, String frequency, String armSize, String response, String responseCalculationMethod, String passages) {
        this.drug = drug;
        this.drugManufacturer = drugManufacturer;
        this.dose = dose;
        this.duration = duration;
        this.frequency = frequency;
        this.armSize = armSize;
        this.response = response;
        this.responseCalculationMethod = responseCalculationMethod;
        this.passages = passages;
    }

    public String getDrug() {
        return drug;
    }

    public void setDrug(String drug) {
        this.drug = drug;
    }

    public String getDrugManufacturer() {
        return drugManufacturer;
    }

    public void setDrugManufacturer(String drugManufacturer) {
        this.drugManufacturer = drugManufacturer;
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

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getArmSize() {
        return armSize;
    }

    public void setArmSize(String armSize) {
        this.armSize = armSize;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getResponseCalculationMethod() {
        return responseCalculationMethod;
    }

    public void setResponseCalculationMethod(String responseCalculationMethod) {
        this.responseCalculationMethod = responseCalculationMethod;
    }

    public String getPassages() {
        return passages;
    }

    public void setPassages(String passages) {
        this.passages = passages;
    }
}
