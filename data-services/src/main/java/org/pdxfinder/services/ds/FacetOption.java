package org.pdxfinder.services.ds;

public class FacetOption{

    //an option that is being displayed on the screen
    String label;
    //an url safe equivalent that can be used for html ids
    String labelId;

    public FacetOption(String label, String labelId) {
        this.label = label;
        this.labelId = labelId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabelId() {
        return labelId;
    }

    public void setLabelId(String labelId) {
        this.labelId = labelId;
    }
}
