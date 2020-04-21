package org.pdxfinder.dataloaders.updog.tablevalidation;

import org.pdxfinder.dataloaders.updog.TableSetUtilities;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class PdxValidationRuleset extends ValidationRuleCreator {

    private Set<ColumnReference> columnReferences;

    public PdxValidationRuleset() {
        this.columnReferences = createColumns();
    }

    private Set<ColumnReference> createColumns() {
        Set<ColumnReference> tableColumns = new HashSet<>();
        Arrays.asList(
            "patient_id",
            "sex",
            "history",
            "ethnicity",
            "ethnicity_assessment_method",
            "initial_diagnosis",
            "age_at_initial_diagnosis"
        ).forEach(s -> tableColumns.add(ColumnReference.of("metadata-patient.tsv", s)));
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
        ).forEach(s -> tableColumns.add(ColumnReference.of("metadata-sample.tsv", s)));
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
        ).forEach(s -> tableColumns.add(ColumnReference.of("metadata-model.tsv", s)));
        Arrays.asList(
            "model_id",
            "validation_technique",
            "description",
            "passages_tested",
            "validation_host_strain_full"
        ).forEach(s -> tableColumns.add(ColumnReference.of("metadata-model_validation.tsv", s)));
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
        ).forEach(s -> tableColumns.add(ColumnReference.of("metadata-sharing.tsv", s)));
        Arrays.asList(
            "name",
            "abbreviation",
            "internal_url",
            "internal_dosing_url"
        ).forEach(s -> tableColumns.add(ColumnReference.of("metadata-loader.tsv", s)));
        return tableColumns;
    }

    @Override
    public TableSetSpecification generate(String provider) {

        Set<String> metadataTables = columnReferences.stream()
            .map(ColumnReference::table)
            .filter(c -> c.contains("metadata"))
            .collect(Collectors.toSet());

        Set<ColumnReference> idColumns = matchingColumnsFromAnyTable(columnReferences, "_id");
        Set<ColumnReference> hostStrainColumns = matchingColumnsFromAnyTable(columnReferences, "host_strain");

        Set<ColumnReference>essentialSampleColumns = matchingColumnsFromTable(columnReferences, "sample",
            new String[]{"age_in_years", "diagnosis", "tumour", "_site", "treatment_naive"});
        Set<ColumnReference>essentialModelColumns = matchingColumnsFromTable(columnReferences, "model.",
            new String[]{"engraftment_", "sample_type", "passage_number"});
        Set<ColumnReference>essentialModelValidationColumns = matchingColumnsFromTable(columnReferences, "model_validation",
            new String[]{"validation_technique", "description", "passages_tested"});
        Set<ColumnReference>essentialSharingColumns = matchingColumnsFromTable(columnReferences, "sharing",
            new String[]{"provider_", "access", "email", "name", "project"});
        Set<ColumnReference>essentialLoaderColumns = matchingColumnsFromTable(columnReferences, "loader",
            new String[]{"name", "abbreviation"});

        Set<ColumnReference> essentialColumns = TableSetUtilities.concatenate(
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
            .addRelations(new HashSet<>(Arrays.asList(
                Relation.between(
                    ColumnReference.of("metadata-patient.tsv", "patient_id"),
                    ColumnReference.of("metadata-sample.tsv", "patient_id")),
                Relation.between(
                    ColumnReference.of("metadata-sample.tsv", "model_id"),
                    ColumnReference.of("metadata-model.tsv", "model_id")),
                Relation.between(
                    ColumnReference.of("metadata-model.tsv", "model_id"),
                    ColumnReference.of("metadata-model_validation.tsv", "model_id")),
                Relation.between(
                    ColumnReference.of("metadata-model.tsv", "model_id"),
                    ColumnReference.of("metadata-sharing.tsv", "model_id"))
            )))
            .setProvider(provider);
    }

}
