package org.pdxfinder.commands;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.graph.dao.OntologyTerm;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by csaba on 12/06/2017.
 */
@Component
@Order(value = -100)
public class LoadDiseaseOntology implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadDiseaseOntology.class);

    private static final String cancerBranchUrl = "http://purl.obolibrary.org/obo/DOID_162";
    private static final String ontologyUrl = "http://www.ebi.ac.uk/ols/api/ontologies/doid/terms/";

    private DataImportService dataImportService;

    @Autowired
    private UtilityService utilityService;

    @Autowired
    public LoadDiseaseOntology(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadDO", "Load Disease ontology");
        OptionSet options = parser.parse(args);

        long startTime = System.currentTimeMillis();

        if (options.has("loadDO")) {

            log.info("Loading cancer branch of Disease Ontology.");
            loadDO();
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int seconds = (int) (totalTime / 1000) % 60;
        int minutes = (int) ((totalTime / (1000 * 60)) % 60);

        log.info(this.getClass().getSimpleName() + " finished after " + minutes + " minute(s) and " + seconds + " second(s)");

    }



    private void loadDO(){

        Set<String> loadedTerms = new HashSet<>();
        Set<OntologyTerm> discoveredTerms = new HashSet<>();

        String cancerRootLabel = "cancer";

        int termCounter = 1;
        int requestCounter = 0;

        //create cancer root term
        OntologyTerm ot = dataImportService.getOntologyTerm(cancerBranchUrl,cancerRootLabel);
        System.out.println("Creating node: "+cancerRootLabel);

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
            String url = ontologyUrl+parentUrlEncoded+"/hierarchicalChildren?size=100";

            System.out.println("Getting data from "+url);

            String json = utilityService.parseURL(url);
            requestCounter++;
            try {
                JSONObject job = new JSONObject(json);
                if (!job.has("_embedded")) continue;
                String embedded = job.getString("_embedded");

                //if this term does not have child nodes, continue

                JSONObject job2 = new JSONObject(embedded);
                JSONArray terms = job2.getJSONArray("terms");


                for (int i = 0; i < terms.length(); i++) {

                    JSONObject term = terms.getJSONObject(i);
                    System.out.println("TERM: "+term.getString("label"));

                    OntologyTerm newTerm = dataImportService.getOntologyTerm(term.getString("iri"), term.getString("label"));
                    discoveredTerms.add(newTerm);

                    OntologyTerm parentTerm = dataImportService.getOntologyTerm(notYetVisitedTerm.getUrl());
                    newTerm.addSubclass(parentTerm);
                    dataImportService.saveOntologyTerm(newTerm);

                    termCounter++;
                }

            } catch (Exception e) {
                log.error("", e);

            }

            System.out.println("Requests made: " + requestCounter);

        }

    }

}
