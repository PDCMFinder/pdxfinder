package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by csaba on 23/10/2017.
 */


@NodeEntity
public class TreatmentProtocol {

    @GraphId
    private Long id;

    private String regime;

    private List<String> drug;
    private List<String> type;
    private List<String> target;



    private String drugManufacturer;
    private String dose;
    private String duration;
    private String frequency;
    private String armSize;
    private String responseCalculationMethod;
    private String passages;


    /**
     * @param drug                      Which drug(s) were used in the treatment
     * @param type                      Type of the treatment, ie drug/control (first type corresponds to the first drug, etc)
     * @param target                    Drug target, ie DNA, EGFR (first target corresponds to the first drug, etc)
     * @param drugManufacturer          What company manufactured the drug
     * @param dose                      What was the concentration of the drug used
     * @param duration                  For how long was the treatment administered
     * @param frequency                 How often the treatment was administered
     * @param armSize                   The number of animals used in the study arm
     * @param response                  A recist classification of the response to the treatment
     * @param responseCalculationMethod The method used to determine the response classification
     * @param passages                  The list of passages at which this treatment was applied
     */

    @Relationship(type = "RESPONSE")
    private Response response;

    public TreatmentProtocol() {

        this.drug = new ArrayList<>();
        this.type = new ArrayList<>();
        this.target = new ArrayList<>();
    }


    public String getRegime() {
        return regime;
    }

    public void setRegime(String regime) {
        this.regime = regime;
    }

    public List<String> getDrug() {
        return drug;
    }

    public void setDrug(List<String> drug) {
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

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }


    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type;
    }

    public List<String> getTarget() {
        return target;
    }

    public void setTarget(List<String> target) {
        this.target = target;
    }

    public String getDrugString(){

        String ret = "";
        for(String s:drug){

            if(!ret.equals("")){
                ret+=" and ";
            }
            ret+=s;
        }

        return ret;
    }


    public void addDrug(String drug){
        this.drug.add(drug);
    }

    public void addType(String type){
        this.type.add(type);
    }

    public void addTarget(String target){
        this.target.add(target);
    }

}
