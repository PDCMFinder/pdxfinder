package org.pdxfinder.services.search;

import org.pdxfinder.services.ds.ModelForQuery;

import java.util.Map;
import java.util.Set;

/*
 * Created by csaba on 28/11/2018.
 */
public class FourParamLinkedSearch extends GeneralSearch{

    private Map<String, Map<String, Map<String, Map<String, Set<Long>>>>> data;


    public FourParamLinkedSearch(String name, String urlParam) {
        super(name, urlParam);
    }


    public FourParamLinkedSearch(String name, String urlParam, Map<String, Map<String, Map<String, Map<String, Set<Long>>>>> data) {
        super(name, urlParam);
        this.data = data;
    }

    public Map<String, Map<String, Map<String, Map<String, Set<Long>>>>> getData() {
        return data;
    }

    public void setData(Map<String, Map<String, Map<String, Map<String, Set<Long>>>>> data) {
        this.data = data;
    }

    public Set<ModelForQuery> search(Map<String, Map<String, Map<String, String>>> filters, Set<ModelForQuery> models){




        //TODO: Implement actual search on data
        return models;
    }

}
