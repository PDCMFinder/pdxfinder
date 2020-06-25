package org.pdxfinder.services.loader.envload;

import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.services.ontology.Ontolia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoadNCITDrugs {


    private static final Logger log = LoggerFactory.getLogger(LoadNCITDrugs.class);

    private DataImportService dataImportService;
    private UtilityService utilityService;

    public LoadNCITDrugs(DataImportService dataImportService, UtilityService utilityService) {
        this.dataImportService = dataImportService;
        this.utilityService = utilityService;
    }

    public void loadRegimens(){

        long startTime = System.currentTimeMillis();

        Ontolia ontolia = new Ontolia(utilityService, dataImportService);
        ontolia.run();

        long totalTime = System.currentTimeMillis() - startTime;

        int seconds = (int) (totalTime / 1000) % 60;
        int minutes = (int) ((totalTime / (1000 * 60)) % 60);

        log.info(" finished after {} {} minute(s) and {} second(s)", this.getClass().getSimpleName(), minutes, seconds);
    }

}
