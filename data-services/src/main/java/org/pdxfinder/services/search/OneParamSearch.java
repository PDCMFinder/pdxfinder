package org.pdxfinder.services.search;

import org.pdxfinder.services.ds.FacetOption;
import org.pdxfinder.services.ds.ModelForQuery;

import java.util.*;
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
     * @param replacementStrings a list of FacetOptions to be able to decode the labelIds to labels
     * @param searchParams A list of values that we are matching in the given MFQ object field
     * @param mfqSet A set of MFQ objects that we perform the search on
     * @param searchFunc the method to be called on the MFQ objects (what are you filtering on)
     * @return a set of models after the search was performed
     */
    public Set<ModelForQuery> searchOnString(List<FacetOption> replacementStrings, List<String> searchParams, Set<ModelForQuery> mfqSet, Function<ModelForQuery, String> searchFunc){

        List<String> decodedSearchParams = new ArrayList<>();
        if(replacementStrings == null){
            decodedSearchParams = searchParams;
        }
        else{

            for(String param: searchParams){
                for(FacetOption fo :replacementStrings){
                    if(param.equals(fo.getLabelId())){
                        decodedSearchParams.add(fo.getLabel());
                    }
                }
            }
        }


        Set<ModelForQuery> results = new HashSet<>();

        for(ModelForQuery mfq: mfqSet){

            String mfqValue = searchFunc.apply(mfq);

            if(decodedSearchParams.contains(mfqValue)){
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
     * @return a set of models after the search was performed
     */
    public Set<ModelForQuery> searchOnCollection(List<FacetOption> replacementStrings, List<String> searchParams, Set<ModelForQuery> mfqSet, Function<ModelForQuery, Collection<String>> searchFunc){

        List<String> decodedSearchParams = new ArrayList<>();

        if(replacementStrings == null){
            decodedSearchParams = searchParams;
        }
        else{

            for(String param: searchParams){
                for(FacetOption fo :replacementStrings){
                    if(param.equals(fo.getLabelId())){
                        decodedSearchParams.add(fo.getLabel());
                    }
                }
            }
        }


        Set<ModelForQuery> results = new HashSet<>();

        for(ModelForQuery mfq: mfqSet){

            Collection<String> mfqValues = searchFunc.apply(mfq);

            if(mfqValues != null){

                for(String mfqValue : mfqValues){

                    if(decodedSearchParams.contains(mfqValue)){
                        results.add(mfq);
                    }
                }
            }

        }

        return results;
    }


}
