package org.pdxfinder.commands;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.dao.OntologyTerm;
import org.pdxfinder.dao.Sample;
import org.pdxfinder.dao.SampleToOntologyRelationShip;
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
@Order(value = 100)
public class LinkSamplesToNCITTerms implements CommandLineRunner{


    private static final String spreadsheetServiceUrl = "http://gsx2json.com/api?id=17LixNQL_BoL_-yev1s_VJt9FDZAJQcl5kyMhHkSF7Xk";
    //old mappings file
    //https://docs.google.com/spreadsheets/d/16JhGWCEUimsOF8q8bYN7wEJqVtjbO259X1YGrbRQLdc/edit

    //new mappings: https://docs.google.com/spreadsheets/d/17LixNQL_BoL_-yev1s_VJt9FDZAJQcl5kyMhHkSF7Xk
    private final static Logger log = LoggerFactory.getLogger(LinkSamplesToNCITTerms.class);
    private LoaderUtils loaderUtils;

    private Map<String,Set<MissingMapping>> missingMappings;
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
        OptionSet options = parser.parse(args);

        long startTime = System.currentTimeMillis();

        if (options.has("linkSamplesToNCITTerms") && options.has("linkSamplesToNCITTermsWithCleanup")) {
            log.warn("Select one or the other of: -linkSamplesToNCITTerms, -linkSamplesToNCITTermsWithCleanup");
            log.warn("Not loading ", this.getClass().getName());

        } else if (options.has("linkSamplesToNCITTermsWithCleanup") || options.has("loadALL")) {

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


    private void loadMappingRules(){

        String json = parseURL(spreadsheetServiceUrl);
        log.info("Fetching mapping rules from "+spreadsheetServiceUrl);

        this.mappingRules = new HashMap<>();

        try {
            JSONObject job = new JSONObject(json);
            if (job.has("rows")){
                JSONArray rows = job.getJSONArray("rows");



                for(int i=0;i<rows.length();i++){
                    JSONObject row = rows.getJSONObject(i);

                    String dataSource = row.getString("datasource");
                    String sampleDiagnosis = row.getString("samplediagnosis").toLowerCase();
                    String originTissue = row.getString("origintissue");
                    String tumorType = row.getString("tumortype");
                    String ontologyTerm = row.getString("ontologyterm");
                    String mapType = row.getString("maptype");
                    String justification = row.getString("justification");

                    if(ontologyTerm.equals("") || ontologyTerm == null) continue;
                    if(sampleDiagnosis.equals("") || sampleDiagnosis == null) continue;

                    //if it is a direct mapping, add it to the rules, key = diagnosis

                    if(mapType.equals("direct")){

                        MappingRule rule = new MappingRule();

                        rule.setOntologyTerm(ontologyTerm);
                        rule.setMapType(mapType);

                        this.mappingRules.put(sampleDiagnosis, rule);

                    }
                    //if it is an inferred mapping, key = dataSource+sampleDiagnosis+originTissue+tumorType
                    else{

                        MappingRule rule = new MappingRule();

                        rule.setOntologyTerm(ontologyTerm);
                        rule.setMapType(mapType);
                        rule.setJustification(justification);

                        this.mappingRules.put(dataSource+sampleDiagnosis+originTissue+tumorType, rule);

                    }

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private MappingRule getMappingForSample(String dataSource, String diagnosis, String originTissue, String tumorType){

        MappingRule mr = new MappingRule();
        //0. if diagnosis is empty return empty object
        if(diagnosis.equals("") || diagnosis == null) return mr;


        // 1. check whether the diagnosis exists in the keys = direct map type
        if(this.mappingRules.containsKey(diagnosis.toLowerCase())){

            mr =  this.mappingRules.get(diagnosis.toLowerCase());
        }
        // 2. else check whether a general inferred rule exists, key = ANY+sampleDiagnosis+originTissue+tumorType
        if(this.mappingRules.containsKey("ANY"+diagnosis.toLowerCase()+originTissue+tumorType)){

            mr =  this.mappingRules.get("ANY"+diagnosis.toLowerCase()+originTissue+tumorType);
        }
        //3. else check whether a dataSource specific inferred rule exists, key = dataSource+sampleDiagnosis+originTissue+tumorType
        if(this.mappingRules.containsKey(dataSource+diagnosis.toLowerCase()+originTissue+tumorType)){

            mr = this.mappingRules.get(dataSource+diagnosis.toLowerCase()+originTissue+tumorType);
        }
        //4. if no mapping rule was found, return empty object

        return mr;
    }

    private void mapSamplesToTerms(){

        int batchSize = 50;
        int startNode = 0;
        int maxSamplesNumber = loaderUtils.getHumanSamplesNumber();

        this.missingMappings = new HashMap<>();
        this.missingTerms = new HashSet<>();



        while(startNode<maxSamplesNumber){

            log.info("Mapping "+batchSize+" samples from "+startNode);
            Collection<Sample> samples = loaderUtils.getHumanSamplesFromTo(startNode, batchSize);

            for(Sample sample:samples){

                String dataSource = "";
                String diagnosis = "";
                String originTissue = "";
                String tumorType = "";

                dataSource = sample.getDataSource();
                diagnosis = sample.getDiagnosis();

                if(sample.getOriginTissue() != null){
                    originTissue = sample.getOriginTissue().getName();
                }

                if(sample.getType() != null){
                    tumorType = sample.getType().getName();
                }


                MappingRule mappingRule = getMappingForSample(dataSource, diagnosis, originTissue, tumorType);

                //deal with empty mapping rules here!

                if(mappingRule.getOntologyTerm() == null || mappingRule.getOntologyTerm().equals("")){

                    MissingMapping mm = new MissingMapping(dataSource, diagnosis, originTissue, tumorType);
                    insertMissingMapping(diagnosis, mm);

                }
                else{

                    OntologyTerm ot = loaderUtils.getOntologyTermByLabel(mappingRule.getOntologyTerm());


                    if(ot == null){

                        log.warn("Missing ontology term: "+mappingRule.getOntologyTerm());
                        this.missingTerms.add(mappingRule.getOntologyTerm());
                    }
                    else{
                        ot.setDirectMappedSamplesNumber(ot.getDirectMappedSamplesNumber() + 1);
                        SampleToOntologyRelationShip r = new SampleToOntologyRelationShip( mappingRule.getMapType(), mappingRule.getJustification(), sample, ot);
                        sample.setSampleToOntologyRelationShip(r);
                        ot.setMappedTo(r);
                        loaderUtils.saveSample(sample);
                        loaderUtils.saveOntologyTerm(ot);
                        //log.info("Mapping "+diagnosis+" to "+mappingRule.getOntologyTerm());
                    }
                }



            }

            startNode+=batchSize;
        }

        printMissingMappings();
    }

    private void insertMissingMapping(String id, MissingMapping mm){

        if(!this.missingMappings.containsKey(id)){
            //log.info("No mapping found for "+id);
            Set<MissingMapping> lmm = new HashSet<>();
            lmm.add(mm);
            this.missingMappings.put(id, lmm);
        }
        else{

            this.missingMappings.get(id).add(mm);
        }
    }


    private void printMissingMappings(){

        log.warn("Couldn't map samples with the following details("+this.missingMappings.size()+"): ");
        for(Set<MissingMapping> mms:this.missingMappings.values()){

            for(MissingMapping mm:mms){
                log.warn(mm.getDataSource()+ " "+mm.getDiagnosis()+" "+mm.getOriginTissue()+" "+mm.getTumorType());

            }
        }
    }

    private void updateIndirectMappingData(){

        Collection<OntologyTerm> termsWithDirectMappings = loaderUtils.getAllOntologyTermsWithNotZeroDirectMapping();
        int remainingTermsToUpdate = termsWithDirectMappings.size();
        log.info("Found "+remainingTermsToUpdate+" terms with direct number. Updating graph...");

        for(OntologyTerm ot:termsWithDirectMappings){

            Set<OntologyTerm> discoveredTerms = new HashSet<>();
            Set<String> visitedTerms = new HashSet<>();
            Collection<OntologyTerm> parents = loaderUtils.getAllDirectParents(ot.getUrl());

            if(parents != null){

                discoveredTerms.addAll(parents);
            }

            while(discoveredTerms.size()>0) {
                OntologyTerm currentParentTerm = discoveredTerms.iterator().next();

                if(visitedTerms.contains(currentParentTerm.getUrl())) {
                    discoveredTerms.remove(currentParentTerm);
                    continue;
                }

                visitedTerms.add(currentParentTerm.getUrl());
                //update indirect number
                currentParentTerm.setIndirectMappedSamplesNumber(currentParentTerm.getIndirectMappedSamplesNumber()+ot.getDirectMappedSamplesNumber());
                loaderUtils.saveOntologyTerm(currentParentTerm);
                //get parents
                parents = loaderUtils.getAllDirectParents(currentParentTerm.getUrl());

                if(parents != null){

                    discoveredTerms.addAll(parents);
                }

                discoveredTerms.remove(currentParentTerm);

            }
            remainingTermsToUpdate--;
            log.info("Updated subgraph for "+ot.getLabel()+", "+remainingTermsToUpdate+" term(s) remained");
        }
    }


    private void deleteTermsWithoutMapping(){

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
