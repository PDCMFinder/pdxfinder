package org.pdxfinder.services.search;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Created by csaba on 20/11/2018.
 */
public class ThreeParamSearch extends GeneralSearch{

    private Map<String, Map<String, Map<String, Set<Long>>>> data;


    public ThreeParamSearch(String name, String urlParam, Map<String, Map<String, Map<String, Set<Long>>>> data) {
        super(name, urlParam);
        this.data = data;
    }

    public Map<String, Map<String, Map<String, Set<Long>>>> getData() {
        return data;
    }

    public void setData(Map<String, Map<String, Map<String, Set<Long>>>> data) {
        this.data = data;
    }


    public Set<Long> search(String param1, String param2, String param3){

        //TODO: Implement actual search on data
        return new HashSet<>();
    }


}
