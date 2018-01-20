package org.pdxfinder.services.ds;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


/*
 * Created by csaba on 19/01/2018.
 */


public class SearchDS implements Serializable{


    private HashSet<ModelForQuery> models;




    public SearchDS() {

        this.models = new HashSet<>();
    }


    public HashSet<ModelForQuery> getModels() {
        return models;
    }

    public void setModels(HashSet<ModelForQuery> models) {
        this.models = models;
    }


    public Set<ModelForQuery> search(String[] filters){

        Set<ModelForQuery> result = new HashSet<>();

        return result;
    }
}
