package org.pdxfinder.commands;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.dao.OntologyTerm;
import org.pdxfinder.dao.Sample;
import org.pdxfinder.dao.SampleToOntologyRelationShip;
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

/**
 * Created by csaba on 19/06/2017.
 */
@Component
@Order(value = 100)
public class LinkSamplesToDOTerms implements CommandLineRunner{


    private static final String googleSpreadsheethUrl = "https://docs.google.com/spreadsheets/d/1TpGeClk6bY-sJ_5Ffs0Rbl6Rfonc9SMzKERCY4KLnes/pubhtml";
    private static final String spreadsheetServiceUrl = "http://gsx2json.com/api?id=1TpGeClk6bY-sJ_5Ffs0Rbl6Rfonc9SMzKERCY4KLnes";

    private final static Logger log = LoggerFactory.getLogger(LinkSamplesToDOTerms.class);
    private LoaderUtils loaderUtils;

    @Autowired
    public LinkSamplesToDOTerms(LoaderUtils loaderUtils) {
        this.loaderUtils = loaderUtils;
    }

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("linkSamplesToDOTerms", "Link samples to DO terms");
        OptionSet options = parser.parse(args);

        long startTime = System.currentTimeMillis();

        if (options.has("linkSamplesToDOTerms")) {

            log.info("Mapping samples to DO terms.");
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
                    String doLabel = row.getString("dolabel");
                    String type = row.getString("type");
                    String justification = row.getString("justification");

                    Sample sample = loaderUtils.getSampleBySourceSampleId(sampleId);
                    OntologyTerm term = loaderUtils.getOntologyTermByLabel(doLabel.toLowerCase());

                    if(sample != null && term != null){

                        SampleToOntologyRelationShip r = new SampleToOntologyRelationShip(type, justification, sample, term);
                        sample.setSampleToOntologyRelationShip(r);
                        term.setMappedTo(r);

                        term.setDirectMappedSamplesNumber(term.getDirectMappedSamplesNumber() + 1);

                        loaderUtils.saveSample(sample);
                        loaderUtils.saveOntologyTerm(term);
                        mapCounter++;
                        //System.out.println("DONE "+sampleId+" "+doLabel);
                    }
                    else{
                        errorCounter++;
                        System.out.println("ERROR "+sampleId+" "+doLabel);
                    }

                }

                System.out.println("Links created: "+mapCounter);
                System.out.println("Mapping errors: "+errorCounter);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void updateIndirectMappingData() {

        Collection<OntologyTerm> terms = loaderUtils.getAllOntologyTerms();

        for (OntologyTerm ot : terms) {
            System.out.println("Updating " + ot.getLabel());
            ot.setIndirectMappedSamplesNumber(loaderUtils.getDirectMappingNumber(ot.getLabel()));
            loaderUtils.saveOntologyTerm(ot);

        }


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
