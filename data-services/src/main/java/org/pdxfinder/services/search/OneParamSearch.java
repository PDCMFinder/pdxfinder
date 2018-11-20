package org.pdxfinder.services.search;

import org.pdxfinder.services.ds.ModelForQuery;

import java.util.HashSet;
import java.util.Set;

/*
 * Created by csaba on 20/11/2018.
 */
public class OneParamSearch extends GeneralSearch{


    Set<ModelForQuery> models;

    public OneParamSearch(String name, String urlParam, Set<ModelForQuery> models) {
        super(name, urlParam);
        this.models = models;
    }

    public Set<ModelForQuery> getModels() {
        return models;
    }

    public void setModels(Set<ModelForQuery> models) {
        this.models = models;
    }



    public Set<Long> search(String param1){

        //TODO: Implement search logic on single selected parameter
        return new HashSet<>();
    }


}
