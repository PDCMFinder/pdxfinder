package org.pdxfinder.utils;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.neo4j.unsafe.impl.batchimport.input.Groups;
import org.pdxfinder.dataexport.UniversalDataExporter;
import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;


/*
 * Created by csaba on 02/10/2019.
 */
@Component
public class ExportDataToTemplate implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ExportDataToTemplate.class);

    static final private String exportTemplate = "exportToTemplate";
    static final private String exportAll = "exportAll";

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
        parser.accepts("exportAll", "Export all providers to templates");

        OptionSet options = parser.parse(args);
        long startTime = System.currentTimeMillis();

        if (options.has(exportTemplate) && args.length > 1) {

            log.info("Exporting data from {}", args[1]);
            export(args[1]);

        }
        else if(options.has(exportTemplate) && args.length <= 1){
            log.error("Missing provider abbrev. Cannot export data.");

        } else if(options.has(exportAll))
            log.info("Exporting All datasets");
            exportAllGroups();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int seconds = (int) (totalTime / 1000) % 60;
        int minutes = (int) ((totalTime / (1000 * 60)) % 60);

        log.info("{} finished after {} minutes and {} second(s)",this.getClass().getSimpleName(), minutes, seconds);
    }

    private void exportAllGroups(){

        List<Group> allProviders = dataImportService.getAllProviderGroups();
        allProviders.forEach(g -> export(g.getAbbreviation()));
    }


    private void export(String dataSourceAbbrev){


        Group ds = dataImportService.findProviderGroupByAbbrev(dataSourceAbbrev);

        if(ds == null) {
            log.error("Datasource {} not found. ",dataSourceAbbrev);
            return;
        }

        UniversalDataExporter downDog = new UniversalDataExporter(dataImportService, utilityService);

        downDog.init(finderRootDir + "/template", ds);
        downDog.export(finderRootDir + "/export");


    }

}
