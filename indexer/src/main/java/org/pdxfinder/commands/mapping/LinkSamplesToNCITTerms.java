package org.pdxfinder.commands.mapping;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.rdbms.dao.MappingEntity;
import org.pdxfinder.graph.dao.OntologyTerm;
import org.pdxfinder.graph.dao.Sample;
import org.pdxfinder.graph.dao.SampleToOntologyRelationShip;
import org.pdxfinder.ontologymapping.MissingMapping;
import org.pdxfinder.services.DataImportService;
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
@Order(value = 80)
public class LinkSamplesToNCITTerms implements CommandLineRunner {


    @Value("${mappings.diagnosis.file}")
    private String diagnosisMappingsFile;

    private final static Logger log = LoggerFactory.getLogger(LinkSamplesToNCITTerms.class);
    private DataImportService dataImportService;

    @Autowired
    private UtilityService utilityService;

    private Map<String, Set<MissingMapping>> missingMappings;
    private Set<String> missingTerms;

    private Map<String, MappingEntity> mappingRules;

    @Autowired
    public LinkSamplesToNCITTerms(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("linkSamplesToNCITTerms", "Link samples to NCIT terms");
        parser.accepts("linkSamplesToNCITTermsWithCleanup", "Link samples to NCIT terms, then cleanup.");
        parser.accepts("loadALL", "Load all, including linking samples to NCIT terms");
        parser.accepts("loadSlim", "Load slim, then link samples to NCIT terms");

        OptionSet options = parser.parse(args);

        long startTime = System.currentTimeMillis();

        if (options.has("linkSamplesToNCITTerms") && options.has("linkSamplesToNCITTermsWithCleanup")) {
            log.warn("Select one or the other of: -linkSamplesToNCITTerms, -linkSamplesToNCITTermsWithCleanup");
            log.warn("Not loading ", this.getClass().getName());

        } else if (options.has("linkSamplesToNCITTermsWithCleanup") || options.has("loadALL")  || options.has("loadSlim")) {

            log.info("Mapping samples to NCIT terms with cleanup.");

            loadMappingRules();
            mapSamplesToTerms();
            updateIndirectMappingData();
            deleteTermsWithoutMapping();

        } else if (options.has("linkSamplesToNCITTerms")) {

            log.info("Mapping samples to NCIT terms.");

            loadMappingRules();
            mapSamplesToTerms();
            updateIndirectMappingData();

        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int seconds = (int) (totalTime / 1000) % 60;
        int minutes = (int) ((totalTime / (1000 * 60)) % 60);

        log.info(this.getClass().getSimpleName() + " finished after " + minutes + " minute(s) and " + seconds + " second(s)");
    }


    private void loadMappingRules() {

        String json = utilityService.parseFile(diagnosisMappingsFile);

        log.info("Fetching mapping rules from " + diagnosisMappingsFile);

        this.mappingRules = new HashMap<>();

        try {
            JSONObject job = new JSONObject(json);
            if (job.has("mappings")) {
                JSONArray rows = job.getJSONArray("mappings");


                for (int i = 0; i < rows.length(); i++) {
                    JSONObject row = rows.getJSONObject(i);

                    JSONObject mappingValues = row.getJSONObject("mappingValues");

                    String dataSource = mappingValues.getString("DataSource");
                    String sampleDiagnosis = mappingValues.getString("SampleDiagnosis").toLowerCase();
                    String originTissue = mappingValues.getString("OriginTissue");
                    String tumorType = mappingValues.getString("TumorType");

                    String ontologyTerm = row.getString("mappedTermLabel");
                    String ontologyTermUrl = row.getString("mappedTermUrl");

                    String mapType = row.getString("mapType");
                    String justification = row.getString("justification");

                    if (ontologyTerm.equals("") || ontologyTerm == null) continue;
                    if (sampleDiagnosis.equals("") || sampleDiagnosis == null) continue;


                    //DO not ask, I know it looks horrible...
                    if (originTissue == null || originTissue.equals("null")) originTissue = "";
                    if (tumorType == null || tumorType.equals("null")) tumorType = "";
                    if (justification == null || justification.equals("null")) justification = "";

                    //make everything lowercase
                    if (dataSource != null) dataSource = dataSource.toLowerCase();
                    if (originTissue != null) originTissue = originTissue.toLowerCase();
                    if (tumorType != null) tumorType = tumorType.toLowerCase();
                    sampleDiagnosis = sampleDiagnosis.toLowerCase();

                    /*
                    DATASOURCE SPECIFIC:
                    Center first
                    1) PDMR-ad-lung-met ->  lung-met-adeno

                    2) PDMR-ad-lung -> lung-adeno

                    3) PDMR-ad ->adeno

                    GENERAL:
                    Maximum information to least specific information
                    4) ad-lung-met ->  lung-met-adeno

                    5) ad-lung ->  lung-adeno

                    6) ad -> adenocarcinoma

                     */

                    MappingEntity me = new MappingEntity();
                    me.setMappedTermUrl(ontologyTermUrl);
                    me.setMappedTermLabel(ontologyTerm);
                    me.setMapType(mapType);
                    me.setJustification(justification);


                    this.mappingRules.put(dataSource + sampleDiagnosis + originTissue + tumorType, me);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private MappingEntity getMappingForSample(String dataSource, String diagnosis, String originTissue, String tumorType) {

        MappingEntity me = new MappingEntity();

        //0. if diagnosis is empty return empty object
        if (diagnosis.equals("") || diagnosis == null) return me;

        if (dataSource == null) dataSource = "";
        if (originTissue == null) originTissue = "";
        if (tumorType == null) tumorType = "";
        /*
        DATASOURCE SPECIFIC:
        Center first
        1) PDMR-ad-lung-met ->  lung-met-adeno

        2) PDMR-ad-lung -> lung-adeno

        3) PDMR-ad ->adeno

        GENERAL:
        Maximum information to least specific information
        4) ad-lung-met ->  lung-met-adeno

        5) ad-lung ->  lung-adeno

        6) ad -> adenocarcinoma

        */

        if (this.mappingRules.containsKey(dataSource.toLowerCase() + diagnosis.toLowerCase() + originTissue.toLowerCase() + tumorType.toLowerCase())) {

            return this.mappingRules.get(dataSource.toLowerCase() + diagnosis.toLowerCase() + originTissue.toLowerCase() + tumorType.toLowerCase());
        }

        //else return empty object
        //log.warn("No mapping for "+dataSource.toLowerCase() + diagnosis.toLowerCase() + originTissue.toLowerCase() + tumorType.toLowerCase());
        return me;
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

                // Per Nat 20180219, remove commas from the ontology terms
                // to fix issues with term "Invasive Ductal Carcinoma, Not Otherwise Specified"
                // URL parsing has trouble with commas
                // Decided solution is to remove commas from ontology labels
                // https://www.ebi.ac.uk/panda/jira/browse/PDXI-258
                // Changes Malignant * Neoplasm to * Cancer
                String updatedDiagnosis = diagnosis;
                String pattern = "(.*)Malignant(.*)Neoplasm(.*)";

                if (diagnosis.matches(pattern)) {
                    updatedDiagnosis = (diagnosis.replaceAll(pattern, "\t$1$2Cancer$3")).trim();
                    log.info("Updating ontology linking of diagnosis '{}' with '{}'", diagnosis, updatedDiagnosis);
                }

                updatedDiagnosis = updatedDiagnosis.replaceAll(",", "");

                if (sample.getOriginTissue() != null) {
                    originTissue = sample.getOriginTissue().getName();
                }

                if (sample.getType() != null) {
                    tumorType = sample.getType().getName();
                }


                MappingEntity me = getMappingForSample(dataSource, updatedDiagnosis, originTissue, tumorType);

                //deal with empty mapping rules here!
                if (me.getMappedTermLabel() == null || me.getMappedTermLabel().equals("")) {

                    MissingMapping mm = new MissingMapping(dataSource, updatedDiagnosis, originTissue, tumorType);
                    insertMissingMapping(updatedDiagnosis, mm);

                } else {

                    OntologyTerm ot = dataImportService.findOntologyTermByUrl(me.getMappedTermUrl());


                    if (ot == null) {

                        log.warn("Missing ontology term: " + me.getMappedTermLabel());
                        this.missingTerms.add(me.getMappedTermLabel());
                    } else {
                        ot.setDirectMappedSamplesNumber(ot.getDirectMappedSamplesNumber() + 1);
                        SampleToOntologyRelationShip r = new SampleToOntologyRelationShip(me.getMapType(), me.getJustification(), sample, ot);
                        sample.setSampleToOntologyRelationShip(r);
                        ot.setMappedTo(r);
                        dataImportService.saveSample(sample);
                        dataImportService.saveOntologyTerm(ot);
                        //log.info("Mapping "+diagnosis+" to "+mappingRule.getOntologyTerm());
                    }
                }

            }

            startNode += batchSize;
        }

        if (this.missingMappings.size() > 0) printMissingMappings();
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


    private void printMissingMappings() {

        log.warn("Couldn't map samples with the following details(" + this.missingMappings.size() + "): ");
        for (Set<MissingMapping> mms : this.missingMappings.values()) {

            for (MissingMapping mm : mms) {
                log.warn("Datasource: " + mm.getDataSource() + ", Diagnosis: " + mm.getDiagnosis() + ", Origin Tissue: " + mm.getOriginTissue() + ", Tumor Type: " + mm.getTumorType());

            }
        }
    }

    private void updateIndirectMappingData() {

        Collection<OntologyTerm> termsWithDirectMappings = dataImportService.getAllOntologyTermsWithNotZeroDirectMapping();
        int remainingTermsToUpdate = termsWithDirectMappings.size();
        log.info("Found " + remainingTermsToUpdate + " terms with direct number. Updating graph...");

        for (OntologyTerm ot : termsWithDirectMappings) {

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
