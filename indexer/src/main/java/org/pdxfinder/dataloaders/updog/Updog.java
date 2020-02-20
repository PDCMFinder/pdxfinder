package org.pdxfinder.dataloaders.updog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tablesaw.api.Table;

import javax.annotation.Nonnull;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Updog {

    private static final Logger log = LoggerFactory.getLogger(Updog.class);
    private Reader reader;
    private TableSetCleaner tableSetCleaner;
    private Validator validator;
    private DomainObjectCreator domainObjectCreator;

    @Autowired
    public Updog(
        @Nonnull Reader reader,
        @Nonnull TableSetCleaner tableSetCleaner,
        @Nonnull Validator validator,
        @Nonnull DomainObjectCreator domainObjectCreator
    ) {
        this.reader = reader;
        this.tableSetCleaner = tableSetCleaner;
        this.validator = validator;
        this.domainObjectCreator = domainObjectCreator;
    }

    public void run(Path updogProviderDirectory, String provider, boolean validateOnly) {
        log.debug("Using UPDOG to import {} PDX data from [{}]", provider, updogProviderDirectory);

        Map<String, Table> pdxTableSet;
        Map<String, Table> omicsTableSet;
        Map<String, Table> treatmentTableSet;
        Map<String, Table> combinedTableSet = new HashMap<>();
        List<ValidationError> validationErrors = new ArrayList<>();

        pdxTableSet = readPdxTablesFromPath(updogProviderDirectory);
        pdxTableSet = tableSetCleaner.cleanPdxTables(pdxTableSet);
        validationErrors.addAll(validatePdxDataTables(pdxTableSet, provider));

        omicsTableSet = readOmicsTablesFromPath(updogProviderDirectory);
        omicsTableSet = tableSetCleaner.cleanOmicsTables(omicsTableSet);
        validationErrors.addAll(validateOmicsTables(omicsTableSet, provider));

        treatmentTableSet = readTreatmentTablesFromPath(updogProviderDirectory);
        treatmentTableSet = TableSetUtilities.removeHeaderRowsIfPresent(treatmentTableSet);

        combinedTableSet.putAll(pdxTableSet);
        combinedTableSet.putAll(omicsTableSet);
        combinedTableSet.putAll(treatmentTableSet);

        if (!validateOnly) {
            createPdxObjects(combinedTableSet);
        }
    }

    private List<ValidationError> validateOmicsTables(Map<String, Table> omicsTableSet, String provider) {
        return new ArrayList<>();
    }

    private Map<String, Table> readOmicsTablesFromPath(Path updogProviderDirectory) {
        PathMatcher allTsvFiles = FileSystems.getDefault().getPathMatcher("glob:**/{cyto,mut,cna}/*.tsv");
        return reader.readAllOmicsFilesIn(updogProviderDirectory, allTsvFiles);
    }

    private Map<String, Table> readPdxTablesFromPath(Path updogProviderDirectory) {
        PathMatcher metadataFiles = FileSystems.getDefault().getPathMatcher("glob:**{metadata-,sampleplatform}*.tsv");
        return reader.readAllTsvFilesIn(updogProviderDirectory, metadataFiles);
    }

    private Map<String, Table> readTreatmentTablesFromPath(Path updogProviderDirectory) {
        PathMatcher metadataFiles = FileSystems.getDefault().getPathMatcher("glob:**{treatment,drug}*.tsv");
        return reader.readAllTreatmentFilesIn(updogProviderDirectory, metadataFiles);
    }

    private List<ValidationError> validatePdxDataTables(Map<String, Table> tableSet, String provider){
        PdxValidationRuleset pdxValidationRuleset = new PdxValidationRuleset();
        return validator.validate(tableSet, pdxValidationRuleset.generate(provider));
    }

    private void createPdxObjects(Map<String, Table> tableSet){
        domainObjectCreator.loadDomainObjects(tableSet);
    }

}
