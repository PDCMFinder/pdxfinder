package org.pdxfinder.services.search;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * Created by csaba on 20/11/2018.
 */
public class TwoParamLinkedSearch extends GeneralSearch{


    Map<String, Map<String, Set<Long>>> data;

    public TwoParamLinkedSearch(String name, String urlParam, Map<String, Map<String, Set<Long>>> data) {
        super(name, urlParam);
        this.data = data;
    }


    public Map<String, Map<String, Set<Long>>> getData() {
        return data;
    }

    public void setData(Map<String, Map<String, Set<Long>>> data) {
        this.data = data;
    }



    Set<Long> search(String param1, String param2){

        //TODO: Implement search on data
        return new HashSet<>();
    }


}
