package org.pdxfinder.utils;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import com.github.openjson.*;
import org.pdxfinder.services.TransformerService;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;


/**
 * This will create a local copy of the data feeds.
 *
 * Directory structure:
 * finderRootDir = /abc/def/
 *
 * datasource1 = /abc/def/DATASOURCEABBREV1/pdx/models.json
 *
 * mutation for datasource 1 = /abc/def/DATASOURCEABBREV1/mut/
 * drug for datasource 1 = /abc/def/DATASOURCEABBREV1/drug/
 * immunohistochemistry for ds 1 = /abc/def/DATASOURCEABBREV1/ihc/
 *
 *
 * MAKE SURE THE DEPLOYMENT SCRIPT COPIES OVER ADDITIONAL DATA FILES
 */


/*
 * Created by csaba on 08/10/2018.
 */
@Component
@PropertySource("classpath:datafeed.properties")
@Order(value = 0)
public class CreateLocalFeeds implements CommandLineRunner {


    private final static Logger log = LoggerFactory.getLogger(CreateLocalFeeds.class);

    @Value("${data-dir}")
    private String finderRootDir;

    //JAX
    @Value("${jaxpdx.url}")
    private String jaxUrlStr;

    @Value("${jaxpdx.variation.url}")
    private String jaxVariationURL;

    @Value("${jaxpdx.histology.url}")
    private String jaxHistologyURL;

    @Value("${jaxpdx.cna.url}")
    private String jaxCnaURL;

    @Value("${jaxpdx.rnaseq.url}")
    private String jaxRnaseqURL;

    //IRCC-CRC
    @Value("${irccpdx.url}")
    private String irccUrlStr;

    @Value("${irccpdx.variation.url}")
    private String irccVariationURLStr;


    //HCI
    @Value("${hcipdx.url}")
    private String hciUrlStr;

    @Value("${hci.ihc.file}")
    private String hciIhcFileStr;


    //MDA
    @Value("#{'${mda.urls}'.split(',')}")
    private List<String> mdaUrlsStr;

    //WISTAR
    @Value("${wistarpdx.url}")
    private String wistarUrlStr;


    @Value("#{'${wustl.urls}'.split(',')}")
    private List<String> wustlUrlsStr;

    @Autowired
    private UtilityService utilityService;

    @Autowired
    private TransformerService dataTransformer;

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("localFeeds", "Creating local feeds");

        OptionSet options = parser.parse(args);
        long startTime = System.currentTimeMillis();

        if (options.has("localFeeds")) {
            log.info("Creating local feeds");

            createJAXFeeds();

        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int seconds = (int) (totalTime / 1000) % 60;
        int minutes = (int) ((totalTime / (1000 * 60)) % 60);

        log.info(this.getClass().getSimpleName() + " finished after " + minutes + " minute(s) and " + seconds + " second(s)");
    }


    private void createJAXFeeds(){

        log.info("Creating JAX feeds");
        String jsonString = utilityService.parseURL(jaxUrlStr);

        saveFile(finderRootDir +"/data/JAX/pdx/", "models.json", jsonString);

        try {
            JSONObject job = new JSONObject(jsonString);
            JSONArray jarray = job.getJSONArray("pdxInfo");

            for (int i = 0; i < jarray.length(); i++) {

                JSONObject j = jarray.getJSONObject(i);
                String modelId = j.getString("Model ID");

                String mutation = utilityService.parseURL(jaxVariationURL + modelId);
                saveFile(finderRootDir +"/data/JAX/mut/", modelId+".json", mutation);

                String histology = utilityService.parseURL(jaxHistologyURL + modelId);
                saveFile(finderRootDir +"/data/JAX/hist/", modelId+".json", histology);

                String cna = utilityService.parseURL(jaxCnaURL + modelId);
                cna = dataTransformer.transformJAXCNV(cna);
                saveFile(finderRootDir +"/data/JAX/cna/", modelId+".json", cna);

                String trans = utilityService.parseURL(jaxRnaseqURL + modelId);
                trans = dataTransformer.transformJaxRNASeq(trans);
                saveFile(finderRootDir +"/data/JAX/trans/", modelId+".json", trans);

            }

        } catch (Exception e) {
            log.error("Error getting JAX PDX models", e);

        }
    }



    private void createIRCCFeeds(){

        log.info("Creating IRCC-CRC feeds");
        String jsonString = utilityService.parseURL(irccUrlStr);

        saveFile(finderRootDir +"/data/IRCC-CRC/pdx/", "models.json", jsonString);

        String mutation = utilityService.parseURL(irccVariationURLStr);
        saveFile(finderRootDir +"/data/IRCC-CRC/mut/", "data.json", mutation);

    }


    private void createHCIFeeds(){

        log.info("Creating HCI feeds");
        String jsonString = utilityService.parseURL(hciUrlStr);

        saveFile(finderRootDir +"/data/PDXNet-HCI-BCM/pdx/", "models.json", jsonString);

        //String ihc = parseURL(hciIhcFileStr);
        //saveFile(finderRootDir+"PDXNet-HCI-BCM/ihc/", "data.json", ihc);

    }

    private void createMDAFeeds(){

        log.info("Creating MDA feeds");

        int counter = 1;
        String fileName;
        for(String url : mdaUrlsStr){

            String jsonString = utilityService.parseURL(url);
            if(counter == 1){

                fileName = "models.json";
            }
            else{
                fileName = "models"+counter+".json";
            }

            saveFile(finderRootDir +"/data/PDXNet-MDAnderson/pdx/", fileName, jsonString);
            counter++;
        }

    }

    private void createWistarFeeds(){

        log.info("Creating WISTAR feeds");
        String jsonString = utilityService.parseURL(wistarUrlStr);

        saveFile(finderRootDir +"/data/PDXNet-Wistar-MDAnderson-Penn/pdx/", "models.json", jsonString);

    }

    private void createWustlFeeds(){
        log.info("Creating WUSTL feeds");
        int counter = 1;
        String fileName;
        for(String url : wustlUrlsStr){

            String jsonString = utilityService.parseURL(url);
            if(counter == 1){

                fileName = "models.json";
            }
            else{
                fileName = "models"+counter+".json";
            }

            saveFile(finderRootDir +"/data/PDXNet-WUSTL/pdx/", fileName, jsonString);
            counter++;
        }

    }

    private void saveFile(String dirPath, String fileName, String fileContent){

        File directory = new File(dirPath);
        if (! directory.exists()){
            directory.mkdirs();

        }

        String fileWithPath = dirPath+fileName;

        log.info("Saving file "+fileWithPath);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileWithPath, false));

            writer.write(fileContent);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
