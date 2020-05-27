package org.pdxfinder.dataloaders.updog;

import org.apache.commons.collections4.CollectionUtils;
import org.pdxfinder.dataloaders.updog.domainobjectcreation.DomainObjectCreator;
import org.pdxfinder.dataloaders.updog.tablevalidation.OmicValidationRuleset;
import org.pdxfinder.dataloaders.updog.tablevalidation.PdxValidationRuleset;
import org.pdxfinder.dataloaders.updog.tablevalidation.TableSetSpecification;
import org.pdxfinder.dataloaders.updog.tablevalidation.error.ValidationError;
import org.pdxfinder.dataloaders.updog.tablevalidation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tech.tablesaw.api.Table;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.*;

@Component
public class Updog {

    private Reader reader;
    private TableSetCleaner tableSetCleaner;
    private Validator validator;
    private DomainObjectCreator domainObjectCreator;
    private static final Logger log = LoggerFactory.getLogger(Updog.class);

    public Updog(
        Reader reader,
        TableSetCleaner tableSetCleaner,
        Validator validator,
        DomainObjectCreator domainObjectCreator
    ) {
        this.reader = reader;
        this.tableSetCleaner = tableSetCleaner;
        this.validator = validator;
        this.domainObjectCreator = domainObjectCreator;
    }

    public void run(Path updogProviderDirectory, String provider, boolean validateOnly) {
        Map<String, Table> pdxTableSet;
        Map<String, Table> omicsTableSet;
        Map<String, Table> treatmentTableSet;
        Map<String, Table> combinedTableSet = new HashMap<>();
        List<ValidationError> validationErrors;
        log.info("Using UPDOG to import {} PDX data from [{}]", provider, updogProviderDirectory);

        pdxTableSet = readPdxTablesFromPath(updogProviderDirectory);
        pdxTableSet = tableSetCleaner.cleanPdxTables(pdxTableSet);

        omicsTableSet = new HashMap<>();
        treatmentTableSet = readTreatmentTablesFromPath(updogProviderDirectory);
        treatmentTableSet = tableSetCleaner.cleanTreatmentTables(treatmentTableSet);

        combinedTableSet.putAll(pdxTableSet);
        combinedTableSet.putAll(treatmentTableSet);

        validationErrors = validateTableSet(combinedTableSet, omicsTableSet.keySet(), provider);
        reportAnyErrors(validationErrors);

        if (!validateOnly) {
            domainObjectCreator.loadDomainObjects(combinedTableSet, updogProviderDirectory);
        }
    }

    private void reportAnyErrors(List<ValidationError> validationErrors) {
        if (CollectionUtils.isNotEmpty(validationErrors))
            for (ValidationError error : validationErrors) {
                log.error(error.verboseMessage());
            }
        else
            log.info("There were no validation errors raised, great!");
    }

    private boolean hasNoValidationErrors(List<ValidationError> errors) {
        return CollectionUtils.isEmpty(errors);
    }

    private Map<String, Table> readOmicsTablesFromPath(Path updogProviderDirectory) {
        PathMatcher allTsvFiles = FileSystems.getDefault().getPathMatcher("glob:**/{cyto,mut,cna,expression}/*.tsv");
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
    ) {
        TableSetSpecification omicSpecifications = TableSetSpecification.create();
        for (String tableName : omicTables) {
            omicSpecifications =  omicSpecifications.merge(OmicValidationRuleset.generateFor(tableName, provider));
        }

        TableSetSpecification combinedValidationRuleset = merge(
            new PdxValidationRuleset().generate(provider),
            omicSpecifications
        );
        combinedValidationRuleset.setProvider(provider);
        return validator.validate(tableSet, combinedValidationRuleset);
    }

    static TableSetSpecification merge(TableSetSpecification ...tableSetSpecifications) {
        TableSetSpecification mergedTableSetSpecifications = TableSetSpecification.create();
        for (TableSetSpecification tss : tableSetSpecifications) {
            mergedTableSetSpecifications.setProvider(tss.getProvider());
            mergedTableSetSpecifications.addRequiredTables(tss.getRequiredTables());
            mergedTableSetSpecifications.addRequiredColumns(tss.getRequiredColumns());
            mergedTableSetSpecifications.addNonEmptyColumns(tss.getNonEmptyColumns());
            mergedTableSetSpecifications.addUniqueColumns(tss.getUniqueColumns());
            mergedTableSetSpecifications.addRelations(tss.getRelations());
        }
        return mergedTableSetSpecifications;
    }

}
