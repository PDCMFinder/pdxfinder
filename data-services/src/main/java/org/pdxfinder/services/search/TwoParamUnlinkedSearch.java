package org.pdxfinder.services.search;

import org.pdxfinder.services.ds.ModelForQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

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

        for(String paramString : params){

            String[] paramArr = paramString.split("___");
            String key1, key2;

            if(paramArr.length == 1){
                key1 = "";
                key2 = paramArr[0];
            }
            else{
                key1 = paramArr[0];
                key2 = paramArr[1];
            }



            //cases:
            //1. key1 not empty, key2 ALL
            //2. key1 not empty + key2 not empty
            //3. key1 empty + key2 not empty
            //4. key1 empty + key2 ALL

            //1. key1 not empty, key2 ALL
            if(!key1.toLowerCase().isEmpty() && key2.toLowerCase().equals("all") ){

                if(data.containsKey(key1)){
                    for(Map.Entry<String, Set<Long>> arr: data.get(key1).entrySet()){

                        String datakey2 = arr.getKey();
                        Set<Long> foundModelIDs = arr.getValue();
                        modelsToKeep.addAll(foundModelIDs);
                        updateModelForQuery(foundModelIDs, models, key1+":"+datakey2, setter);
                    }
                }
            }
            //2. key1 not empty + key2 not empty and not ALL
            else if(!key1.isEmpty() && !key2.isEmpty() && !key2.toLowerCase().equals("all")){

                if(data.containsKey(key1)){
                    if(data.get(key1).containsKey(key2)){
                        Set<Long> foundModelIDs = data.get(key1).get(key2);

                        modelsToKeep.addAll(foundModelIDs);
                        updateModelForQuery(foundModelIDs, models, key1+":"+key2, setter);
                    }

                }

            }
            //3. key1 empty + key2 not empty
            //4. key1 empty + key2 ALL
            else if(key1.isEmpty() && !key2.isEmpty()){

                //is the second key ALL? if yes, loop through the whole structure and get every element regardless of values
                if(key2.toLowerCase().equals("all")){

                    for(Map.Entry<String, Map<String, Set<Long>>> arr1: data.entrySet()){

                        String datakey1 = arr1.getKey();

                        for(Map.Entry<String, Set<Long>> arr2 : arr1.getValue().entrySet()){

                            String datakey2 = arr2.getKey();
                            Set<Long> foundModelIDs = arr2.getValue();

                            modelsToKeep.addAll(foundModelIDs);
                            updateModelForQuery(foundModelIDs, models, datakey1+":"+datakey2, setter);
                        }

                    }

                }
                //the second key is a single value, loop through the whole structure and collect everything where key2 is the value
                else{

                    for(Map.Entry<String, Map<String, Set<Long>>> arr1: data.entrySet()){

                        String datakey1 = arr1.getKey();

                        if(arr1.getValue().containsKey(key2)){

                            Set<Long> foundModelIDs = arr1.getValue().get(key2);
                            modelsToKeep.addAll(foundModelIDs);
                            updateModelForQuery(foundModelIDs, models, datakey1+":"+key2, setter);
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
