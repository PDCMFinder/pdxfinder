package org.pdxfinder.dataloaders.updog;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class PdxValidationRuleset extends ValidationRuleCreator {

    private Set<Pair<String, String>> tableColumns;

    PdxValidationRuleset() {
        this.tableColumns = createColumns();
    }

    private Set<Pair<String, String>> createColumns() {
        Set<Pair<String, String>> tableColumns = new HashSet<>();
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

        Set<Pair<String, String>> idColumns = matchingColumnsFromAnyTable(tableColumns, "_id");
        Set<Pair<String, String>> hostStrainColumns = matchingColumnsFromAnyTable(tableColumns, "host_strain");

        Set<Pair<String, String>> essentialSampleColumns = matchingColumnsFromTable(tableColumns, "sample",
            new String[]{"age_in_years", "diagnosis", "tumour", "_site", "treatment_naive"});
        Set<Pair<String, String>> essentialModelColumns = matchingColumnsFromTable(tableColumns, "model.",
            new String[]{"engraftment_", "sample_type", "passage_number"});
        Set<Pair<String, String>> essentialModelValidationColumns = matchingColumnsFromTable(tableColumns, "model_validation",
            new String[]{"validation_technique", "description", "passages_tested"});
        Set<Pair<String, String>> essentialSharingColumns = matchingColumnsFromTable(tableColumns, "sharing",
            new String[]{"provider_", "access", "email", "name", "project"});
        Set<Pair<String, String>> essentialLoaderColumns = matchingColumnsFromTable(tableColumns, "loader",
            new String[]{"name", "abbreviation"});

        Set<Pair<String, String>> essentialColumns = TableSetUtilities.concatenate(
            idColumns,
            hostStrainColumns,
            essentialSampleColumns,
            essentialModelColumns,
            essentialModelValidationColumns,
            essentialSharingColumns,
            essentialLoaderColumns
        );

        return TableSetSpecification.create()
            .addRequiredTables(metadataTables)
            .addRequiredColumns(essentialColumns)
            .addNonEmptyColumns(essentialColumns)
            .addUniqueColumns(idColumns)
            .addHasRelations(new HashSet<>(Arrays.asList(
                relation("metadata-patient.tsv", "metadata-sample.tsv", "patient_id"),
                relation("metadata-sample.tsv", "metadata-model.tsv", "model_id"),
                relation("metadata-model.tsv", "metadata-model_validation.tsv", "model_id"),
                relation("metadata-model.tsv", "metadata-sharing.tsv", "model_id")
            )))
            .setProvider(provider);
    }

}
