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

    /**
     * @param components                The treatment components that were used in this protocol
     * @param armSize                   The number of animals used in the study arm
     * @param response                  A recist classification of the response to the treatment
     * @param responseCalculationMethod The method used to determine the response classification
     * @param passages                  The list of passages at which this treatment was applied
     */

    @GraphId
    private Long id;

    @Relationship(type = "TREATMENT_COMPONENT")
    private List<TreatmentComponent> components;

    @Relationship(type = "RESPONSE")
    private Response response;

    @Relationship(type = "CURRENT_TREATMENT")
    private CurrentTreatment currentTreatment;

    private String armSize;
    private String responseCalculationMethod;
    private String passages;

    private String treatmentDate;

    public TreatmentProtocol() {
        components = new ArrayList<>();
    }


    public List<TreatmentComponent> getComponents() {
        return components;
    }

    public void setComponents(List<TreatmentComponent> components) {
        this.components = components;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
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

    public CurrentTreatment getCurrentTreatment() {
        return currentTreatment;
    }

    public void setCurrentTreatment(CurrentTreatment currentTreatment) {
        this.currentTreatment = currentTreatment;
    }

    public String getTreatmentDate() {
        return treatmentDate;
    }

    public void setTreatmentDate(String treatmentDate) {
        this.treatmentDate = treatmentDate;
    }

    public String getDrugString(boolean includeControlDrugs){

        String ret = "";

        for(TreatmentComponent comp:components){

            String drugName = comp.getDrug().getName();

            if(includeControlDrugs){
                if(!ret.isEmpty()){
                    ret+=" and ";
                }
                ret+=drugName;
            }
            //include only Drugs but no Controls
            else{

                if(comp.getType().equals("Drug")){

                    if(!ret.isEmpty()){
                        ret+=" and ";
                    }
                    ret+=drugName;
                }

            }

        }

        return ret;
    }





    public String getDurationString(boolean includeControlDrugs){

        String durString = "";

        for(TreatmentComponent comp:components){

            String dur = comp.getDuration();

            if(includeControlDrugs){
                if(!durString.isEmpty()){
                    durString+=" / ";
                }
                durString+=dur;
            }
            //include only Drugs but no Controls
            else{

                if(comp.getType().equals("Drug")){

                    if(!durString.isEmpty()){
                        durString+=" / ";
                    }
                    durString+=dur;
                }

            }

        }

        return durString;
    }

    public String getDoseString(boolean includeControlDrugs){

        String doseString = "";

        for(TreatmentComponent comp:components){

            String dose = comp.getDose();

            if(includeControlDrugs){
                if(!doseString.isEmpty()){
                    doseString+=" / ";
                }
                doseString+=dose;
            }
            //include only Drugs but no Controls
            else{

                if(comp.getType().equals("Drug")){

                    if(!doseString.isEmpty()){
                        doseString+=" / ";
                    }
                    doseString+=dose;
                }

            }

        }

        return doseString;
    }


    public void addTreatmentComponent(TreatmentComponent tc){

        if(components == null){
            components = new ArrayList<>();
        }

        components.add(tc);
    }


    public void addDurationForAllComponents(String duration){

        if(components != null){

            for(TreatmentComponent tc : components){

                tc.setDuration(duration);
            }
        }
    }

}
