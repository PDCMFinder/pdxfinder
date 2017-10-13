package org.pdxfinder.commands;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.dao.OntologyTerm;
import org.pdxfinder.dao.Sample;
import org.pdxfinder.dao.SampleToDiseaseOntologyRelationship;
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by csaba on 24/08/2017.
 */
@Component
@Order(value = 100)
public class LinkSamplesToNCITTerms implements CommandLineRunner{


    private static final String spreadsheetServiceUrl = "http://gsx2json.com/api?id=16JhGWCEUimsOF8q8bYN7wEJqVtjbO259X1YGrbRQLdc";
    //https://docs.google.com/spreadsheets/d/16JhGWCEUimsOF8q8bYN7wEJqVtjbO259X1YGrbRQLdc/edit
    private final static Logger log = LoggerFactory.getLogger(LinkSamplesToNCITTerms.class);
    private LoaderUtils loaderUtils;

    @Autowired
    public LinkSamplesToNCITTerms(LoaderUtils loaderUtils) {
        this.loaderUtils = loaderUtils;
    }

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadAccessionIds", "Link samples to NCIT terms");
        parser.accepts("loadALL", "Load all, including linking samples to NCIT terms");
        OptionSet options = parser.parse(args);

        long startTime = System.currentTimeMillis();

        if (options.has("linkSamplesToNCITTerms") && options.has("linkSamplesToNCITTermsWithCleanup")) {
            log.warn("Select one or the other of: -linkSamplesToNCITTerms, -linkSamplesToNCITTermsWithCleanup");
            log.warn("Not loading ", this.getClass().getName());

        } else if (options.has("linkSamplesToNCITTermsWithCleanup") || options.has("loadALL")) {

            log.info("Mapping samples to NCIT terms.");

            mapSamplesToTerms();
            updateIndirectMappingData();
            deleteTermsWithoutMapping();

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


    private void mapSamplesToTerms(){

        System.out.println("Getting data from "+spreadsheetServiceUrl);

        String json = parseURL(spreadsheetServiceUrl);

        try {
            JSONObject job = new JSONObject(json);
            if (job.has("rows")){
                JSONArray rows = job.getJSONArray("rows");

                int errorCounter = 0;
                int mapCounter = 0;

                for(int i=0;i<rows.length();i++){
                    JSONObject row = rows.getJSONObject(i);
                    String sampleId = row.getString("sampleid");
                    String label = row.getString("ncitlabel");
                    String type = row.getString("type");
                    String justification = row.getString("justification");
                    String dataSource = row.getString("datasource");

                    Sample sample = loaderUtils.getHumanSample(sampleId, dataSource);
                    OntologyTerm term = loaderUtils.getOntologyTermByLabel(label.toLowerCase());

                    if(sample != null && term != null){

                        SampleToDiseaseOntologyRelationship r = new SampleToDiseaseOntologyRelationship(sample, term, type, justification);
                        sample.setSampleToDiseaseOntologyRelationship(r);
                        term.setMappedTo(r);

                        term.setDirectMappedSamplesNumber(term.getDirectMappedSamplesNumber() + 1);

                        loaderUtils.saveSample(sample);
                        loaderUtils.saveOntologyTerm(term);
                        mapCounter++;
                        System.out.println("DONE "+sampleId+" "+label);
                    }
                    else{
                        errorCounter++;
                        System.out.println("ERROR "+sampleId+" "+label);
                    }

                }

                System.out.println("Links created: "+mapCounter);
                System.out.println("Mapping errors: "+errorCounter);
            }


        } catch (JSONException e) {
            e.printStackTrace();
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

/*
    private void updateIndirectMappingData() {

        log.info("Getting all terms to update indirect mapping numbers");

        int maxTermNumber = loaderUtils.getOntologyTermNumber();
        int batchSize = 50;
        int startNode = 0;

        while(startNode<maxTermNumber){
            int to = startNode+batchSize;
            log.info("Loading terms from "+startNode+" to "+to);

            Collection<OntologyTerm> terms = loaderUtils.getAllOntologyTermsFromTo(startNode,batchSize);

            for (OntologyTerm ot : terms) {
                System.out.println("Updating " + ot.getLabel());
                ot.setIndirectMappedSamplesNumber(loaderUtils.getDirectMappingNumber(ot.getLabel()));
                loaderUtils.saveOntologyTerm(ot);

            }

            startNode+=batchSize;

        }
    }
*/


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
