package org.pdxfinder.services.search;

import java.util.List;
import java.util.Map;

/*
 * Created by csaba on 19/11/2018.
 */
public class TwoParamLinkedFilter extends GeneralFilter{

    private Map<String, List<String>> options;

    private Map<String, List<String>> selected;


    public TwoParamLinkedFilter(String name, String urlParam, Boolean isActive, Map<String, List<String>> options, Map<String, List<String>> selected) {
        super(name, urlParam, isActive);
        this.options = options;
        this.selected = selected;
    }

    public Map<String, List<String>> getOptions() {
        return options;
    }

    public void setOptions(Map<String, List<String>> options) {
        this.options = options;
    }

    public Map<String, List<String>> getSelected() {
        return selected;
    }

    public void setSelected(Map<String, List<String>> selected) {
        this.selected = selected;
    }
}
