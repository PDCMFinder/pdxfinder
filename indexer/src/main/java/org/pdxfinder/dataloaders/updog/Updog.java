package org.pdxfinder.dataloaders.updog;

import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import tech.tablesaw.api.Table;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Map;

@Component
public class Updog {

    private static final Logger log = LoggerFactory.getLogger(Updog.class);
    private UtilityService utilityService;
    private DataImportService dataImportService;
    private MetadataReader metadataReader;
    private MetadataValidator metadataValidator;
    private Map<String, Table> pdxDataTables;
    private Map<String, Table> omicsTables;
    private String provider;

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

    }

    public void run(Path updogDir, String provider) {
        Assert.notNull(provider, "provider cannot be null");
        log.debug("Using UPDOG to import {} from [{}]", provider, updogDir);

        pdxDataTables = readPdxDataFromPath(updogDir);
        omicsTables = readOmicsDataFromPath(updogDir);
        validatePdxDataTables(pdxDataTables, provider);
        createPdxObjects();
    }

    private Map<String, Table> readOmicsDataFromPath(Path updogDir) {
        // Only cytogenetics import supported so far
        PathMatcher allTsvFiles = FileSystems.getDefault().getPathMatcher("glob:**/cyto/*.tsv");
        return metadataReader.readAllOmicsFilesIn(updogDir, allTsvFiles);
    }

    private Map<String, Table> readPdxDataFromPath(Path updogDir) {
        PathMatcher metadataFiles = FileSystems.getDefault().getPathMatcher("glob:**metadata-*.tsv");
        return metadataReader.readAllTsvFilesIn(updogDir, metadataFiles);
    }

    private boolean validatePdxDataTables(Map<String, Table> pdxDataTables, String provider){
        return metadataValidator.passesValidation(pdxDataTables, provider);
    }

    private void createPdxObjects(){

        //create domain objects database nodes
        DomainObjectCreator doc = new DomainObjectCreator(dataImportService, pdxDataTables);
        //save db
        doc.loadDomainObjects();

    }

}
