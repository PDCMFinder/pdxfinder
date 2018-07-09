package org.pdxfinder.transdatamodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.persistence.*;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "Drug",
        "Manufacturer",
        "Dose",
        "Duration",
        "Frequency",
        "Arm Size",
        "Response Class",
        "Passage Range"
})




public class Treatment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String drug;
    private String manufacturer;
    private String dose;
    private String duration;
    private String frequency;
    private String armSize;
    private String responseClass;
    private String passageRange;

    @ManyToOne
    @JoinColumn(name="pdxinfo_id")
    private PdmrPdxInfo pdmrPdxInfo;


    public Treatment(String drug, String manufacturer,
                     String dose, String duration,
                     String frequency, String armSize,
                     String responseClass, String passageRange) {
        this.drug = drug;
        this.manufacturer = manufacturer;
        this.dose = dose;
        this.duration = duration;
        this.frequency = frequency;
        this.armSize = armSize;
        this.responseClass = responseClass;
        this.passageRange = passageRange;
    }

    public Treatment() {
    }


    @JsonProperty("Drug")
    public String getDrug() {
        return drug;
    }

    @JsonProperty("Drug")
    public void setDrug(String drug) {
        this.drug = drug;
    }

    @JsonProperty("Manufacturer")
    public String getManufacturer() {
        return manufacturer;
    }

    @JsonProperty("Manufacturer")
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    @JsonProperty("Dose")
    public String getDose() {
        return dose;
    }

    @JsonProperty("Dose")
    public void setDose(String dose) {
        this.dose = dose;
    }

    @JsonProperty("Duration")
    public String getDuration() {
        return duration;
    }

    @JsonProperty("Duration")
    public void setDuration(String duration) {
        this.duration = duration;
    }

    @JsonProperty("Frequency")
    public String getFrequency() {
        return frequency;
    }

    @JsonProperty("Frequency")
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    @JsonProperty("Arm Size")
    public String getArmSize() {
        return armSize;
    }

    @JsonProperty("Arm Size")
    public void setArmSize(String armSize) {
        this.armSize = armSize;
    }

    @JsonProperty("Response Class")
    public String getResponseClass() {
        return responseClass;
    }

    @JsonProperty("Response Class")
    public void setResponseClass(String responseClass) {
        this.responseClass = responseClass;
    }

    @JsonProperty("Passage Range")
    public String getPassageRange() {
        return passageRange;
    }

    @JsonProperty("Passage Range")
    public void setPassageRange(String passageRange) {
        this.passageRange = passageRange;
    }

    public void setPdmrPdxInfo(PdmrPdxInfo pdmrPdxInfo) {
        this.pdmrPdxInfo = pdmrPdxInfo;
    }
}
