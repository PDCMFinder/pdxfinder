package org.pdxfinder.commands.envload;

/**
 * Created by csaba on 07/05/2019.
 */

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.graph.dao.Drug;
import org.pdxfinder.graph.dao.OntologyTerm;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Order(value = -65)
public class LoadNCITDrugs implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadNCIT.class);

    private static final String drugsBranchUrl = "http://purl.obolibrary.org/obo/NCIT_C1908";
    private static final String ontologyUrl = "https://www.ebi.ac.uk/ols/api/ontologies/ncit/terms/";

    private DataImportService dataImportService;

    @Autowired
    private UtilityService utilityService;

    @Autowired
    public LoadNCITDrugs(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @Override
    public void run(String... args) throws Exception {


        //http://www.ebi.ac.uk/ols/api/ontologies/ncit/terms/http%253A%252F%252Fpurl.obolibrary.org%252Fobo%252FNCIT_C7057/hierarchicalChildren
        //http://www.ebi.ac.uk/ols/api/ontologies/doid/terms/http%253A%252F%252Fpurl.obolibrary.org%252Fobo%252FNCIT_C7057/hierarchicalChildren

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadNCITDrugs", "Load NCIT drugs");
        parser.accepts("loadALL", "Load all, including NCiT drug ontology");
        parser.accepts("loadEssentials", "Loading essentials");
        OptionSet options = parser.parse(args);

        long startTime = System.currentTimeMillis();

        if (options.has("loadNCITDrugs") || options.has("loadALL")  || options.has("loadEssentials")) {

            log.info("Loading all Drugs from NCIT.");
            loadNCITLeafDrugs();

        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int seconds = (int) (totalTime / 1000) % 60;
        int minutes = (int) ((totalTime / (1000 * 60)) % 60);

        log.info(this.getClass().getSimpleName() + " finished after " + minutes + " minute(s) and " + seconds + " second(s)");

    }



    private void loadNCITDrugs(){

        Set<String> loadedTerms = new HashSet<>();
        Set<OntologyTerm> discoveredTerms = new HashSet<>();

        String drugsRootLabel = "Drugs"; // Neoplasm

        int termCounter = 1;
        int requestCounter = 0;

        //create drug root term
        OntologyTerm ot = dataImportService.getOntologyTerm(drugsBranchUrl,drugsRootLabel);
        log.info("Creating node: "+drugsRootLabel);

        discoveredTerms.add(ot);

        while(discoveredTerms.size()>0){
            //get term from notVisited

            OntologyTerm notYetVisitedTerm = discoveredTerms.iterator().next();
            discoveredTerms.remove(notYetVisitedTerm);

            if(loadedTerms.contains(notYetVisitedTerm.getUrl())) continue;

            loadedTerms.add(notYetVisitedTerm.getUrl());

            String parentUrlEncoded = "";
            try {
                //have to double encode the url to get the desired result
                parentUrlEncoded = URLEncoder.encode(notYetVisitedTerm.getUrl(), "UTF-8");
                parentUrlEncoded = URLEncoder.encode(parentUrlEncoded, "UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String url = ontologyUrl+parentUrlEncoded+"/hierarchicalChildren?size=200";

            log.info("Getting data from "+url);

            String json = utilityService.parseURL(url);
            requestCounter++;

            if(requestCounter%200 == 0){
                log.info("Terms loaded: " + requestCounter);
            }

            try {
                JSONObject job = new JSONObject(json);
                if (!job.has("_embedded")) continue;
                String embedded = job.getString("_embedded");

                //if this term does not have child nodes, continue

                JSONObject job2 = new JSONObject(embedded);
                JSONArray terms = job2.getJSONArray("terms");


                for (int i = 0; i < terms.length(); i++) {

                    JSONObject term = terms.getJSONObject(i);
                    String termLabel = term.getString("label");

                    log.debug("TERM: "+termLabel);

                    termLabel = termLabel.replaceAll(",", "");

                    OntologyTerm newTerm = dataImportService.getOntologyTerm(term.getString("iri"), termLabel);

                    JSONArray synonyms = term.getJSONArray("synonyms");
                    Set<String> synonymsSet = new HashSet<>();

                    for(int j=0; j<synonyms.length();j++){
                        synonymsSet.add(synonyms.getString(j));
                    }

                    newTerm.setSynonyms(synonymsSet);
                    discoveredTerms.add(newTerm);

                    OntologyTerm parentTerm = dataImportService.getOntologyTerm(notYetVisitedTerm.getUrl());
                    newTerm.addSubclass(parentTerm);
                    dataImportService.saveOntologyTerm(newTerm);

                    termCounter++;


                }

            } catch (Exception e) {
                log.error("", e);

            }

        }

        if(requestCounter%200 != 0){
            log.info("Terms loaded: " + requestCounter);
        }

    }



    private void loadNCITLeafDrugs(){

        int totalDrugs = 0;
        int totalPages = 0;
        boolean totalPagesDetermined = false;


        for(int currentPage = 0; currentPage<= totalPages; currentPage++){

            String encodedTermUrl = "";
            try {
                //have to double encode the url to get the desired result
                encodedTermUrl = URLEncoder.encode(drugsBranchUrl, "UTF-8");
                encodedTermUrl = URLEncoder.encode(encodedTermUrl, "UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String url = ontologyUrl+encodedTermUrl+"/hierarchicalDescendants?size=500&page="+currentPage;

            String json = utilityService.parseURL(url);

            try {
                JSONObject job = new JSONObject(json);

                String embedded = job.getString("_embedded");

                JSONObject job2 = new JSONObject(embedded);
                JSONArray terms = job2.getJSONArray("terms");

                for (int j = 0; j < terms.length(); j++) {

                    JSONObject term = terms.getJSONObject(j);

                    boolean hasChildren = Boolean.parseBoolean(term.getString("has_children"));

                    if(!hasChildren){


                        Drug drug = new Drug();
                        String drugName = term.getString("label");
                        String description = "";
                        String synonyms = "";

                        if(term.has("description")){
                            description = term.getString("description");
                        }

                        if(term.has("synonyms")){
                            JSONArray syn = term.getJSONArray("synonyms");
                            List<Object> synList = syn.toList();

                            for(int k=0; k<synList.size(); k++){
                                String s = (String)synList.get(k);
                                synonyms += s;
                                synonyms += ";";

                            }
                        }

                        drug.setName(drugName);
                        drug.setSynonyms(synonyms);
                        drug.setDescription(description);
                        dataImportService.createDrug(drug);

                        totalDrugs++;

                    }

                    if(totalDrugs != 0 && totalDrugs % 500 == 0) {
                        log.info("Loaded "+totalDrugs + " drugs from NCIT.");
                    }


                }

                if(!totalPagesDetermined){
                    String page = job.getString("page");
                    JSONObject pageObj = new JSONObject(page);
                    totalPages = Integer.parseInt(pageObj.getString("totalPages")) -1;
                    totalPagesDetermined = true;
                }


            } catch (Exception e) {
                log.error("", e);

            }

        }

        log.info("Finished loading "+totalDrugs+ " drugs from NCIT.");
    }

}


/*

TOTAL terms: 2001
TOTAL relationships: 2731
Loading finished after 2 minute(s) and 40 second(s)

Deleted 2119 nodes, deleted 2731 relationships, statement completed in 32 ms.
 */

/*
private void loadDO(){

    Set<OntologyTerm> loadedTerms = new HashSet<>();

    int termCounter = 1;
    String cancerRootLabel = "cancer";
    int relationshipCounter = 0;

    //create cancer root term
    OntologyTerm ot = loaderUtils.getOntologyTerm(cancerBranchUrl,cancerRootLabel);
    System.out.println("Creating node: "+cancerRootLabel);

    String cancerUrlEncoded = "";

    try {
        //have to double encode the url to get the desired result
        cancerUrlEncoded = URLEncoder.encode(ot.getUrl(), "UTF-8");
        cancerUrlEncoded = URLEncoder.encode(cancerUrlEncoded, "UTF-8");

    } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
    }

    int totalPages = 1;
    boolean totalPagesDetermined = false;

    for (int p=0;p<totalPages;p++){

        String url = ontologyUrl+cancerUrlEncoded+"/descendants?size=500&page="+p;

        System.out.println("Getting data from "+url);

        String json = parseURL(url);

        //First create all nodes
        try {
            JSONObject job = new JSONObject(json);

            String embedded = job.getString("_embedded");

            JSONObject job2 = new JSONObject(embedded);
            JSONArray terms = job2.getJSONArray("terms");

            for (int i = 0; i < terms.length(); i++) {

                JSONObject term = terms.getJSONObject(i);
                System.out.println("Creating term: "+term.getString("label"));

                OntologyTerm newTerm = loaderUtils.getOntologyTerm(term.getString("iri"), term.getString("label"));
                loadedTerms.add(newTerm);

                termCounter++;
            }

            if(!totalPagesDetermined){
                String page = job.getString("page");
                JSONObject pageObj = new JSONObject(page);
                totalPages = Integer.parseInt(pageObj.getString("totalPages"))-1;
                totalPagesDetermined = true;
            }


        } catch (Exception e) {
            log.error("", e);

        }

    }



    //then create relationships

    String urlEnc = "";
    for (OntologyTerm t : loadedTerms) {

        OntologyTerm childTerm = loaderUtils.getOntologyTerm(t.getUrl());

        try {
            urlEnc = URLEncoder.encode(childTerm.getUrl(), "UTF-8");
            urlEnc = URLEncoder.encode(urlEnc, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        String parentUrl = ontologyUrl+urlEnc+"/parents";

        System.out.println("Getting parents data from "+parentUrl);

        String parentJson = parseURL(parentUrl);

        try {
            JSONObject job = new JSONObject(parentJson);

            String embedded = job.getString("_embedded");

            JSONObject job2 = new JSONObject(embedded);
            JSONArray terms = job2.getJSONArray("terms");

            for (int i = 0; i < terms.length(); i++) {

                JSONObject term = terms.getJSONObject(i);


                OntologyTerm parentTerm = loaderUtils.getOntologyTerm(term.getString("iri"), term.getString("label"));
                childTerm.addSubclass(parentTerm);
                loaderUtils.saveOntologyTerm(childTerm);
                relationshipCounter++;
            }

            System.out.println("Creating relationships for: "+childTerm.getLabel());



        } catch (Exception e) {
            log.error("", e);

        }

    }


    System.out.println("TOTAL terms: "+termCounter);
    System.out.println("TOTAL relationships: "+relationshipCounter);

}
*/