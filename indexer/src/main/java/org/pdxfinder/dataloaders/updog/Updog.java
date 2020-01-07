package org.pdxfinder.dataloaders.updog;

import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tech.tablesaw.api.Table;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class Updog {

    private String provider;
    private String updogDir;
    private UtilityService utilityService;
    private DataImportService dataImportService;

    @Autowired
    public Updog(DataImportService dataImportService, UtilityService utilityService) {
        this.dataImportService = dataImportService;
        this.utilityService = utilityService;
    }

    private static final Logger log = LoggerFactory.getLogger(Updog.class);

    private Map<String, Table> pdxDataTables;
    private Map<String, Object> domainObjects;

    public void run() {
        readPdxDataForProvider();
        validatePdxDataTables();
        load();
    }

    private void readPdxDataForProvider() {

        Path path = Paths.get(updogDir, provider);
        TemplateReader templateReader = new TemplateReader();
        templateReader.setTargetDirectory(path);

        pdxDataTables = templateReader.read();
    }

    public boolean validatePdxDataTables(){

        //instantiate a validator class
        TemplateValidator templateValidator = new TemplateValidator();
        return true;
    }


    public void load(){

        //create domain objects database nodes
        DomainObjectCreator doc = new DomainObjectCreator(dataImportService, utilityService, pdxDataTables);
        //save db


    }


    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getUpdogDir() {
        return updogDir;
    }

    public void setUpdogDir(String updogDir) {
        this.updogDir = updogDir;
    }
}
