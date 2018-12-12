package org.pdxfinder.services.search;

import org.pdxfinder.services.ds.FacetOption;

import java.util.List;

/*
 * Created by csaba on 19/11/2018.
 */
public class OneParamFilter extends GeneralFilter{


    private List<FacetOption> options;

    private List<String> selected;


    public OneParamFilter(String name, String urlParam, Boolean isActive, List<FacetOption> options, List<String> selected) {
        super(name, urlParam, isActive);
        this.options = options;
        this.selected = selected;
    }

    public List<FacetOption> getOptions() {
        return options;
    }

    public void setOptions(List<FacetOption> options) {
        this.options = options;
    }

    public List<String> getSelected() {
        return selected;
    }

    public void setSelected(List<String> selected) {
        this.selected = selected;
    }


}
