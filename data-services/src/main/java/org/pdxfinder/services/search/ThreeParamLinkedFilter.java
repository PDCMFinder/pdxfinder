package org.pdxfinder.services.search;

import java.util.List;
import java.util.Map;

/*
 * Created by csaba on 19/11/2018.
 */
public class ThreeParamLinkedFilter extends GeneralFilter{

    private Map<String, Map<String, List<String>>> options;

    private Map<String, Map<String, List<String>>> selected;


    public ThreeParamLinkedFilter(String name, String urlParam, Map<String, Map<String, List<String>>> options, Map<String, Map<String, List<String>>> selected) {
        super(name, urlParam);
        this.options = options;
        this.selected = selected;
    }

    public Map<String, Map<String, List<String>>> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Map<String, List<String>>> options) {
        this.options = options;
    }

    public Map<String, Map<String, List<String>>> getSelected() {
        return selected;
    }

    public void setSelected(Map<String, Map<String, List<String>>> selected) {
        this.selected = selected;
    }
}
