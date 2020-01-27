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
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

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
        PathMatcher metadataFiles = FileSystems.getDefault().getPathMatcher("glob:**{metadata-,sampleplatform}*.tsv");
        return cleanPdxDataTables(metadataReader.readAllTsvFilesIn(updogDir, metadataFiles));
    }

    private Map<String, Table> cleanPdxDataTables(Map<String, Table> pdxDataTables) {
        pdxDataTables.remove("metadata-checklist.tsv");
        removeDescriptionColumn(pdxDataTables);
        pdxDataTables = removePdxHeaderRows(pdxDataTables);
        pdxDataTables = removeBlankRows(pdxDataTables);
        return pdxDataTables;
    }

    private Map<String, Table> removePdxHeaderRows(Map<String, Table> pdxDataTables) {
        return pdxDataTables.entrySet().stream().collect(
            Collectors.toMap(
                e -> e.getKey(),
                e -> TableUtilities.removeHeaderRows(e.getValue(), 4)
            ));
    }

    private Map<String, Table> removeBlankRows(Map<String, Table> pdxDataTables) {
        return pdxDataTables.entrySet().stream().collect(
            Collectors.toMap(
                e -> e.getKey(),
                e -> TableUtilities.removeRowsMissingRequiredColumnValue(
                    e.getValue(),
                    e.getValue().column(0).asStringColumn())
            ));
    }

    private void removeDescriptionColumn(Map<String, Table> pdxDataTables) {
        pdxDataTables.values().forEach(t -> t.removeColumns("Field"));
    }

    private boolean validatePdxDataTables(Map<String, Table> pdxDataTables, String provider){
        return metadataValidator.passesValidation(pdxDataTables, fileSetSpecification(), provider);
    }

    private FileSetSpecification fileSetSpecification() {
        return FileSetSpecification.create().addRequiredFileList(
            Arrays.asList(
                "metadata-loader.tsv",
                "metadata-checklist.tsv",
                "metadata-sharing.tsv",
                "metadata-model_validation.tsv",
                "metadata-patient.tsv",
                "metadata-model.tsv",
                "metadata-sample.tsv"
            ));
    }

    private void createPdxObjects(){

        //create domain objects database nodes
        DomainObjectCreator doc = new DomainObjectCreator(dataImportService, pdxDataTables);
        //save db
        doc.loadDomainObjects();

    }

}
