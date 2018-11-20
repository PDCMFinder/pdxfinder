package org.pdxfinder.services.search;

import java.util.List;

/*
 * Created by csaba on 19/11/2018.
 */
public class OneParamFilter extends GeneralFilter{


    private List<String> options;

    private List<String> selected;


    public OneParamFilter(String name, String urlParam, List<String> options, List<String> selected) {
        super(name, urlParam);
        this.options = options;
        this.selected = selected;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public List<String> getSelected() {
        return selected;
    }

    public void setSelected(List<String> selected) {
        this.selected = selected;
    }


}
