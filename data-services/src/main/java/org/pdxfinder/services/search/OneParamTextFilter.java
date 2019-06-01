package org.pdxfinder.services.search;

import java.util.List;

/*
 * Created by csaba on 04/12/2018.
 */
public class OneParamTextFilter extends GeneralFilter{

    private String param1Name;

    private List<String> options1;

    List<String> selected;


    public OneParamTextFilter(String name, String urlParam, Boolean isActive, String type, String param1Name, List<String> options1, List<String> selected) {
        super(name, urlParam, isActive, type);
        this.param1Name = param1Name;
        this.options1 = options1;
        this.selected = selected;
    }

    public String getParam1Name() {
        return param1Name;
    }

    public void setParam1Name(String param1Name) {
        this.param1Name = param1Name;
    }

    public List<String> getOptions1() {
        return options1;
    }

    public void setOptions1(List<String> options1) {
        this.options1 = options1;
    }

    public List<String> getSelected() {
        return selected;
    }

    public void setSelected(List<String> selected) {
        this.selected = selected;
    }
}
