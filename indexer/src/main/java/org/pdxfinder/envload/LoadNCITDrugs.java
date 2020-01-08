package org.pdxfinder.envload;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.services.ontology.Ontolia;
import org.pdxfinder.utils.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(value = -65)
public class LoadNCITDrugs implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LoadNCITDrugs.class);

    //DRUGS_BRANCH_URL=http://purl.obolibrary.org/obo/NCIT_C1908
    //ONTOLOGY_URL=https://www.ebi.ac.uk/ols/api/ontologies/ncit/terms/

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
        parser.accepts(Option.loadNCITDrugs.get());
        parser.accepts(Option.loadALL.get());
        parser.accepts(Option.reloadCache.get());
        parser.accepts(Option.loadEssentials.get());
        OptionSet options = parser.parse(args);

        long startTime = System.currentTimeMillis();

        if (options.has(Option.reloadCache.get()) ||
                options.has(Option.loadALL.get())  ||
                options.has(Option.loadEssentials.get()) ||
                (options.has(Option.loadNCITDrugs.get())  || utilityService.getLoadCache())) {

                Ontolia ontolia = new Ontolia(utilityService, dataImportService);
                ontolia.run();

        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int seconds = (int) (totalTime / 1000) % 60;
        int minutes = (int) ((totalTime / (1000 * 60)) % 60);

        log.info(" finished after {} {} minute(s) and {} second(s)", this.getClass().getSimpleName(), minutes, seconds);

    }


}
