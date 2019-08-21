package org.pdxfinder.reportmanager;

/*
 * Created by csaba on 26/02/2019.
 */

import org.pdxfinder.services.reporting.LogEntity;
import org.pdxfinder.services.reporting.MarkerLogEntity;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("ReportManager")
@Order(value = -80)
public class ReportManager {

    private List<LogEntity> messages;

    public ReportManager() {
        this.messages = new ArrayList();
    }



    public void addMessage(LogEntity m){

        if(!messages.contains(m))
            messages.add(m);
    }

    public void printMessages(String level){

        if(messages.size() == 0){
            System.out.println("There are no message entries in the Report Manager.");
        }
        else{

            for(LogEntity le: messages){

                if(le.getType().equals(level) || level.equals("ALL")){
                    System.out.println(le);
                }


            }
        }
    }


    public List<List<String>> getMarkerHarmonizationMessagesInList(){

        List list = new ArrayList();

        for(LogEntity le: messages){
            List<String> row = new ArrayList();

            if(le instanceof MarkerLogEntity){

                MarkerLogEntity mle = (MarkerLogEntity) le;
                row.add(mle.getType());
                row.add(mle.getReporter());
                row.add(mle.getDataSource());
                row.add(mle.getModel());
                row.add(mle.getCharacterizationType());
                row.add(mle.getPlatform());
                row.add(mle.getUsedMarkerSymbol());
                row.add(mle.getSuggestedMarkerSymbol());
                row.add(mle.getReasonForChange());
                row.add(mle.getMessage());
                list.add(row);
            }
        }

        return list;
    }

}
