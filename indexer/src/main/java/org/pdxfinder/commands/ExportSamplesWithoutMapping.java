package org.pdxfinder.commands;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.pdxfinder.utilities.LoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

/*
 * Created by csaba on 30/11/2017.
 */
public class ExportSamplesWithoutMapping implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(ExportSamplesWithoutMapping.class);
    private LoaderUtils loaderUtils;

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


        }

    }


}
