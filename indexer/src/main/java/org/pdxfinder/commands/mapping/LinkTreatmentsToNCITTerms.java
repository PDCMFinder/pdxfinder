package org.pdxfinder.commands.mapping;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.graph.dao.OntologyTerm;
import org.pdxfinder.graph.dao.Treatment;
import org.pdxfinder.graph.dao.TreatmentToOntologyRelationship;
import org.pdxfinder.rdbms.dao.MappingEntity;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.MappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/*
 * Created by csaba on 05/06/2019.
 */
@Component
@Order(value = 40)
public class LinkTreatmentsToNCITTerms implements CommandLineRunner {


    private final static Logger log = LoggerFactory.getLogger(LinkTreatmentsToNCITTerms.class);
    private MappingService mappingService;
    private DataImportService dataImportService;


    public LinkTreatmentsToNCITTerms(MappingService mappingService, DataImportService dataImportService) {
        this.mappingService = mappingService;
        this.dataImportService = dataImportService;
    }

    @Override
    public void run(String... args) throws Exception {
        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("linkTreatmentsToNCITTerms", "Link treatments to NCIT terms");
        parser.accepts("loadALL", "Load all, including linking treatments to NCIT terms");
        parser.accepts("loadSlim", "Load slim, then link treatments to NCIT terms");
        parser.accepts("loadEssentials", "Load essentials then link treatments to terms");


        OptionSet options = parser.parse(args);

        long startTime = System.currentTimeMillis();

        if (options.has("linkTreatmentsToNCITTerms") || options.has("loadALL")  || options.has("loadSlim") || options.has("loadEssentials")) {

            log.info("Mapping treatments to NCIT terms.");

            mapTreatmentsToTerms();


        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int seconds = (int) (totalTime / 1000) % 60;
        int minutes = (int) ((totalTime / (1000 * 60)) % 60);

        log.info(this.getClass().getSimpleName() + " finished after " + minutes + " minute(s) and " + seconds + " second(s)");




    }



    private void mapTreatmentsToTerms(){


        int batchSize = 50;
        int startNode = 0;



        List<Group> providerGroups = dataImportService.getAllProviderGroups();

        for(Group group: providerGroups){

            //STEP1: Look up patient treatments and link them to ontology terms

            String dataSource = group.getAbbreviation();

            int maxTreatmentNumber = dataImportService.findPatientTreatmentNumber(dataSource);

            if(maxTreatmentNumber > 0) log.info("Mapping patient treatments for "+dataSource);

            while(startNode < maxTreatmentNumber){

                Collection<Treatment> treatments = dataImportService.getPatientTreatmentFrom(startNode, batchSize, dataSource);

                addRelationshipToTreatments(treatments, dataSource);

                startNode += batchSize;
            }

            //STEP2: Look up drug dosing and link them to ontology terms

            maxTreatmentNumber = dataImportService.findDrugDosingStudyNumberByDataSource(dataSource);
            startNode = 0;

            if(maxTreatmentNumber > 0) log.info("Mapping drug dosing for "+dataSource);

            while(startNode < maxTreatmentNumber){

                Collection<Treatment> treatments = dataImportService.getModelTreatmentFrom(startNode, batchSize, dataSource);

                addRelationshipToTreatments(treatments, dataSource);

                startNode += batchSize;
            }

        }









    }


    private void addRelationshipToTreatments(Collection<Treatment> treatments, String dataSource){

        for(Treatment treatment : treatments){

            MappingEntity me = mappingService.getTreatmentMapping(dataSource, treatment.getName());

            if(me == null){

                //TODO: deal with missing mapping rules here
                log.warn("No mapping rule found for "+dataSource+" "+treatment.getName());

                mappingService.saveUnmappedTreatment(dataSource, treatment.getName());
            }
            else{


                OntologyTerm ot = dataImportService.findOntologyTermByUrl(me.getMappedTermUrl());

                if(ot == null){


                    log.error("Ontology term not found "+me.getMappedTermUrl());
                }
                else{

                    TreatmentToOntologyRelationship r = new TreatmentToOntologyRelationship();
                    r.setType(me.getMapType());
                    r.setJustification(me.getJustification());
                    r.setOntologyTerm(ot);
                    r.setTreatment(treatment);


                    treatment.setTreatmentToOntologyRelationship(r);
                    dataImportService.saveTreatment(treatment);
                }
            }
        }
    }



}
