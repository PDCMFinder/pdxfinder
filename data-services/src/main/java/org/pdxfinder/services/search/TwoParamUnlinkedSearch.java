package org.pdxfinder.services.search;

import org.pdxfinder.services.ds.ModelForQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Created by csaba on 20/11/2018.
 */
public class TwoParamUnlinkedSearch {

    //param1=>param2=>Set of model ids
    private Map<String, Map<String, Set<Long>>> data;

    public TwoParamUnlinkedSearch() {
    }

    public Map<String, Map<String, Set<Long>>> getData() {
        return data;
    }

    public void setData(Map<String, Map<String, Set<Long>>> data) {
        this.data = data;
    }

    public Set<ModelForQuery> search(Map<String, List<String>> params, Set<ModelForQuery> models){

        //TODO: Implement 2 param search
        return models;
    }
}
