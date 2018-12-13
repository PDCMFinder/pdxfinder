package org.pdxfinder.services.search;

import org.pdxfinder.services.ds.FacetOption;
import org.pdxfinder.services.ds.ModelForQuery;

import java.util.*;
import java.util.function.BiConsumer;

/*
 * Created by csaba on 20/11/2018.
 */
public class TwoParamLinkedSearch extends GeneralSearch{


    Map<String, Map<String, Set<Long>>> data;

    public TwoParamLinkedSearch(String name, String urlParam, Map<String, Map<String, Set<Long>>> data) {
        super(name, urlParam);
        this.data = data;
    }

    public TwoParamLinkedSearch(String name, String urlParam) {
        super(name, urlParam);
    }

    public Map<String, Map<String, Set<Long>>> getData() {
        return data;
    }

    public void setData(Map<String, Map<String, Set<Long>>> data) {
        this.data = data;
    }


    /**
     *
     * @param params The search parameters = a list of key combinations for the data
     * @param models A list of models that the search is performed on
     * @param setter A setter method that is called when a match is found (this value is being displayed on the extended
     *               result list, ie: drug search=> drug name + response)
     * @return       A set of MFQ objects
     */
    public Set<ModelForQuery> search(List<String> params, Set<ModelForQuery> models, BiConsumer<ModelForQuery, String> setter ){


        //param: AAA__BBB

        //this set will hold the model ids that were a match and were updated
        Set<Long> modelsToKeep = new HashSet<>();


        for(String paramString : params) {

            String[] paramArr = paramString.split("___");

            String key1, key2;

            //if the first param is missing, make it ALL
            if(paramArr.length == 1){

                key1 = "ALL";
                key2 = paramArr[0];
            }
            else {
                key1 = paramArr[0];
                key2 = paramArr[1];
            }

            if(key1.toLowerCase().equals("all")){

                for(Map.Entry<String, Map<String, Set<Long>>> arr1: data.entrySet()){

                    if(arr1.getValue().containsKey(key2)){
                        Set<Long> foundModelIDs = arr1.getValue().get(key2);
                        modelsToKeep.addAll(foundModelIDs);

                        //TODO: only the second key value is passed back to the MFQ obj, this needs to be changed in the future
                        updateModelForQuery(foundModelIDs, models, key2, setter);
                    }
                }
            }
        }

        Set<ModelForQuery> results = new HashSet<>();

        for(ModelForQuery mfq:models){

            if(modelsToKeep.contains(mfq.getModelId())){

                results.add(mfq);
            }
        }


        return results;
    }



    /**
     *
     * @param modelIDs a set of model IDs to indicate which models need to be updated
     * @param models a set of models that we perform the updates on
     * @param value the value that is being passed to the setter
     * @param setter a setter reference that updates a certain field of the MFQ object
     */
    private void updateModelForQuery(Set<Long> modelIDs, Set<ModelForQuery> models, String value, BiConsumer<ModelForQuery, String> setter ){

        for(ModelForQuery mfq: models){

            if(modelIDs.contains(mfq.getModelId())){

                setter.accept(mfq, value);
            }

        }
    }

}
