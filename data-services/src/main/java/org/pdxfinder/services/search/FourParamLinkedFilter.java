package org.pdxfinder.services.search;

import java.util.List;
import java.util.Map;

/*
 * Created by csaba on 28/11/2018.
 */
public class FourParamLinkedFilter extends GeneralFilter{

    private Map<String, Map<String, Map<String, List<String>>>> options;

    private Map<Map,Map<String, Map<String, List<String>>>> selected;


    public FourParamLinkedFilter(String name, String urlParam, Boolean isActive, FilterType type, Map<String, Map<String, Map<String, List<String>>>> options, Map<Map, Map<String, Map<String, List<String>>>> selected) {
        super(name, urlParam, isActive, type);
        this.options = options;
        this.selected = selected;
    }

    public Map<String, Map<String, Map<String, List<String>>>> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Map<String, Map<String, List<String>>>> options) {
        this.options = options;
    }

    public Map<Map, Map<String, Map<String, List<String>>>> getSelected() {
        return selected;
    }

    public void setSelected(Map<Map, Map<String, Map<String, List<String>>>> selected) {
        this.selected = selected;
    }
}
