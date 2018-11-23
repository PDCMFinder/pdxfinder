package org.pdxfinder.services.search;

import org.pdxfinder.services.ds.ModelForQuery;

import java.util.Collection;
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


    /**
     * Performs a search on a ModelForQuery field that's type is a String
     * @param searchParams A list of values that we are matching in the given MFQ object field
     * @param mfqSet A set of MFQ objects that we perform the search on
     * @param searchFunc the method to be called on the MFQ objects (what are you filtering on)
     * @return
     */
    public Set<ModelForQuery> searchOnString(List<String> searchParams, Set<ModelForQuery> mfqSet, Function<ModelForQuery, String> searchFunc){

        Set<ModelForQuery> results = new HashSet<>();

        for(ModelForQuery mfq: mfqSet){

            String mfqValue = searchFunc.apply(mfq);

            if(searchParams.contains(mfqValue)){
                results.add(mfq);
            }

        }

        return results;
    }



    /**
     * Performs a search on a selected ModelForQuery field that's type is List of Strings
     * @param searchParams A list of values that we are matching in the given MFQ object field
     * @param mfqSet A set of MFQ objects that we perform the search on
     * @param searchFunc the method to be called on the MFQ objects (what are you filtering on)
     * @return
     */
    public Set<ModelForQuery> searchOnCollection(List<String> searchParams, Set<ModelForQuery> mfqSet, Function<ModelForQuery, Collection<String>> searchFunc){

        Set<ModelForQuery> results = new HashSet<>();

        for(ModelForQuery mfq: mfqSet){

            Collection<String> mfqValues = searchFunc.apply(mfq);

            for(String mfqValue : mfqValues){

                if(searchParams.contains(mfqValue)){
                    results.add(mfq);
                }
            }
        }

        return results;
    }


}
