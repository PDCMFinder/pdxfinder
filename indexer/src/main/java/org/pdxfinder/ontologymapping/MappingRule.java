package org.pdxfinder.ontologymapping;

/*
 * Created by csaba on 16/11/2017.
 */
public class MappingRule {

    private String ontologyTerm;
    private String mapType;
    private String justification;

    public MappingRule() {
    }


    public String getOntologyTerm() {
        return ontologyTerm;
    }

    public void setOntologyTerm(String ontologyTerm) {
        this.ontologyTerm = ontologyTerm;
    }

    public String getMapType() {
        return mapType;
    }

    public void setMapType(String mapType) {
        this.mapType = mapType;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }
}
