package org.pdxfinder.commands.postload;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.pdxfinder.graph.dao.ModelCreation;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/*
 * Created by csaba on 23/03/2019.
 */

@Component
public class CheckPDXPages implements CommandLineRunner{

    private final static Logger log = LoggerFactory.getLogger(CheckPDXPages.class);

    private String errorHtml = "err_body";
    private String successHtml = "level2";


    @Autowired
    DataImportService dataImportService;

    @Autowired
    UtilityService utilityService;

    @Override
    public void run(String... args) throws Exception {


        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("checkPDXPages", "Testing PDX details page urls");
        OptionSet options = parser.parse(args);


        if (options.has("checkPDXPages")) {

            testDetailsPageUrls();
        }

    }


    private void testDetailsPageUrls(){

        Collection<ModelCreation> models = dataImportService.findAllModels();

        String html = "";
        for(ModelCreation model : models){

            long startTime = System.currentTimeMillis();
            String url = "http://localhost:8080/data/pdx/"+model.getDataSource()+"/"+model.getSourcePdxId();

            html = parseURL(url);
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            checkPageContent(html, url, Long.toString(totalTime));

        }

        //log.info(html);

    }


    private void checkPageContent(String html, String url, String responseTime){

        if(html == null || html.isEmpty() || html.contains(errorHtml)){
            log.error("Page is not available "+url);
        }
        else if(html.contains(successHtml)){
            log.info("Details page is up: "+url+" Response time: "+responseTime+"ms");
        }
        else{
            log.warn("What has just happened??? "+url);
        }
    }



    public String parseURL(String urlStr) {

        StringBuilder sb = new StringBuilder();

        try {
            URL url = new URL(urlStr);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            return "";
        }
        return sb.toString();
    }
}
