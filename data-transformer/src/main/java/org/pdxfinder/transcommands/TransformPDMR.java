package org.pdxfinder.transcommands;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TransformPDMR implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(TransformPDMR.class);

    private Options options;
    private CommandLineParser parser;
    private DataTransformerService dataTransformerService;

    @Value("${mydatasource.url1}")
    private String url1;

    @Value("${mydatasource.url2}")
    private String url2;

    @Autowired
    public TransformPDMR(DataTransformerService dataTransformerService){
        this.dataTransformerService = dataTransformerService;
    }



    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("transformPDMR", "Transform and Map PDMR data to PdxFinder Format");
        OptionSet options = parser.parse(args);

        if (options.has("transformPDMR") ) {

            log.info("Loading PDMR PDX data.");

            if (url1 != null && url2 != null) {
                log.info("Loading from URL " + url1);
                log.info("Loading from URL " + url2);

                dataTransformerService.transformDataAndSave(url1,url2);
            }
            else {
                log.error("No mydatasource.url1 or mydatasource.url2 provided in properties");
            }
        }
        else{
            log.info("No data Loading or transformation at the moment, the tomcat server is running for browser based access, to Load data use program argument: -transformPDMR");
        }
    }



}


