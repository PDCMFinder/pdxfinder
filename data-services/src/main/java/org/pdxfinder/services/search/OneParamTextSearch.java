package org.pdxfinder.services.search;

import org.pdxfinder.services.ds.ModelForQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/*
 * Created by csaba on 04/12/2018.
 */
public class OneParamTextSearch extends GeneralSearch{

    Map<String, Set<Long>> data;


    public OneParamTextSearch(String name, String urlParam, Map<String, Set<Long>> data) {
        super(name, urlParam);
        this.data = data;
    }


    public Map<String, Set<Long>> getData() {
        return data;
    }

    public void setData(Map<String, Set<Long>> data) {
        this.data = data;
    }



    public Set<ModelForQuery> search(List<String> params, Set<ModelForQuery> models, BiConsumer<ModelForQuery, String> setter, ComparisonOperator op ){

        Set<Long> modelsToKeep = new HashSet<>();
        boolean firstTimeZero = true;

        for(String param: params){

            if(data.containsKey(param)){

                Set<Long> foundModelIDs = data.get(param);

                if(op.equals(ComparisonOperator.OR)){

                    modelsToKeep.addAll(foundModelIDs);
                    updateModelForQuery(foundModelIDs, models, param, setter);
                }

                else if(op.equals(ComparisonOperator.AND)){

                    if(firstTimeZero && modelsToKeep.size() == 0){
                        modelsToKeep.addAll(foundModelIDs);
                        firstTimeZero = false;
                    }
                    else{

                        //keep only those elements that are present in both sets
                        modelsToKeep.retainAll(foundModelIDs);

                    }

                    updateModelForQuery(foundModelIDs, models, param, setter);
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
