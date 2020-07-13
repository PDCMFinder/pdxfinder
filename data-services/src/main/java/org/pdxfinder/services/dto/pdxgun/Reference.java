package org.pdxfinder.services.dto.pdxgun;

import java.util.Map;

public class Reference {

    private String label;
    private Map<String, String> referenceDbs;

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

    public Map<String, String> getReferenceDbs() {
        return referenceDbs;
    }

    public Reference setReferenceDbs(Map<String, String> referenceDbs) {
        this.referenceDbs = referenceDbs;
        return this;
    }

    public Reference build() {
        return this;
    }

}
