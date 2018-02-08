package org.pdxfinder.commands;

/*
 * Created by csaba on 19/01/2018.
 */

import org.pdxfinder.services.ds.SearchDS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

//This command is responsible for creating the Search DS
@Component
@Order(value = 0)
public class CreateSearchDS implements CommandLineRunner{

    private SearchDS searchDS;
    private final static Logger log = LoggerFactory.getLogger(CreateSearchDS.class);




    @Override
    public void run(String... strings) throws Exception {

    }


    //loads the models and populates fields in the searchDS
    private void loadModels(){

    }

    //saves the SearchDS object to disk
    private void serialize(){


    }

}
