package org.pdxfinder.services.search;

import java.util.List;
import java.util.Map;

/*
 * Created by csaba on 19/11/2018.
 */
public class TwoParamUnlinkedFilter extends GeneralFilter{

    private String param1Name;
    private String param2Name;

    private List<String> options1;
    private List<String> options2;

    //"option1"=>"option2"
    private Map<String, List<String>> selected;


    public TwoParamUnlinkedFilter(String name, String urlParam, Boolean isActive, String type, String param1Name, String param2Name, List<String> options1, List<String> options2, Map<String, List<String>> selected) {
        super(name, urlParam, isActive, type);
        this.param1Name = param1Name;
        this.param2Name = param2Name;
        this.options1 = options1;
        this.options2 = options2;
        this.selected = selected;
    }

    public List<String> getOptions1() {
        return options1;
    }

    public void setOptions1(List<String> options1) {
        this.options1 = options1;
    }

    public List<String> getOptions2() {
        return options2;
    }

    public void setOptions2(List<String> options2) {
        this.options2 = options2;
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
