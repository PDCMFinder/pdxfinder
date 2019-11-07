package org.pdxfinder.utils;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.pdxfinder.dataexport.UniversalDataExporter;
import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/*
 * Created by csaba on 02/10/2019.
 */
@Component
@Order(value = 0)
public class ExportDataToTemplate implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(ExportDataToTemplate.class);


    @Autowired
    private UtilityService utilityService;

    @Autowired
    private DataImportService dataImportService;

    @Value("${pdxfinder.root.dir}")
    private String finderRootDir;

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("exportToTemplate", "Exporting data in template format");

        OptionSet options = parser.parse(args);
        long startTime = System.currentTimeMillis();

        if (options.has("exportToTemplate")) {


            export();

        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int seconds = (int) (totalTime / 1000) % 60;
        int minutes = (int) ((totalTime / (1000 * 60)) % 60);

        log.info(this.getClass().getSimpleName() + " finished after " + minutes + " minute(s) and " + seconds + " second(s)");
    }




    private void export(){


        Group traceDS = dataImportService.findProviderGroupByAbbrev("JAX");

        UniversalDataExporter downDog = new UniversalDataExporter(dataImportService, utilityService);
        downDog.setTemplateDir(finderRootDir + "/template");

        downDog.setDs(traceDS);
        downDog.export(finderRootDir + "/export");


    }

}
