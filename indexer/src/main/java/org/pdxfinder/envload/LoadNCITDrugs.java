package org.pdxfinder.envload;

/**
 * Created by csaba on 07/05/2019.
 */

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.services.ontology.Ontolia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(value = -65)
public class LoadNCITDrugs implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadNCIT.class);

    private static final String drugsBranchUrl = "http://purl.obolibrary.org/obo/NCIT_C1908";
    private static final String ontologyUrl = "https://www.ebi.ac.uk/ols/api/ontologies/ncit/terms/";

    private DataImportService dataImportService;

    private UtilityService utilityService;

    public LoadNCITDrugs(DataImportService dataImportService, UtilityService utilityService) {
        this.dataImportService = dataImportService;
        this.utilityService = utilityService;
    }

    @Override
    public void run(String... args) throws Exception {

        //http://www.ebi.ac.uk/ols/api/ontologies/ncit/terms/http%253A%252F%252Fpurl.obolibrary.org%252Fobo%252FNCIT_C7057/hierarchicalChildren
        //http://www.ebi.ac.uk/ols/api/ontologies/doid/terms/http%253A%252F%252Fpurl.obolibrary.org%252Fobo%252FNCIT_C7057/hierarchicalChildren

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadNCITDrugs", "Load NCIT drugs");
        parser.accepts("loadALL", "Load all, including NCiT drug ontology");
        parser.accepts("reloadCache", "Catches Markers and Ontologies");
        parser.accepts("loadEssentials", "Loading essentials");
        OptionSet options = parser.parse(args);

        long startTime = System.currentTimeMillis();

        if (options.has("reloadCache") || options.has("loadALL")  || options.has("loadEssentials")) {

            if ( options.has("loadNCITDrugs")  || utilityService.getShouldLoad() == true){

                Ontolia ontolia = new Ontolia(utilityService, dataImportService);
                ontolia.run();
            }

        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int seconds = (int) (totalTime / 1000) % 60;
        int minutes = (int) ((totalTime / (1000 * 60)) % 60);

        log.info(this.getClass().getSimpleName() + " finished after " + minutes + " minute(s) and " + seconds + " second(s)");

    }


}
