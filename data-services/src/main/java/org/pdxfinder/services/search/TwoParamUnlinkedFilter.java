package org.pdxfinder.services.search;

import java.util.List;
import java.util.Map;

/*
 * Created by csaba on 19/11/2018.
 */
public class TwoParamUnlinkedFilter extends GeneralFilter{

    private List<String> param1;
    private List<String> param2;

    private Map<String, List<String>> selected;


    public TwoParamUnlinkedFilter(String name, String urlParam, List<String> param1, List<String> param2, Map<String, List<String>> selected) {
        super(name, urlParam);
        this.param1 = param1;
        this.param2 = param2;
        this.selected = selected;
    }

    public List<String> getParam1() {
        return param1;
    }

    public void setParam1(List<String> param1) {
        this.param1 = param1;
    }

    public List<String> getParam2() {
        return param2;
    }

    public void setParam2(List<String> param2) {
        this.param2 = param2;
    }

    public Map<String, List<String>> getSelected() {
        return selected;
    }

    public void setSelected(Map<String, List<String>> selected) {
        this.selected = selected;
    }



}
