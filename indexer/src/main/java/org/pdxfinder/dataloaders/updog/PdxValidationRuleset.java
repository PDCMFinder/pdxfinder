package org.pdxfinder.dataloaders.updog;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PdxValidationRuleset implements ValidationRuleCreator {

    private List<Pair<String, String>> tableColumns;

    PdxValidationRuleset() {
        this.tableColumns = createColumns();
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

    @Override
    public TableSetSpecification generate(String provider) {

        Set<String> metadataTables = tableColumns.stream()
            .map(Pair::getKey)
            .filter(s -> s.contains("metadata"))
            .collect(Collectors.toSet());

        List<Pair<String, String>> idColumns = matchingColumnsFromAnyTable(tableColumns, "_id");
        List<Pair<String, String>> hostStrainColumns = matchingColumnsFromAnyTable(tableColumns, "host_strain");

        List<Pair<String, String>> sampleColumns = matchingColumnsFromTable(tableColumns, "sample",
            new String[]{"age_in_years", "diagnosis", "tumour", "_site", "treatment_naive"});
        List<Pair<String, String>> modelColumns = matchingColumnsFromTable(tableColumns, "model.",
            new String[]{"engraftment_", "sample_type", "passage_number"});
        List<Pair<String, String>> modelValidationColumns = matchingColumnsFromTable(tableColumns, "model_validation",
            new String[]{"validation_technique", "description", "passages_tested"});
        List<Pair<String, String>> sharingColumns = matchingColumnsFromTable(tableColumns, "sharing",
            new String[]{"provider_", "access", "email", "name", "project"});
        List<Pair<String, String>> loaderColumns = matchingColumnsFromTable(tableColumns, "loader",
            new String[]{"name", "abbreviation"});

        List<Pair<String, String>> requiredColumns = TableSetUtilities.concatenate(
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

    private Pair<Pair<String, String>, Pair<String, String>> relation(
        String from, String to, String columnName
    ) {
        return Pair.of(Pair.of(from, columnName), Pair.of(to, columnName));
    }

    private static boolean containsAny(String inputStr, String[] items) {
        return Arrays.stream(items).parallel().anyMatch(inputStr::contains);
    }

}
