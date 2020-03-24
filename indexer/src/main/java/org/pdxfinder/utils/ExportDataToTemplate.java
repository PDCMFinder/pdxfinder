package org.pdxfinder.utils;

import org.pdxfinder.dataexport.UniversalDataExporter;
import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class ExportDataToTemplate  {

    private static final Logger log = LoggerFactory.getLogger(ExportDataToTemplate.class);
    private UtilityService utilityService;
    private DataImportService dataImportService;

    @Autowired
    ExportDataToTemplate(UtilityService utilityService, DataImportService dataImportService){
        this.utilityService = utilityService;
        this.dataImportService = dataImportService;
    }

    public void exportAllGroups(File rootDir){
        List<Group> allProviders = dataImportService.getAllProviderGroups();
        allProviders.forEach(g -> {
            try {
                export(rootDir, g.getAbbreviation());
            } catch (IOException e) {
                log.error(Arrays.toString(e.getStackTrace()));
            }
        });
    }

    public void export(File rootDir, String dataSourceAbbrev) throws IOException {
        Group ds = dataImportService.findProviderGroupByAbbrev(dataSourceAbbrev);
        if(ds == null) {
            log.error("Datasource {} not found. ",dataSourceAbbrev);
            return;
        }
        UniversalDataExporter downDog = new UniversalDataExporter(dataImportService, utilityService);
        downDog.init(rootDir + "/template", ds);
        downDog.export(rootDir + "/export");
    }
}
