package org.pdxfinder.dataloaders.updog;

import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import tech.tablesaw.api.Table;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Component
public class Updog {

    private UtilityService utilityService;
    private DataImportService dataImportService;
    private MetadataReader metadataReader;
    private MetadataValidator metadataValidator;
    private Map<String, Table> pdxDataTables;

    @Autowired
    public Updog(
            DataImportService dataImportService,
            UtilityService utilityService,
            MetadataReader metadataReader,
            MetadataValidator metadataValidator) {

        Assert.notNull(dataImportService, "dataImportService cannot be null");
        Assert.notNull(utilityService, "utilityService cannot be null");
        Assert.notNull(metadataReader, "metadataReader cannot be null");
        Assert.notNull(metadataValidator, "templateValidator cannot be null");

        this.dataImportService = dataImportService;
        this.utilityService = utilityService;
        this.metadataReader = metadataReader;
        this.metadataValidator = metadataValidator;
        this.pdxDataTables = new HashMap<>();
    }


    private static final Logger log = LoggerFactory.getLogger(Updog.class);


    public void run(Path updogDir) {
        log.debug("Running UPDOG on [{}]", updogDir);
        pdxDataTables = readPdxDataFromPath(updogDir);
        validatePdxDataTables(pdxDataTables);
        createPdxObjects();
    }

    private Map<String, Table> readPdxDataFromPath(Path updogDir) {
        return metadataReader.readMetadataTsvs(updogDir);
    }

    public boolean validatePdxDataTables(Map<String, Table> pdxDataTables){
        return metadataValidator.passesValidation(pdxDataTables);
    }


    public void createPdxObjects(){

        //create domain objects database nodes
        DomainObjectCreator doc = new DomainObjectCreator(dataImportService, pdxDataTables);
        //save db
        doc.loadDomainObjects();

    }

}
