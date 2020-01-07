package org.pdxfinder.envload;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.graph.dao.OntologyTerm;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.utils.Cmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@Order(value = -65)
public class LoadNCIT implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadNCIT.class);

    private static final String diseasesBranchUrl = "http://purl.obolibrary.org/obo/NCIT_C3262";
    private static final String ontologyUrl = "https://www.ebi.ac.uk/ols/api/ontologies/ncit/terms/";

    @Value("${ncitpredef.file}")
    private String ncitFile;

    private DataImportService dataImportService;
    private UtilityService utilityService;

    @Autowired
    public LoadNCIT(DataImportService dataImportService, UtilityService utilityService) {
        this.dataImportService = dataImportService;
        this.utilityService = utilityService;
    }

    @Override
    public void run(String... args) throws Exception {


        //http://www.ebi.ac.uk/ols/api/ontologies/ncit/terms/http%253A%252F%252Fpurl.obolibrary.org%252Fobo%252FNCIT_C7057/hierarchicalChildren
        //http://www.ebi.ac.uk/ols/api/ontologies/doid/terms/http%253A%252F%252Fpurl.obolibrary.org%252Fobo%252FNCIT_C7057/hierarchicalChildren

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts(Cmd.loadNCIT.get(), Cmd.Load_NCIT_all_ontology.get());
        parser.accepts(Cmd.loadNCITPreDef.get(), Cmd.Load_predefined_NCIT_ontology.get());
        parser.accepts(Cmd.loadALL.get(), Cmd.Load_all_including_NCiT_ontology.get());
        parser.accepts(Cmd.reloadCache.get(), Cmd.Catches_Markers_and_Ontologies.get());
        parser.accepts(Cmd.loadSlim.get(), Cmd.Load_slim_then_link_samples_to_NCIT_terms.get());
        parser.accepts(Cmd.loadEssentials.get(), Cmd.Loading_essentials.get());
        OptionSet options = parser.parse(args);

        long startTime = System.currentTimeMillis();

        if (options.has(Cmd.loadNCIT.get()) && options.has(Cmd.loadNCITPreDef.get())) {

            log.warn("Select one or the other of: -loadNCIT, -loadNCITPreDef");
            log.warn("Not loading {} ", this.getClass().getName());

        } else if (options.has(Cmd.loadNCIT.get()) ||
                options.has(Cmd.reloadCache.get()) ||
                options.has(Cmd.loadSlim.get()) ||
                options.has(Cmd.loadEssentials.get()) ||
                (options.has(Cmd.loadALL.get()) && dataImportService.countAllOntologyTerms() == 0)) {


            loadNCIT();
            utilityService.setLoadCache(true);

        } else if (options.has(Cmd.loadNCITPreDef.get())) {

            loadNCITPreDef();
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int seconds = (int) (totalTime / 1000) % 60;
        int minutes = (int) ((totalTime / (1000 * 60)) % 60);

        log.info(this.getClass().getSimpleName() + " finished after " + minutes + " minute(s) and " + seconds + " second(s)");

    }


    private void loadNCIT() {

        log.info("Loading all Neoplasm subnodes.");

        Set<String> loadedTerms = new HashSet<>();
        Set<OntologyTerm> discoveredTerms = new HashSet<>();

        String diseaseRootLabel = "Cancer"; // Neoplasm

        int termCounter = 1;
        int requestCounter = 0;

        //create cancer root term
        OntologyTerm ot = dataImportService.getOntologyTerm(diseasesBranchUrl, diseaseRootLabel);
        ot.setType("diagnosis");
        log.debug("Creating node: " + diseaseRootLabel);

        discoveredTerms.add(ot);

        while (discoveredTerms.size() > 0) {
            //get term from notVisited

            OntologyTerm notYetVisitedTerm = discoveredTerms.iterator().next();
            discoveredTerms.remove(notYetVisitedTerm);

            if (loadedTerms.contains(notYetVisitedTerm.getUrl())) continue;

            loadedTerms.add(notYetVisitedTerm.getUrl());

            String parentUrlEncoded = "";
            try {
                //have to double encode the url to get the desired result
                parentUrlEncoded = URLEncoder.encode(notYetVisitedTerm.getUrl(), "UTF-8");
                parentUrlEncoded = URLEncoder.encode(parentUrlEncoded, "UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String url = ontologyUrl + parentUrlEncoded + "/hierarchicalChildren?size=200";

            log.debug("Getting data from " + url);

            String json = utilityService.parseURL(url);
            requestCounter++;

            if (requestCounter % 200 == 0) {
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

                    //do not load this branch
                    //if(termLabel.equals("Neoplasm by Special Category")) continue;

                    log.debug("TERM: " + termLabel);

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

                    for (int j = 0; j < synonyms.length(); j++) {
                        synonymsSet.add(synonyms.getString(j));
                    }

                    newTerm.setSynonyms(synonymsSet);
                    newTerm.setType("diagnosis");

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

        if (requestCounter % 200 != 0) {
            log.info("Terms loaded: " + requestCounter);
        }

    }


    private void loadNCITPreDef() {


        log.info("Loading predefined nodes from NCIT.");

        String currentLine;
        long currentLineCounter = 1;
        String[] rowData;

        Map<String, OntologyTerm> ncitLoaded = new HashMap<>();

        try {
            BufferedReader buf = new BufferedReader(new FileReader(ncitFile));

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

                    log.info("Creating " + label);
                }
                currentLineCounter++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        for (Map.Entry<String, OntologyTerm> entry : ncitLoaded.entrySet()) {
            String key = entry.getKey();
            OntologyTerm parentTerm = entry.getValue();


            String parentUrlEncoded = "";
            try {
                //have to double encode the url to get the desired result
                parentUrlEncoded = URLEncoder.encode(parentTerm.getUrl(), "UTF-8");
                parentUrlEncoded = URLEncoder.encode(parentUrlEncoded, "UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String url = ontologyUrl + parentUrlEncoded + "/hierarchicalChildren?size=100";

            log.debug("Getting data from " + url);

            String json = utilityService.parseURL(url);

            try {
                JSONObject job = new JSONObject(json);
                if (!job.has("_embedded")) continue;
                String embedded = job.getString("_embedded");

                //if this term does not have child nodes, continue

                JSONObject job2 = new JSONObject(embedded);
                JSONArray terms = job2.getJSONArray("terms");


                for (int i = 0; i < terms.length(); i++) {

                    JSONObject term = terms.getJSONObject(i);

                    String childTermUrl = term.getString("iri");

                    if (!ncitLoaded.containsKey(childTermUrl)) continue;

                    OntologyTerm childTerm = ncitLoaded.get(childTermUrl);

                    JSONArray synonyms = term.getJSONArray("synonyms");
                    Set<String> synonymsSet = new HashSet<>();

                    for (int j = 0; j < synonyms.length(); j++) {
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

/*
        //build relationships
        for(OntologyTerm parentTerm:ncitLoaded){


            String parentUrlEncoded = "";
            try {
                //have to double encode the url to get the desired result
                parentUrlEncoded = URLEncoder.encode(parentTerm.getUrl(), "UTF-8");
                parentUrlEncoded = URLEncoder.encode(parentUrlEncoded, "UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String url = ontologyUrl+parentUrlEncoded+"/hierarchicalChildren?size=100";

            System.out.println("Getting data from "+url);

            String json = parseURL(url);

            try {
                JSONObject job = new JSONObject(json);
                if (!job.has("_embedded")) continue;
                String embedded = job.getString("_embedded");

                //if this term does not have child nodes, continue

                JSONObject job2 = new JSONObject(embedded);
                JSONArray terms = job2.getJSONArray("terms");


                for (int i = 0; i < terms.length(); i++) {

                    JSONObject term = terms.getJSONObject(i);

                    OntologyTerm childTerm = loaderUtils.getOntologyTerm(term.getString("iri"));

                    if(childTerm == null) continue;

                    JSONArray synonyms = term.getJSONArray("synonyms");
                    Set<String> synonymsSet = new HashSet<>();

                    for(int j=0; j<synonyms.length();j++){
                        synonymsSet.add(synonyms.getString(j));
                    }

                    childTerm.setSynonyms(synonymsSet);

                    childTerm.addSubclass(parentTerm);
                    loaderUtils.saveOntologyTerm(childTerm);

                }

            } catch (Exception e) {
                log.error("", e);

            }


        }

        */


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