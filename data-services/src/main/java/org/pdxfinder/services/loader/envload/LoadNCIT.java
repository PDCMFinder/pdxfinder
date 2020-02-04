package org.pdxfinder.services.loader.envload;

import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.graph.dao.OntologyTerm;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.services.constants.DataUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class LoadNCIT {


    private static final Logger log = LoggerFactory.getLogger(LoadNCIT.class);

    private static final String UTF8 = "UTF-8";
    private static final String EMBEDDED = "_embedded";

    private DataImportService dataImportService;
    private UtilityService utilityService;

    public LoadNCIT(DataImportService dataImportService, UtilityService utilityService) {
        this.dataImportService = dataImportService;
        this.utilityService = utilityService;
    }


    public void loadOntology(String branchUrl){

        long startTime = System.currentTimeMillis();

        log.info("Loading all Neoplasm subnodes. ");

        Set<String> loadedTerms = new HashSet<>();
        Set<OntologyTerm> discoveredTerms = new HashSet<>();

        String diseaseRootLabel = "Cancer"; // Neoplasm

        int requestCounter = 0;

        //create cancer root term
        OntologyTerm ot = dataImportService.getOntologyTerm(branchUrl,diseaseRootLabel);
        ot.setType("diagnosis");
        log.debug("Creating node: {}", diseaseRootLabel);

        discoveredTerms.add(ot);

        while(!discoveredTerms.isEmpty()){
            //get term from notVisited

            OntologyTerm notYetVisitedTerm = discoveredTerms.iterator().next();
            discoveredTerms.remove(notYetVisitedTerm);

            if(loadedTerms.contains(notYetVisitedTerm.getUrl())) continue;

            loadedTerms.add(notYetVisitedTerm.getUrl());

            String parentUrlEncoded = "";
            try {
                //have to double encode the url to get the desired result
                parentUrlEncoded = URLEncoder.encode(notYetVisitedTerm.getUrl(), UTF8);
                parentUrlEncoded = URLEncoder.encode(parentUrlEncoded, UTF8);

            } catch (UnsupportedEncodingException e) {
               log.warn(e.getMessage());
            }
            String url = DataUrl.ONTOLOGY_URL.get()+parentUrlEncoded+"/hierarchicalChildren?size=200";

            log.debug("Getting data from {}", url);

            String json = utilityService.parseURL(url);
            requestCounter++;

            if(requestCounter%200 == 0){
                log.info("Terms loaded: {}", requestCounter);
            }

            try {
                JSONObject job = new JSONObject(json);
                if (!job.has(EMBEDDED)) continue;
                String embedded = job.getString(EMBEDDED);

                //if this term does not have child nodes, continue

                JSONObject job2 = new JSONObject(embedded);
                JSONArray terms = job2.getJSONArray("terms");


                for (int i = 0; i < terms.length(); i++) {

                    JSONObject term = terms.getJSONObject(i);
                    String termLabel = term.getString("label");

                    //do not load this branch
                    //if termLabel equals 'Neoplasm by Special Category' continue

                    log.debug("TERM: {}", termLabel);

                    // Changes Malignant * Neoplasm to * Cancer
                    String pattern = "(.*)Malignant(.*)Neoplasm(.*)";
                    String updatedTermlabel = null;

                    if (termLabel.matches(pattern)) {
                        updatedTermlabel = (termLabel.replaceAll(pattern, "\t$1$2Cancer$3")).trim();
                        log.info("Replacing term label '{}' with '{}'", termLabel, updatedTermlabel);
                    }

                    termLabel = termLabel.replaceAll(",", "");

                    OntologyTerm newTerm = dataImportService.getOntologyTerm(term.getString("iri"), updatedTermlabel != null ? updatedTermlabel : termLabel);

                    JSONArray synonyms = term.getJSONArray("synonyms");
                    Set<String> synonymsSet = new HashSet<>();

                    for(int j=0; j<synonyms.length();j++){
                        synonymsSet.add(synonyms.getString(j));
                    }

                    newTerm.setSynonyms(synonymsSet);
                    newTerm.setType("diagnosis");

                    discoveredTerms.add(newTerm);

                    OntologyTerm parentTerm = dataImportService.getOntologyTerm(notYetVisitedTerm.getUrl());
                    newTerm.addSubclass(parentTerm);
                    dataImportService.saveOntologyTerm(newTerm);

                }

            } catch (Exception e) {
                log.error(" {} ", e.getMessage());
            }
        }

        if(requestCounter%200 != 0){
            log.info("Terms loaded: {}", requestCounter);
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int seconds = (int) (totalTime / 1000) % 60;
        int minutes = (int) ((totalTime / (1000 * 60)) % 60);

        log.info("{} finished after {} minute(s) and {} second(s)", this.getClass().getSimpleName(), minutes, seconds);

    }






    public void loadNCITPreDef(String ncitFile){

        String currentLine;
        long currentLineCounter = 1;
        String[] rowData;

        Map<String, OntologyTerm> ncitLoaded = new HashMap<>();

        try(BufferedReader buf = new BufferedReader(new FileReader(ncitFile))) {

            while (true) {
                currentLine = buf.readLine();
                if (currentLine == null) {
                    break;
                } else if (currentLineCounter < 2) {
                    currentLineCounter++;
                    continue;

                } else {
                    rowData = currentLine.split("\t");
                    String url = rowData[0];
                    String label = rowData[1];

                    OntologyTerm ot = dataImportService.getOntologyTerm(url, label);
                    ncitLoaded.put(url, ot);

                    log.info("Creating "+label);
                }
                currentLineCounter++;
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }


        for (Map.Entry<String, OntologyTerm> entry : ncitLoaded.entrySet()) {
            String key = entry.getKey();
            OntologyTerm parentTerm = entry.getValue();


            String parentUrlEncoded = "";
            try {
                //have to double encode the url to get the desired result
                parentUrlEncoded = URLEncoder.encode(parentTerm.getUrl(), UTF8);
                parentUrlEncoded = URLEncoder.encode(parentUrlEncoded, UTF8);

            } catch (UnsupportedEncodingException e) {
                log.warn(e.getMessage());
            }
            String url = DataUrl.ONTOLOGY_URL.get()+parentUrlEncoded+"/hierarchicalChildren?size=100";

            log.debug("Getting data from "+url);

            String json = utilityService.parseURL(url);

            try {
                JSONObject job = new JSONObject(json);
                if (!job.has(EMBEDDED)) continue;
                String embedded = job.getString(EMBEDDED);

                //if this term does not have child nodes, continue

                JSONObject job2 = new JSONObject(embedded);
                JSONArray terms = job2.getJSONArray("terms");


                for (int i = 0; i < terms.length(); i++) {

                    JSONObject term = terms.getJSONObject(i);

                    String childTermUrl = term.getString("iri");

                    if(!ncitLoaded.containsKey(childTermUrl)) continue;

                    OntologyTerm childTerm = ncitLoaded.get(childTermUrl);

                    JSONArray synonyms = term.getJSONArray("synonyms");
                    Set<String> synonymsSet = new HashSet<>();

                    for(int j=0; j<synonyms.length();j++){
                        synonymsSet.add(synonyms.getString(j));
                    }

                    childTerm.setSynonyms(synonymsSet);

                    childTerm.addSubclass(parentTerm);
                    dataImportService.saveOntologyTerm(childTerm);

                }
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }


}
