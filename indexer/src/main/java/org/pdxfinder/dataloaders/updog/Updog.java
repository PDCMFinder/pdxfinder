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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class Updog {

    private static final Logger log = LoggerFactory.getLogger(Updog.class);
    private UtilityService utilityService;
    private DataImportService dataImportService;
    private Reader reader;
    private Validator validator;

    @Autowired
    public Updog(
        DataImportService dataImportService,
        UtilityService utilityService,
        Reader reader,
        Validator validator) {

        Assert.notNull(dataImportService, "dataImportService cannot be null");
        Assert.notNull(utilityService, "utilityService cannot be null");
        Assert.notNull(reader, "reader cannot be null");
        Assert.notNull(validator, "validator cannot be null");

        this.dataImportService = dataImportService;
        this.utilityService = utilityService;
        this.reader = reader;
        this.validator = validator;

    }

    public void run(Path updogProviderDirectory, String provider) {
        Assert.notNull(provider, "provider cannot be null");
        log.debug("Using UPDOG to import {} PDX data from [{}]", provider, updogProviderDirectory);

        Map<String, Table> pdxTableSet, omicsTableSet;
        pdxTableSet = readPdxTablesFromPath(updogProviderDirectory);
        pdxTableSet = cleanPdxTableSet(pdxTableSet);
        omicsTableSet = readOmicsTablesFromPath(updogProviderDirectory);
        validatePdxDataTables(pdxTableSet, provider);
        createPdxObjects(pdxTableSet);
    }

    private Map<String, Table> readOmicsTablesFromPath(Path updogProviderDirectory) {
        // Only cytogenetics import supported so far
        PathMatcher allTsvFiles = FileSystems.getDefault().getPathMatcher("glob:**/cyto/*.tsv");
        return reader.readAllOmicsFilesIn(updogProviderDirectory, allTsvFiles);
    }

    private Map<String, Table> readPdxTablesFromPath(Path updogProviderDirectory) {
        PathMatcher metadataFiles = FileSystems.getDefault().getPathMatcher("glob:**{metadata-,sampleplatform}*.tsv");
        return reader.readAllTsvFilesIn(updogProviderDirectory, metadataFiles);
    }

    private Map<String, Table> cleanPdxTableSet(Map<String, Table> pdxTableSet) {
        pdxTableSet.remove("metadata-checklist.tsv");
        removeDescriptionColumn(pdxTableSet);
        pdxTableSet = removeHeaderRows(pdxTableSet);
        pdxTableSet = removeBlankRows(pdxTableSet);
        return pdxTableSet;
    }

    private Map<String, Table> removeHeaderRows(Map<String, Table> tableSet) {
        return tableSet.entrySet().stream().collect(
            Collectors.toMap(
                e -> e.getKey(),
                e -> TableUtilities.removeHeaderRows(e.getValue(), 4)
            ));
    }

    private Map<String, Table> removeBlankRows(Map<String, Table> tableSet) {
        return tableSet.entrySet().stream().collect(
            Collectors.toMap(
                e -> e.getKey(),
                e -> TableUtilities.removeRowsMissingRequiredColumnValue(
                    e.getValue(),
                    e.getValue().column(0).asStringColumn())
            ));
    }

    private void removeDescriptionColumn(Map<String, Table> tableSet) {
        tableSet.values().forEach(t -> t.removeColumns("Field"));
    }

    private boolean validatePdxDataTables(Map<String, Table> tableSet, String provider){
        return validator.passesValidation(tableSet, tableSetSpecification(provider));
    }

    private TableSetSpecification tableSetSpecification(String provider) {
        return TableSetSpecification.create()
            .addRequiredFileList(
                Stream.of(
                    "metadata-loader.tsv",
                    "metadata-checklist.tsv",
                    "metadata-sharing.tsv",
                    "metadata-model_validation.tsv",
                    "metadata-patient.tsv",
                    "metadata-model.tsv",
                    "metadata-sample.tsv"
                ).collect(Collectors.toSet()))
            .setProvider(provider);
    }

    private void createPdxObjects(Map<String, Table> pdxTableSet){

        //create domain objects database nodes
        DomainObjectCreator doc = new DomainObjectCreator(dataImportService, pdxTableSet);
        //save db
        doc.loadDomainObjects();

    }

}
