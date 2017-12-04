package org.pdxfinder.commands;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.dao.Sample;
import org.pdxfinder.ontologymapping.MappingRule;
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
import java.util.HashMap;
import java.util.Map;

/*
 * Created by csaba on 30/11/2017.
 */
@Component
@Order(value = 100)
public class ExportSamplesWithoutMapping implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(ExportSamplesWithoutMapping.class);
    private LoaderUtils loaderUtils;
    private Collection<Sample> samplesWithoutMapping;
    private Map<String, MappingRule> mappingRules;

    private static final String spreadsheetServiceUrl = "http://gsx2json.com/api?id=17LixNQL_BoL_-yev1s_VJt9FDZAJQcl5kyMhHkSF7Xk";

    @Autowired
    public ExportSamplesWithoutMapping(LoaderUtils loaderUtils) {
        this.loaderUtils = loaderUtils;
    }



    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("exportSamplesWithoutMapping", "Creates a csv file with samples without mappings");
        OptionSet options = parser.parse(args);

        if (options.has("exportSamplesWithoutMapping") ) {

            log.info("Exporting samples without mapping.");

            getSamplesWithoutMapping();
            getMappingRules();

            System.out.print("");

        }

    }


    public void getSamplesWithoutMapping(){

        this.samplesWithoutMapping = loaderUtils.getSamplesWithoutOntologyMapping();

    }


    public void getMappingRules(){

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

                    //DO not ask, I know it looks horrible...
                    if(originTissue == null || originTissue.equals("null")) originTissue = "";
                    if(tumorType == null || tumorType.equals("null")) tumorType = "";

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
