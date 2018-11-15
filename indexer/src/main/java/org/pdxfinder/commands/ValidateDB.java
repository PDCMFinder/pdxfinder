package org.pdxfinder.commands;

/*
 * Created by csaba on 15/11/2018.
 */

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.pdxfinder.dao.DataProjection;
import org.pdxfinder.services.DataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Order(value = 0)
public class ValidateDB implements CommandLineRunner{

    private final static Logger log = LoggerFactory.getLogger(ValidateDB.class);

    private DataImportService dataImportService;


    public ValidateDB(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("validateDB", "Validating DB");
        parser.accepts("loadALL", "Validating DB");
        OptionSet options = parser.parse(args);

        if (options.has("validateDB") || options.has("loadALL")) {


            log.info("******************************************************");
            log.info("* Starting validation checks                         *");
            log.info("******************************************************");

            runValidationChecks();
        }

    }


    private void runValidationChecks(){

        boolean isDBValid = true;
        boolean noWarnings = true;

        //check if there is any unlinked nodes that are not dataprojections
        log.info("Looking for sad nodes (nodes without a relationship)");
        Set<String> unlinkedNodeTypeSet = new HashSet<>();
        Set<Object> unlinkedNodes = dataImportService.findUnlinkedNodes();

        if(unlinkedNodes.size() == 0){
            log.error("DataProjection nodes not found - invalid database!");
            isDBValid = false;
        }
        else{

            for(Object node : unlinkedNodes){

                if(!(node instanceof DataProjection)){
                    //Uh-oh! We found a lonely node that is not DataProjection!!!

                    unlinkedNodeTypeSet.add(node.getClass().getSimpleName());
                }

            }

        }

        //TODO: Check if DataProjection nodes are there
        //TODO: Define additional db checks!





        //list errors
        if(unlinkedNodeTypeSet.size() > 0){
            noWarnings = false;
            log.warn("Node types found without a relationship: "+unlinkedNodeTypeSet.toString());
            log.warn("Get those nodes a relationship!");
        }


        //inform user
        if(noWarnings && isDBValid){

            log.info("*******************************************************");
            log.info("* Finished DB validation: VALID DATABASE, NO WARNINGS *");
            log.info("*******************************************************");
        }
        else if(!noWarnings){
            log.warn("********************************************************");
            log.warn("* Finished DB validation: VALID DATABASE WITH WARNINGS *");
            log.warn("********************************************************");
        }
        else if(!isDBValid){

            log.error("*******************************************************");
            log.error("* Finished DB validation: INVALID DATABASE            *");
            log.error("*******************************************************");
        }



    }




}
