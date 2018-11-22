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

    public OneParamSearch(String name, String urlParam) {
        super(name, urlParam);
    }


    public Set<ModelForQuery> search(List<String> searchParams, Set<ModelForQuery> mfqSet, Function<ModelForQuery, String> searchFunc){

        Set<ModelForQuery> results = new HashSet<>();

        for(ModelForQuery mfq: mfqSet){

            String mfqValue = searchFunc.apply(mfq);

            if(searchParams.contains(mfqValue)){
                results.add(mfq);
            }

        }

        return results;
    }


}
