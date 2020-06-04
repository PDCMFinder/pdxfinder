package org.pdxfinder.services.dto.pdxgun;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class Reference {

    private String label;
    private Map referenceDbs;

    public Reference() {
        // Empty Constructor
    }

    public Reference(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public Reference setLabel(String label) {
        this.label = label;
        return this;
    }

    public Map getReferenceDbs() {
        return referenceDbs;
    }

    public Reference setReferenceDbs(Map referenceDbs) {
        this.referenceDbs = referenceDbs;
        return this;
    }

    public Reference build() {
        return this;
    }

}
