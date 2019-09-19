package org.pdxfinder.utils;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.pdxfinder.services.TransformerService;
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
    private TransformerService transformerService;

    @Value("${pdxfinder.data.root.dir}")
    private String dataRootDir;



    @Autowired
    public TransformPDMR(TransformerService transformerService){
        this.transformerService = transformerService;
    }


    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("transformPDMR", "Transform and Map PDMR data to PdxFinder Format");
        OptionSet options = parser.parse(args);

        if (options.has("transformPDMR")) {

            log.info("Loading PDMR PDX data.");

            if (dataRootDir != null) {
                log.info("Loading from URL " + dataRootDir);

                transformerService.transformDataAndSave();
            } else {
                log.error("No mydatasource.url1 or mydatasource.url2 provided in properties");
            }
        }
        else{
            log.info("No data Loading or transformation at the moment, the tomcat server is running for browser based access, to Load data use program argument: -transformPDMR");
        }
    }



}


