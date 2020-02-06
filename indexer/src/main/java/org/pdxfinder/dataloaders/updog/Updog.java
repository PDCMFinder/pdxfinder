package org.pdxfinder.dataloaders.updog;

import org.apache.commons.lang3.tuple.Pair;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

        Map<String, Table> pdxTableSet;
        Map<String, Table> omicsTableSet;
        Map<String, Table> treatmentTableSet;
        Map<String, Table> combinedTableSet = new HashMap<>();

        pdxTableSet = readPdxTablesFromPath(updogProviderDirectory);
        pdxTableSet = TableSetUtilities.cleanPdxTableSet(pdxTableSet);
        omicsTableSet = readOmicsTablesFromPath(updogProviderDirectory);
        omicsTableSet = TableSetUtilities.removeProviderNameFromFilename(omicsTableSet);
        treatmentTableSet = readTreatmentTablesFromPath(updogProviderDirectory);


        combinedTableSet.putAll(pdxTableSet);
        combinedTableSet.putAll(omicsTableSet);
        combinedTableSet.putAll(treatmentTableSet);
        List<ValidationError> validationErrors = validatePdxDataTables(combinedTableSet, provider);

        createPdxObjects(combinedTableSet);
    }

    private Map<String, Table> readOmicsTablesFromPath(Path updogProviderDirectory) {
        // Only cytogenetics and mutation import supported so far
        PathMatcher allTsvFiles = FileSystems.getDefault().getPathMatcher("glob:**/{cyto,mut}/*.tsv");
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
        return validator.validate(tableSet, createTableSetSpecification(createColumns(), provider));
    }

    private void createPdxObjects(Map<String, Table> pdxTableSet){
        DomainObjectCreator doc = new DomainObjectCreator(dataImportService, pdxTableSet);
        doc.loadDomainObjects();
    }

    private TableSetSpecification createTableSetSpecification(
        List<Pair<String, String>> columns,
        String provider
    ) {
        Set<String> metadataTables = columns.stream()
            .map(Pair::getKey)
            .filter(s -> s.contains("metadata"))
            .collect(Collectors.toSet());

        List<Pair<String, String>> idColumns = matchingColumnsFromAnyTable(columns, "_id");
        List<Pair<String, String>> hostStrainColumns = matchingColumnsFromAnyTable(columns, "host_strain");

        List<Pair<String, String>> sampleColumns = matchingColumnsFromTable(columns, "sample",
            new String[]{"age_in_years", "diagnosis", "tumour", "_site", "treatment_naive"});
        List<Pair<String, String>> modelColumns = matchingColumnsFromTable(columns, "model.",
            new String[]{"engraftment_", "sample_type", "passage_number"});
        List<Pair<String, String>> modelValidationColumns = matchingColumnsFromTable(columns, "model_validation",
            new String[]{"validation_technique", "description", "passages_tested"});
        List<Pair<String, String>> sharingColumns = matchingColumnsFromTable(columns, "sharing",
            new String[]{"provider_", "access", "email", "name", "project"});
        List<Pair<String, String>> loaderColumns = matchingColumnsFromTable(columns, "loader",
            new String[]{"name", "abbreviation"});

        List<Pair<String, String>> requiredColumns = concatenate(
            idColumns,
            hostStrainColumns,
            sampleColumns,
            modelColumns,
            modelValidationColumns,
            sharingColumns,
            loaderColumns
        );

        return TableSetSpecification.create()
            .addRequiredTables(metadataTables)
            .addRequiredColumns(requiredColumns)
            .addUniqueColumns(idColumns)
            .addHasRelations(Arrays.asList(
                relation("metadata-patient.tsv", "metadata-sample.tsv", "patient_id"),
                relation("metadata-sample.tsv", "metadata-model.tsv", "model_id"),
                relation("metadata-model.tsv", "metadata-model_validation.tsv", "model_id"),
                relation("metadata-model.tsv", "metadata-sharing.tsv", "model_id")
            ))
            .setProvider(provider);
    }

    private List<Pair<String, String>> matchingColumnsFromTable(
        List<Pair<String, String>> columns,
        String tableName,
        String[] columnNamePatterns) {
        return columns
            .stream()
            .filter(p -> p.getKey().contains(tableName))
            .filter(p -> containsAny(p.getValue(), columnNamePatterns))
            .collect(Collectors.toList());
    }

    private List<Pair<String, String>> matchingColumnsFromAnyTable(
        List<Pair<String, String>> columns,
        String columnNamePattern
    ) {
        return columns.stream()
            .filter(p -> p.getValue().contains(columnNamePattern))
            .collect(Collectors.toList());
    }

    private static boolean containsAny(String inputStr, String[] items) {
        return Arrays.stream(items).parallel().anyMatch(inputStr::contains);
    }

    public static<T> List<T> concatenate(List<T>... lists) {
        return Stream.of(lists)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    private Pair<Pair<String, String>, Pair<String, String>> relation(
        String from, String to, String columnName
    ) {
        return Pair.of(Pair.of(from, columnName), Pair.of(to, columnName));
    }

    private List<Pair<String, String>> createColumns() {
        List<Pair<String, String>> tableColumns = new ArrayList<>();
        Arrays.asList(
            "patient_id",
            "sex",
            "history",
            "ethnicity",
            "ethnicity_assessment_method",
            "initial_diagnosis",
            "age_at_initial_diagnosis"
        ).forEach(s -> tableColumns.add(Pair.of("metadata-patient.tsv", s)));
        Arrays.asList(
            "patient_id",
            "sample_id",
            "collection_date",
            "collection_event",
            "months_since_collection_1",
            "age_in_years_at_collection",
            "diagnosis",
            "tumour_type",
            "primary_site",
            "collection_site",
            "stage",
            "staging_system",
            "grade",
            "grading_system",
            "virology_status",
            "sharable",
            "treatment_naive_at_collection",
            "treated",
            "prior_treatment",
            "model_id"
        ).forEach(s -> tableColumns.add(Pair.of("metadata-sample.tsv", s)));
        Arrays.asList(
            "model_id",
            "host_strain",
            "host_strain_full",
            "engraftment_site",
            "engraftment_type",
            "sample_type",
            "sample_state",
            "passage_number",
            "publications"
        ).forEach(s -> tableColumns.add(Pair.of("metadata-model.tsv", s)));
        Arrays.asList(
            "model_id",
            "validation_technique",
            "description",
            "passages_tested",
            "validation_host_strain_full"
        ).forEach(s -> tableColumns.add(Pair.of("metadata-model_validation.tsv", s)));
        Arrays.asList(
            "model_id",
            "provider_type",
            "accessibility",
            "europdx_access_modality",
            "email",
            "name",
            "form_url",
            "database_url",
            "provider_name",
            "provider_abbreviation",
            "project"
        ).forEach(s -> tableColumns.add(Pair.of("metadata-sharing.tsv", s)));
        Arrays.asList(
            "name",
            "abbreviation",
            "internal_url",
            "internal_dosing_url"
        ).forEach(s -> tableColumns.add(Pair.of("metadata-loader.tsv", s)));
        return tableColumns;
    }


}
