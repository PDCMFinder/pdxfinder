package org.pdxfinder.services.search;

import org.pdxfinder.services.ds.ModelForQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/*
 * Created by csaba on 20/11/2018.
 */
public class OneParamSearch extends GeneralSearch{

    //static Function a = ModelForQuery::getPatientAge;



    public OneParamSearch(String name, String urlParam) {
        super(name, urlParam);
    }



    public Set<ModelForQuery> search(List<String> param, List<ModelForQuery> mfq, Function searchFunc){

        //TODO: Implement search logic on single selected parameter
        return new HashSet<>();
    }


}
