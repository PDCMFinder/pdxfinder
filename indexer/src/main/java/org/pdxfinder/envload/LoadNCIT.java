package org.pdxfinder.envload;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.graph.dao.OntologyTerm;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.utils.Option;
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

    private static final Logger log = LoggerFactory.getLogger(LoadNCIT.class);

    private static final String DISEASES_BRANCH_URL = "http://purl.obolibrary.org/obo/NCIT_C3262";
    private static final String ONTOLOGY_URL = "https://www.ebi.ac.uk/ols/api/ontologies/ncit/terms/";
    private static final String UTF8 = "UTF-8";
    private static final String EMBEDDED = "_embedded";


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
        parser.accepts(Option.loadNCIT.get());
        parser.accepts(Option.loadNCITPreDef.get());
        parser.accepts(Option.loadALL.get());
        parser.accepts(Option.reloadCache.get());
        parser.accepts(Option.loadSlim.get());
        parser.accepts(Option.loadEssentials.get());
        OptionSet options = parser.parse(args);

        long startTime = System.currentTimeMillis();

        if (options.has(Option.loadNCIT.get()) && options.has(Option.loadNCITPreDef.get())) {

            log.warn("Select one or the other of: -loadNCIT, -loadNCITPreDef");
            log.warn("Not loading {} ", this.getClass().getName());

        } else if (options.has(Option.loadNCIT.get()) ||
                options.has(Option.reloadCache.get()) ||
                options.has(Option.loadSlim.get()) ||
                options.has(Option.loadEssentials.get()) ||
                (options.has(Option.loadALL.get()) && dataImportService.countAllOntologyTerms() == 0)) {


            loadNCIT();
            utilityService.setLoadCache(true);

        } else if (options.has(Option.loadNCITPreDef.get())) {

            loadNCITPreDef();
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int seconds = (int) (totalTime / 1000) % 60;
        int minutes = (int) ((totalTime / (1000 * 60)) % 60);

        log.info("{} finished after {} minute(s) and {} second(s)", this.getClass().getSimpleName(), minutes, seconds);

    }


    private void loadNCIT() {

        log.info("Loading all Neoplasm subnodes.");

        Set<String> loadedTerms = new HashSet<>();
        Set<OntologyTerm> discoveredTerms = new HashSet<>();

        String diseaseRootLabel = "Cancer"; // Neoplasm

        int requestCounter = 0;

        //create cancer root term
        OntologyTerm ot = dataImportService.getOntologyTerm(DISEASES_BRANCH_URL, diseaseRootLabel);
        ot.setType("diagnosis");
        log.debug("Creating node: {}", diseaseRootLabel);

        discoveredTerms.add(ot);

        while (discoveredTerms.isEmpty()) {
            //get term from notVisited

            OntologyTerm notYetVisitedTerm = discoveredTerms.iterator().next();
            discoveredTerms.remove(notYetVisitedTerm);

            if (loadedTerms.contains(notYetVisitedTerm.getUrl())) continue;

            loadedTerms.add(notYetVisitedTerm.getUrl());

            String parentUrlEncoded = "";
            try {
                //have to double encode the url to get the desired result
                parentUrlEncoded = URLEncoder.encode(notYetVisitedTerm.getUrl(), UTF8);
                parentUrlEncoded = URLEncoder.encode(parentUrlEncoded, UTF8);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String url = ONTOLOGY_URL + parentUrlEncoded + "/hierarchicalChildren?size=200";

            log.debug("Getting data from {}", url);

            String json = utilityService.parseURL(url);
            requestCounter++;

            if (requestCounter % 200 == 0) {
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

                    for (int j = 0; j < synonyms.length(); j++) {
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
                log.error("", e);

            }

        }

        if (requestCounter % 200 != 0) {
            log.info("Terms loaded: {}", requestCounter);
        }

    }


    private void loadNCITPreDef() {


        log.info("Loading predefined nodes from NCIT.");

        String currentLine;
        long currentLineCounter = 1;
        String[] rowData;

        Map<String, OntologyTerm> ncitLoaded = new HashMap<>();

        try(BufferedReader buf = new BufferedReader(new FileReader(ncitFile));) {

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

                    log.info("Creating {}", label);
                }
                currentLineCounter++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        for (Map.Entry<String, OntologyTerm> entry : ncitLoaded.entrySet()) {

            OntologyTerm parentTerm = entry.getValue();


            String parentUrlEncoded = "";
            try {
                //have to double encode the url to get the desired result
                parentUrlEncoded = URLEncoder.encode(parentTerm.getUrl(), UTF8);
                parentUrlEncoded = URLEncoder.encode(parentUrlEncoded, UTF8);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String url = ONTOLOGY_URL + parentUrlEncoded + "/hierarchicalChildren?size=100";

            log.debug("Getting data from {}", url);

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

    }


}