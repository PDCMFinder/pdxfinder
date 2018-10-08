package org.pdxfinder.commands;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;


/**
 * This will create a local copy of the data feeds.
 *
 * Directory structure:
 * dataRootDir = /abc/def/
 *
 * datasource1 = /abc/def/DATASOURCEABBREV1/pdx/models.json
 *
 * mutation for datasource 1 = /abc/def/DATASOURCEABBREV1/mut/
 * drug for datasource 1 = /abc/def/DATASOURCEABBREV1/drug/
 *
 */


/*
 * Created by csaba on 08/10/2018.
 */
@Component
@Order(value = 99)
public class CreateLocalFeeds implements CommandLineRunner {


    private final static Logger log = LoggerFactory.getLogger(CreateLocalFeeds.class);

    @Value("${pdxfinder.data.root.dir}")
    private String dataRootDir;

    //JAX
    @Value("${jaxpdx.url}")
    private String jaxURLStr;

    @Value("${jaxpdx.variation.url}")
    private String jaxVariationURL;

    @Value("${jaxpdx.histology.url}")
    private String histologyURL;




    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("createLocalFeeds", "Creating local feeds");

        OptionSet options = parser.parse(args);
        long startTime = System.currentTimeMillis();

        if (options.has("createLocalFeeds")) {

            createJAXFeeds();

        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int seconds = (int) (totalTime / 1000) % 60;
        int minutes = (int) ((totalTime / (1000 * 60)) % 60);

        log.info(this.getClass().getSimpleName() + " finished after " + minutes + " minute(s) and " + seconds + " second(s)");
    }


    private void createJAXFeeds(){

        String jsonString = parseURL(jaxURLStr);

        saveFile(dataRootDir+"JAX/pdx/models.json", jsonString);

        try {
            JSONObject job = new JSONObject(jsonString);
            JSONArray jarray = job.getJSONArray("pdxInfo");

            for (int i = 0; i < jarray.length(); i++) {

                JSONObject j = jarray.getJSONObject(i);
                String modelId = j.getString("Model ID");

                String mutation = parseURL(jaxVariationURL + modelId);
                saveFile(dataRootDir+"JAX/mut/"+modelId+".json", mutation);

            }

        } catch (Exception e) {
            log.error("Error getting JAX PDX models", e);

        }
    }










    private void saveFile(String fileName, String fileContent){

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false));

            writer.write(fileContent);
            writer.close();

        } catch (IOException e) {
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
