package org.pdxfinder.commands.mapping;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.rdbms.dao.MappingEntity;
import org.pdxfinder.graph.dao.OntologyTerm;
import org.pdxfinder.graph.dao.Sample;
import org.pdxfinder.graph.dao.SampleToOntologyRelationship;
import org.pdxfinder.ontologymapping.MissingMapping;
import org.pdxfinder.rdbms.repositories.MappingEntityRepository;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.MappingService;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/*
 * Created by csaba on 24/08/2017.
 */
@Component
@Order(value = 40)
public class LinkSamplesToNCITTerms implements CommandLineRunner {



    private final static Logger log = LoggerFactory.getLogger(LinkSamplesToNCITTerms.class);
    private DataImportService dataImportService;
    private UtilityService utilityService;
    private MappingService mappingService;

    private Map<String, Set<MissingMapping>> missingMappings;
    private Set<String> missingTerms;

    private Map<String, MappingEntity> mappingRules;

    private MappingEntityRepository mappingEntityRepository;

    @Autowired
    public LinkSamplesToNCITTerms(DataImportService dataImportService,
                                  UtilityService utilityService,
                                  MappingService mappingService,
                                  MappingEntityRepository mappingEntityRepository) {
        this.dataImportService = dataImportService;
        this.utilityService = utilityService;
        this.mappingService = mappingService;
        this.mappingEntityRepository = mappingEntityRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("linkSamplesToNCITTerms", "Link samples to NCIT terms");
        parser.accepts("linkSamplesToNCITTermsWithCleanup", "Link samples to NCIT terms, then cleanup.");
        parser.accepts("loadALL", "Load all, including linking samples to NCIT terms");
        parser.accepts("loadSlim", "Load slim, then link samples to NCIT terms");
        parser.accepts("loadEssentials", "Load essentials then link samples to terms");

        OptionSet options = parser.parse(args);

        long startTime = System.currentTimeMillis();

        if (options.has("linkSamplesToNCITTerms") && options.has("linkSamplesToNCITTermsWithCleanup")) {
            log.warn("Select one or the other of: -linkSamplesToNCITTerms, -linkSamplesToNCITTermsWithCleanup");
            log.warn("Not loading ", this.getClass().getName());

        } else if (options.has("linkSamplesToNCITTermsWithCleanup") || options.has("loadALL")  || options.has("loadSlim") || options.has("loadEssentials")) {

            log.info("Mapping samples to NCIT terms with cleanup.");

            mapSamplesToTerms();
            updateIndirectMappingData();
            //deleteTermsWithoutMapping();

        } else if (options.has("linkSamplesToNCITTerms")) {

            log.info("Mapping samples to NCIT terms.");

            mapSamplesToTerms();
            updateIndirectMappingData();

        }

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
                    insertMissingMapping(diagnosis, mm);

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
            //log.info("No mapping found for "+id);
            Set<MissingMapping> lmm = new HashSet<>();
            lmm.add(mm);
            this.missingMappings.put(id, lmm);
        } else {

            this.missingMappings.get(id).add(mm);
        }
    }


    private void printAndSaveMissingMappings() {

        log.warn("Couldn't map samples with the following details(" + this.missingMappings.size() + "): ");
        for (Set<MissingMapping> mms : this.missingMappings.values()) {

            for (MissingMapping mm : mms) {

                log.warn("Datasource: " + mm.getDataSource() + ", Diagnosis: " + mm.getDiagnosis() + ", Origin Tissue: " + mm.getOriginTissue() + ", Tumor Type: " + mm.getTumorType());

                saveUnmappedDiagnosis(mm.getDataSource(), mm.getDiagnosis(), mm.getOriginTissue(), mm.getTumorType());
            }
        }

    }



    public void saveUnmappedDiagnosis(String dataSource, String diagnosis, String originTissue, String tumorType){

        ArrayList<String> mappingLabels = new ArrayList<>();
        mappingLabels.add("DataSource");
        mappingLabels.add("SampleDiagnosis");
        mappingLabels.add("OriginTissue");
        mappingLabels.add("TumorType");

        Map mappingValues = new HashMap();
        mappingValues.put("OriginTissue", originTissue);
        mappingValues.put("DataSource", dataSource);
        mappingValues.put("SampleDiagnosis", diagnosis);
        mappingValues.put("TumorType", tumorType);

        MappingEntity mappingEntity = new MappingEntity("DIAGNOSIS", mappingLabels, mappingValues);
        mappingEntity.setStatus("Created");
        mappingEntity.setDateCreated(new Date());

        String mappingKey = StringUtils.join(
                Arrays.asList(dataSource, diagnosis, originTissue, tumorType), "__"
        );
        mappingEntity.setMappingKey(mappingKey);

        MappingEntity entity = mappingEntityRepository.findByMappingKey(mappingKey);

        if(entity == null){

            mappingEntityRepository.save(mappingEntity);

        }else{

            log.warn("NOT SAVED: {}_{}_{}_{} was found in the Database", dataSource, diagnosis, originTissue, tumorType);
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
