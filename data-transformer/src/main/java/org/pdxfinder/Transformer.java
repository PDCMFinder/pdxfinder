package org.pdxfinder;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Transformer {

    private final static Logger log = LoggerFactory.getLogger(Transformer.class);

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            Options options = new Options();
            HelpFormatter formatter = new HelpFormatter();

            Option tranOpt = Option.builder("transformPDMR").desc("Transforms and Maps PDMR data into PdxFinder's Schema Structure.").build();

            options.addOption(tranOpt);

            formatter.printHelp("Application", options);

            SpringApplication.run(Transformer.class, args);

        } else {
            SpringApplication.run(Transformer.class, args);
        }
    }

}