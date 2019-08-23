package org.pdxfinder.commands.envload;

/**
 * Created by csaba on 07/05/2019.
 */

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


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

@Component
@Order(value = -65)
public class LoadNCITDrugs implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadNCIT.class);

    private static final String drugsBranchUrl = "http://purl.obolibrary.org/obo/NCIT_C1908";
    private static final String regimenBranchUrl = "http://purl.obolibrary.org/obo/NCIT_C12218";

    private static final String ontologyUrl = "https://www.ebi.ac.uk/ols/api/ontologies/ncit/terms/";

    private DataImportService dataImportService;

    @Autowired
    private UtilityService utilityService;

    @Autowired
    public LoadNCITDrugs(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }


    private List<String> unlinkedRegimens;
    private List<String> linkedRegimens;
    private Map<String, OntologyTerm> loadedTreatmentTerms;



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

            loadedTreatmentTerms = new HashMap<>();

            log.info("Loading all Drugs from NCIT.");
            loadNCITLeafDrugs("treatment", drugsBranchUrl);

            log.info("Loading all regimens from NCIT.");
            loadNCITLeafDrugs("treatment regimen", regimenBranchUrl);

            log.info("Linking drug regimens to individual drugs");
            linkRegimens();


        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int seconds = (int) (totalTime / 1000) % 60;
        int minutes = (int) ((totalTime / (1000 * 60)) % 60);

        log.info(this.getClass().getSimpleName() + " finished after " + minutes + " minute(s) and " + seconds + " second(s)");

    }





    private void linkRegimens() {

        unlinkedRegimens = new ArrayList<>();
        linkedRegimens = new ArrayList<>();

        int regimenNumber = dataImportService.getOntologyTermNumberByType("treatment regimen");
        int batch = 100;

        int i;
        for (i = 0; i < regimenNumber; i += batch) {

            Collection<OntologyTerm> regimens = dataImportService.getAllOntologyTermsByTypeFromTo("treatment regimen", i, batch);

            for(OntologyTerm regimen : regimens){

                linkRegimenToTreatments(regimen);
            }

            log.info("Linked regimens: "+Integer.toString(i + batch));
        }


        log.info("Linked regimens: "+linkedRegimens.size());
        log.info("Unlinked regimens: "+unlinkedRegimens.size());
        log.info(unlinkedRegimens.get(0));
        log.info(unlinkedRegimens.get(1));
        log.info(unlinkedRegimens.get(2));
        log.info(unlinkedRegimens.get(3));

    }

    private void linkRegimenToTreatments(OntologyTerm regimen){


        Set<String> synonyms = regimen.getSynonyms();

        boolean linked = false;
        List<OntologyTerm> slashMatrix = new ArrayList<>();

        for(String synonym : synonyms){

            String[] synSlash = synonym.split("/");
            String[] synDash = synonym.split("-");
            String[] synComma = synonym.split(",");
            String[] synSemicolon = synonym.split(";");


            boolean[] dashMatrix = new boolean[synDash.length];
            boolean[] commaMatrix = new boolean[synComma.length];
            boolean[] semicolonMatrix = new boolean[synSemicolon.length];

            for(int i=0; i < synSlash.length; i++){

                String label = synSlash[i].toLowerCase().replace("regimen", "").trim();

                if(loadedTreatmentTerms.containsKey(label)){

                    slashMatrix.add(loadedTreatmentTerms.get(label));
                    //log.info(regimen.getLabel() + " label found: "+label);
                }
                else{

                    slashMatrix.add(null);
                }
            }

            if(isAllNotNull(slashMatrix)) {
                linked = true;
                break;
            }

        }

        if(linked){

            linkedRegimens.add(regimen.getLabel());
        }
        else{
            unlinkedRegimens.add(regimen.getLabel());
        }
    }




    private void loadNCITLeafDrugs(String type, String branchRootUrl){

        int totalDrugs = 0;
        int totalPages = 0;
        boolean totalPagesDetermined = false;


        for(int currentPage = 0; currentPage<= totalPages; currentPage++){

            String encodedTermUrl = "";
            try {
                //have to double encode the url to get the desired result
                encodedTermUrl = URLEncoder.encode(branchRootUrl, "UTF-8");
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

                        OntologyTerm ot = new OntologyTerm();
                        ot.setType(type);
                        ot.setLabel(term.getString("label"));
                        ot.setUrl(term.getString("iri"));

                        if(term.has("synonyms")) {
                            JSONArray synonyms = term.getJSONArray("synonyms");
                            Set<String> synonymsSet = new HashSet<>();

                            for (int i = 0; i < synonyms.length(); i++) {
                                synonymsSet.add(synonyms.getString(i));
                            }

                            ot.setSynonyms(synonymsSet);
                        }
                        if(term.has("description")){
                            ot.setDescription(term.getString("description"));
                        }

                        ot.setAllowAsSuggestion(false);

                        if(type.equals("treatment")) loadedTreatmentTerms.put(ot.getLabel().toLowerCase(), ot);
                        dataImportService.saveOntologyTerm(ot);

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



    private boolean isAllNotNull(List<OntologyTerm> list){

        for(OntologyTerm ot : list){
            if(ot == null) return false;
        }
        return true;
    }

}
