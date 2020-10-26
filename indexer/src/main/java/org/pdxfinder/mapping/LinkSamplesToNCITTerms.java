package org.pdxfinder.mapping;

import org.pdxfinder.rdbms.dao.MappingEntity;
import org.pdxfinder.graph.dao.OntologyTerm;
import org.pdxfinder.graph.dao.Sample;
import org.pdxfinder.graph.dao.SampleToOntologyRelationship;
import org.pdxfinder.ontologymapping.MissingMapping;
import org.pdxfinder.rdbms.repositories.MappingEntityRepository;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.MappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@Order(value = 40)
public class LinkSamplesToNCITTerms {


    private final static Logger log = LoggerFactory.getLogger(LinkSamplesToNCITTerms.class);
    private DataImportService dataImportService;
    private MappingService mappingService;

    private Map<String, MissingMapping> missingMappings;
    private Set<String> missingTerms;

    @Autowired
    public LinkSamplesToNCITTerms(DataImportService dataImportService,
                                  MappingService mappingService,
                                  MappingEntityRepository mappingEntityRepository) {
        this.dataImportService = dataImportService;
        this.mappingService = mappingService;
    }

    public void run() {

        long startTime = System.currentTimeMillis();

        log.info("Mapping samples to NCIT terms.");

        mapSamplesToTerms();
        updateIndirectMappingData();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int seconds = (int) (totalTime / 1000) % 60;
        int minutes = (int) ((totalTime / (1000 * 60)) % 60);

        log.info(this.getClass().getSimpleName() + " finished after " + minutes + " minute(s) and " + seconds + " second(s)");
    }


    private void mapSamplesToTerms() {


        int batchSize = 50;
        int startNode = 0;
        int maxSamplesNumber = dataImportService.getHumanSamplesNumber();

        this.missingMappings = new HashMap<>();
        this.missingTerms = new HashSet<>();


        while (startNode < maxSamplesNumber) {

            log.info("Mapping " + batchSize + " samples from " + startNode);
            Collection<Sample> samples = dataImportService.findHumanSamplesFromTo(startNode, batchSize);

            for (Sample sample : samples) {

                String dataSource = "";
                String diagnosis = "";
                String originTissue = "";
                String tumorType = "";

                dataSource = sample.getDataSource();
                diagnosis = sample.getDiagnosis();


                if (sample.getOriginTissue() != null) {
                    originTissue = sample.getOriginTissue().getName();
                }

                if (sample.getType() != null) {
                    tumorType = sample.getType().getName();
                }

                MappingEntity me = mappingService.getDiagnosisMapping(dataSource, diagnosis, originTissue, tumorType);

                if(me == null){

                    MissingMapping mm = new MissingMapping(dataSource, diagnosis, originTissue, tumorType);
                    String mapKey = mappingService.getDiagnosisMappingKey(dataSource, diagnosis, originTissue, tumorType);
                    insertMissingMapping(mapKey, mm);

                }
                 else {

                    OntologyTerm ot = dataImportService.findOntologyTermByUrl(me.getMappedTermUrl());


                    if (ot == null) {

                        log.warn("Missing ontology term: " + me.getMappedTermLabel());
                        this.missingTerms.add(me.getMappedTermLabel());
                    } else {
                        ot.setDirectMappedSamplesNumber(ot.getDirectMappedSamplesNumber() + 1);
                        SampleToOntologyRelationship r = new SampleToOntologyRelationship(me.getMapType(), me.getJustification(), sample, ot);
                        sample.setSampleToOntologyRelationShip(r);
                        ot.setSampleMappedTo(r);
                        dataImportService.saveSample(sample);
                        dataImportService.saveOntologyTerm(ot);
                        //log.info("Mapping "+diagnosis+" to "+mappingRule.getOntologyTerm());
                    }
                }
            }
            startNode += batchSize;
        }


        if (this.missingMappings.size() > 0) {
            printAndSaveMissingMappings();
        }
        else{
            log.info("YAY, we mapped all samples to an ontology term!");
        }
    }

    private void insertMissingMapping(String id, MissingMapping mm) {

        if (!this.missingMappings.containsKey(id)) {
            this.missingMappings.put(id, mm);
        }
    }


    private void printAndSaveMissingMappings() {

        log.warn("Couldn't map samples with the following details(" + this.missingMappings.size() + "): ");

        for(Map.Entry<String, MissingMapping> entry : missingMappings.entrySet()){

            MissingMapping mm = entry.getValue();
            System.out.println("Datasource: " + mm.getDataSource() + ", Diagnosis: " + mm.getDiagnosis() + ", Origin Tissue: " + mm.getOriginTissue() + ", Tumor Type: " + mm.getTumorType());
            mappingService.saveUnmappedDiagnosis(mm.getDataSource(), mm.getDiagnosis(), mm.getOriginTissue(), mm.getTumorType());
        }


    }




    private void updateIndirectMappingData() {

        Collection<OntologyTerm> termsWithDirectMappings = dataImportService.getAllOntologyTermsWithNotZeroDirectMapping();
        int remainingTermsToUpdate = termsWithDirectMappings.size();
        log.info("Found " + remainingTermsToUpdate + " terms with direct number. Updating graph...");

        for (OntologyTerm ot : termsWithDirectMappings) {

            ot.setAllowAsSuggestion(true);
            Set<OntologyTerm> discoveredTerms = new HashSet<>();
            Set<String> visitedTerms = new HashSet<>();
            Collection<OntologyTerm> parents = dataImportService.getAllDirectParents(ot.getUrl());

            if (parents != null) {

                discoveredTerms.addAll(parents);
            }

            while (discoveredTerms.size() > 0) {
                OntologyTerm currentParentTerm = discoveredTerms.iterator().next();

                if (visitedTerms.contains(currentParentTerm.getUrl())) {
                    discoveredTerms.remove(currentParentTerm);
                    continue;
                }

                visitedTerms.add(currentParentTerm.getUrl());
                //update indirect number
                currentParentTerm.setIndirectMappedSamplesNumber(currentParentTerm.getIndirectMappedSamplesNumber() + ot.getDirectMappedSamplesNumber());
                currentParentTerm.setAllowAsSuggestion(true);

                dataImportService.saveOntologyTerm(currentParentTerm);
                //get parents
                parents = dataImportService.getAllDirectParents(currentParentTerm.getUrl());

                if (parents != null) {

                    discoveredTerms.addAll(parents);
                }

                discoveredTerms.remove(currentParentTerm);

            }
            remainingTermsToUpdate--;
            log.info("Updated subgraph for " + ot.getLabel() + ", " + remainingTermsToUpdate + " term(s) remained");
        }
    }


    private void deleteTermsWithoutMapping() {

        log.info("Deleting terms and their relationships where direct and indirectMappedNumber is zero");

        dataImportService.deleteOntologyTermsWithoutMapping();
    }


}
