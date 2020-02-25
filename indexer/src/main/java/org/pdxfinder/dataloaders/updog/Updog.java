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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class Updog {

    private Reader reader;
    private TableSetCleaner tableSetCleaner;
    private Validator validator;
    private DomainObjectCreator domainObjectCreator;
    private static final Logger log = LoggerFactory.getLogger(Updog.class);

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
        List<ValidationError> validationErrors;

        pdxTableSet = readPdxTablesFromPath(updogProviderDirectory);
        pdxTableSet = tableSetCleaner.cleanPdxTables(pdxTableSet);

        omicsTableSet = readOmicsTablesFromPath(updogProviderDirectory);
        omicsTableSet = tableSetCleaner.cleanOmicsTables(omicsTableSet);

        treatmentTableSet = readTreatmentTablesFromPath(updogProviderDirectory);
        treatmentTableSet = tableSetCleaner.cleanTreatmentTables(treatmentTableSet);

        combinedTableSet.putAll(pdxTableSet);
        combinedTableSet.putAll(omicsTableSet);
        combinedTableSet.putAll(treatmentTableSet);

        validationErrors = validateTableSet(combinedTableSet, omicsTableSet.keySet(), provider);
        log.info(validationErrors.toString());

        if (!validateOnly) {
            createPdxObjects(combinedTableSet);
        }
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

    private List<ValidationError> validateTableSet(
        Map<String, Table> tableSet,
        Set<String> omicTables,
        String provider
    ){
        TableSetSpecification omicSpecifications = TableSetSpecification.create();
        for (String tableName : omicTables) {
            merge(omicSpecifications, new OmicValidationRuleset().generateForOmicTable(tableName, provider));
        }

        TableSetSpecification combinedValidationRuleset = merge(
            new PdxValidationRuleset().generate(provider),
            omicSpecifications
        );
        return validator.validate(tableSet, combinedValidationRuleset);
    }

    private void createPdxObjects(Map<String, Table> tableSet){
        domainObjectCreator.loadDomainObjects(tableSet);
    }

    static TableSetSpecification merge(TableSetSpecification ...tableSetSpecifications) {
        TableSetSpecification mergedTableSetSpecifications = TableSetSpecification.create();
        for (TableSetSpecification tss : tableSetSpecifications) {
            mergedTableSetSpecifications.setProvider(tss.getProvider());
            mergedTableSetSpecifications.addRequiredTables(tss.getRequiredTables());
            mergedTableSetSpecifications.addRequiredColumns(tss.getRequiredColumns());
            mergedTableSetSpecifications.addNonEmptyColumns(tss.getNonEmptyColumns());
            mergedTableSetSpecifications.addUniqueColumns(tss.getUniqueColumns());
            mergedTableSetSpecifications.addHasRelations(tss.getHasRelations());
        }
        return mergedTableSetSpecifications;
    }

}
