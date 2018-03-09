package org.pdxfinder.commands;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.dao.OntologyTerm;
import org.pdxfinder.dao.Sample;
import org.pdxfinder.dao.SampleToOntologyRelationShip;
import org.pdxfinder.ontologymapping.MappingRule;
import org.pdxfinder.ontologymapping.MissingMapping;
import org.pdxfinder.utilities.LoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

/*
 * Created by csaba on 24/08/2017.
 */
@Component
@Order(value = 95)
public class LinkSamplesToNCITTerms implements CommandLineRunner {


    private static final String spreadsheetServiceUrl = "http://gsx2json.com/api?id=17LixNQL_BoL_-yev1s_VJt9FDZAJQcl5kyMhHkSF7Xk";
    //old mappings file
    //https://docs.google.com/spreadsheets/d/16JhGWCEUimsOF8q8bYN7wEJqVtjbO259X1YGrbRQLdc/edit

    //new mappings: https://docs.google.com/spreadsheets/d/17LixNQL_BoL_-yev1s_VJt9FDZAJQcl5kyMhHkSF7Xk
    private final static Logger log = LoggerFactory.getLogger(LinkSamplesToNCITTerms.class);
    private LoaderUtils loaderUtils;

    private Map<String, Set<MissingMapping>> missingMappings;
    private Set<String> missingTerms;

    private Map<String, MappingRule> mappingRules;

    @Autowired
    public LinkSamplesToNCITTerms(LoaderUtils loaderUtils) {
        this.loaderUtils = loaderUtils;
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

        String json = parseURL(spreadsheetServiceUrl);
        log.info("Fetching mapping rules from " + spreadsheetServiceUrl);

        this.mappingRules = new HashMap<>();

        try {
            JSONObject job = new JSONObject(json);
            if (job.has("rows")) {
                JSONArray rows = job.getJSONArray("rows");


                for (int i = 0; i < rows.length(); i++) {
                    JSONObject row = rows.getJSONObject(i);

                    String dataSource = row.getString("datasource");
                    String sampleDiagnosis = row.getString("samplediagnosis").toLowerCase();
                    String originTissue = row.getString("origintissue");
                    String tumorType = row.getString("tumortype");
                    String ontologyTerm = row.getString("ontologyterm");
                    String mapType = row.getString("maptype");
                    String justification = row.getString("justification");

                    if (ontologyTerm.equals("") || ontologyTerm == null) continue;
                    if (sampleDiagnosis.equals("") || sampleDiagnosis == null) continue;

                    String updatedDiagnosis = sampleDiagnosis;
                    String pattern = "(.*)Malignant(.*)Neoplasm(.*)";

                    if (sampleDiagnosis.matches(pattern)) {
                        updatedDiagnosis = (sampleDiagnosis.replaceAll(pattern, "\t$1$2Cancer$3")).trim();
                        log.info("Updating label from mapping service of diagnosis '{}' with '{}'", sampleDiagnosis, updatedDiagnosis);
                    }

                    // Remove commas from diagnosis
                    sampleDiagnosis = updatedDiagnosis.replaceAll(",", "");

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

                    MappingRule rule = new MappingRule();
                    rule.setOntologyTerm(ontologyTerm);
                    rule.setMapType(mapType);
                    rule.setJustification(justification);

                    this.mappingRules.put(dataSource + sampleDiagnosis + originTissue + tumorType, rule);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private MappingRule getMappingForSample(String dataSource, String diagnosis, String originTissue, String tumorType) {

        MappingRule mr = new MappingRule();

        //0. if diagnosis is empty return empty object
        if (diagnosis.equals("") || diagnosis == null) return mr;

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
        return mr;
    }

    private void mapSamplesToTerms() {

        int batchSize = 50;
        int startNode = 0;
        int maxSamplesNumber = loaderUtils.getHumanSamplesNumber();

        this.missingMappings = new HashMap<>();
        this.missingTerms = new HashSet<>();


        while (startNode < maxSamplesNumber) {

            log.info("Mapping " + batchSize + " samples from " + startNode);
            Collection<Sample> samples = loaderUtils.getHumanSamplesFromTo(startNode, batchSize);

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


                MappingRule mappingRule = getMappingForSample(dataSource, updatedDiagnosis, originTissue, tumorType);

                //deal with empty mapping rules here!
                if (mappingRule.getOntologyTerm() == null || mappingRule.getOntologyTerm().equals("")) {

                    MissingMapping mm = new MissingMapping(dataSource, updatedDiagnosis, originTissue, tumorType);
                    insertMissingMapping(updatedDiagnosis, mm);

                } else {

                    OntologyTerm ot = loaderUtils.getOntologyTermByLabel(mappingRule.getOntologyTerm());


                    if (ot == null) {

                        log.warn("Missing ontology term: " + mappingRule.getOntologyTerm());
                        this.missingTerms.add(mappingRule.getOntologyTerm());
                    } else {
                        ot.setDirectMappedSamplesNumber(ot.getDirectMappedSamplesNumber() + 1);
                        SampleToOntologyRelationShip r = new SampleToOntologyRelationShip(mappingRule.getMapType(), mappingRule.getJustification(), sample, ot);
                        sample.setSampleToOntologyRelationShip(r);
                        ot.setMappedTo(r);
                        loaderUtils.saveSample(sample);
                        loaderUtils.saveOntologyTerm(ot);
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

        Collection<OntologyTerm> termsWithDirectMappings = loaderUtils.getAllOntologyTermsWithNotZeroDirectMapping();
        int remainingTermsToUpdate = termsWithDirectMappings.size();
        log.info("Found " + remainingTermsToUpdate + " terms with direct number. Updating graph...");

        for (OntologyTerm ot : termsWithDirectMappings) {

            Set<OntologyTerm> discoveredTerms = new HashSet<>();
            Set<String> visitedTerms = new HashSet<>();
            Collection<OntologyTerm> parents = loaderUtils.getAllDirectParents(ot.getUrl());

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
                loaderUtils.saveOntologyTerm(currentParentTerm);
                //get parents
                parents = loaderUtils.getAllDirectParents(currentParentTerm.getUrl());

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

        loaderUtils.deleteOntologyTermsWithoutMapping();
    }


    private String parseURL(String urlStr) {
        StringBuilder sb = new StringBuilder();

        try {
            URL url = new URL(urlStr);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(url.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            log.error("Unable to read from URL " + urlStr, e);
        }
        return sb.toString();
    }

}
