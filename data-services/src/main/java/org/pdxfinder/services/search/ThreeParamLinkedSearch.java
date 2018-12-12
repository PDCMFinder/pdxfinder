package org.pdxfinder.services.search;

import org.pdxfinder.services.ds.ModelForQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/*
 * Created by csaba on 20/11/2018.
 */
public class ThreeParamLinkedSearch extends GeneralSearch{

    private Map<String, Map<String, Map<String, Set<Long>>>> data;

    public ThreeParamLinkedSearch(String name, String urlParam) {
        super(name, urlParam);
    }

    public ThreeParamLinkedSearch(String name, String urlParam, Map<String, Map<String, Map<String, Set<Long>>>> data) {
        super(name, urlParam);
        this.data = data;
    }

    public Map<String, Map<String, Map<String, Set<Long>>>> getData() {
        return data;
    }

    public void setData(Map<String, Map<String, Map<String, Set<Long>>>> data) {
        this.data = data;
    }


    public Set<ModelForQuery> search(List<String> params, Set<ModelForQuery> models, BiConsumer<ModelForQuery, String> setter){

        //this set will hold the model ids that were a match and were updated
        Set<Long> modelsToKeep = new HashSet<>();

        for(String paramString : params){

            String[] paramArr = paramString.split("___");

            String key1 = paramArr[0];
            String key2 = paramArr[1];
            String key3 = paramArr[2];

            if(key1.toLowerCase().equals("all")){

                for(Map.Entry<String, Map<String, Map<String, Set<Long>>>> arr: data.entrySet()){

                    String datakey1 = arr.getKey();

                    if(arr.getValue().containsKey(key2)){

                        if(key3.toLowerCase().equals("all")){

                            for(Map.Entry<String, Set<Long>> arr3 :arr.getValue().get(key2).entrySet()){

                                String datakey3 = arr3.getKey();

                                Set<Long> foundModelIDs = arr3.getValue();

                                updateModelForQuery(foundModelIDs, models, datakey1+":"+key2+" "+datakey3, setter);
                            }

                        }
                        else if(arr.getValue().get(key2).containsKey(key3)){

                            Set<Long> foundModelIDs = arr.getValue().get(key2).get(key3);
                            updateModelForQuery(foundModelIDs, models, datakey1+":"+key2+" "+key3, setter);
                        }


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
