package org.pdxfinder.services.search;

import java.util.List;
import java.util.Map;

/*
 * Created by csaba on 19/11/2018.
 */
public class TwoParamLinkedFilter extends GeneralFilter{

    private String param1Name;
    private String param2Name;

    private Map<String, List<String>> options;

    private Map<String, List<String>> selected;

    public TwoParamLinkedFilter(String name, String urlParam, Boolean isActive, String type, String param1Name, String param2Name, Map<String, List<String>> options, Map<String, List<String>> selected) {
        super(name, urlParam, isActive, type);
        this.param1Name = param1Name;
        this.param2Name = param2Name;
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
}
