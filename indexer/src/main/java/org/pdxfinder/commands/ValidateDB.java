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
import java.util.Set;

@Component
@Order(value = 95)
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


        /*
            Scenarios when DB is invalid:
            - Missing DataProjection nodes
            - Patients with multiple treatmentsummaries

            Scenarios when DB is valid with warnings:
            - Found nodes without relationships and they are not DataProjections
            - Platforms without url attribute value
         */




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


        //check if there is any patient with multiple treatment summaries
        log.info("Looking for patients with multiple treatment summaries");
        Set<Object> patientsWithMultipleSummaries = dataImportService.findPatientsWithMultipleSummaries();

        if(patientsWithMultipleSummaries.size() > 0){
            isDBValid = false;

            log.error("Found patients with multiple treatment summaries!");
        }

        //check if there is any platform without url
        log.info("Validating platforms");
        Set<Object> platformsWithoutUrl = dataImportService.findPlatformsWithoutUrl();

        if(platformsWithoutUrl.size() > 0){

            noWarnings = false;
            log.warn("Found Platforms without url: "+ platformsWithoutUrl.toString());
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
