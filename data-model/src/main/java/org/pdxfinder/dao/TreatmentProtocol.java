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

    private String armSize;
    private String responseCalculationMethod;
    private String passages;

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

    public String getDrugString(){

        String ret = "";

        for(TreatmentComponent comp:components){

            String drugName = comp.getDrug().getName();

            if(!ret.isEmpty()){
                ret+=" and ";
            }
            ret+=drugName;
        }

        return ret;
    }

    public void addTreatmentComponent(TreatmentComponent tc){

        if(components == null){
            components = new ArrayList<>();
        }

        components.add(tc);
    }

}
