package org.pdxfinder.services.search;

import java.util.List;
import java.util.Map;

/*
 * Created by csaba on 19/11/2018.
 */
public class ThreeParamLinkedFilter extends GeneralFilter{


    private String param1Name;
    private String param2Name;
    private String param3Name;

    private Map<String, Map<String, List<String>>> options;

    private Map<String, Map<String, List<String>>> selected;

    public ThreeParamLinkedFilter(String name, String urlParam, Boolean isActive, FilterType type, String param1Name, String param2Name, String param3Name, Map<String, Map<String, List<String>>> options, Map<String, Map<String, List<String>>> selected) {
        super(name, urlParam, isActive, type);
        this.param1Name = param1Name;
        this.param2Name = param2Name;
        this.param3Name = param3Name;
        this.options = options;
        this.selected = selected;
    }

    public String getParam1Name() {
        return param1Name;
    }

    public void setParam1Name(String param1Name) {
        this.param1Name = param1Name;
    }

    public String getParam2Name() {
        return param2Name;
    }

    public void setParam2Name(String param2Name) {
        this.param2Name = param2Name;
    }

    public String getParam3Name() {
        return param3Name;
    }

    public void setParam3Name(String param3Name) {
        this.param3Name = param3Name;
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
