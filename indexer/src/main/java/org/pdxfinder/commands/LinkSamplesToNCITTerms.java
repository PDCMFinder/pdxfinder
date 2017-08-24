package org.pdxfinder.commands;

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
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;

/**
 * Created by csaba on 24/08/2017.
 */
@Component
@Order(value = Ordered.LOWEST_PRECEDENCE)
public class LinkSamplesToNCITTerms implements CommandLineRunner{


    private static final String spreadsheetServiceUrl = "http://gsx2json.com/api?id=16JhGWCEUimsOF8q8bYN7wEJqVtjbO259X1YGrbRQLdc";

    private final static Logger log = LoggerFactory.getLogger(LinkSamplesToNCITTerms.class);
    private LoaderUtils loaderUtils;

    @Autowired
    public LinkSamplesToNCITTerms(LoaderUtils loaderUtils) {
        this.loaderUtils = loaderUtils;
    }

    @Override
    public void run(String... args) throws Exception {

        log.info(args[0]);

        if ("linkSamplesToNCITTerms".equals(args[0]) || "-linkSamplesToNCITTerms".equals(args[0])) {

            log.info("Mapping samples to NCIT terms.");
            long startTime = System.currentTimeMillis();
            mapSamplesToTerms();
            updateIndirectMappingData();
            long endTime   = System.currentTimeMillis();
            long totalTime = endTime - startTime;

            int seconds = (int) (totalTime / 1000) % 60 ;
            int minutes = (int) ((totalTime / (1000*60)) % 60);

            System.out.println("Mapping finished after "+minutes+" minute(s) and "+seconds+" second(s)");
        }
        else{
            log.info("Not running linkSamplesToNCITTerms command");
        }

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

                    Sample sample = loaderUtils.getSampleBySourceSampleId(sampleId);
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
