package org.pdxfinder.dataloaders.updog.tablevalidation;

import org.pdxfinder.dataloaders.updog.TableSetUtilities;

import java.util.*;
import java.util.stream.Collectors;

public class PdxValidationRuleset extends ValidationRuleCreator {

    private Set<ColumnReference> columnReferences;
    static final String NOTCOLLECTED = "not collected";
    static final String NOTPROVIDED = " not provided";

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
        Set<String> metadataTables = getMetadataTables();
        Set<ColumnReference> idColumns = matchingColumnsFromAnyTable(columnReferences, "_id");
        Set<ColumnReference> uniqIdColumns = getUniqueColumns();
        Set<ColumnReference> essentialColumns = getEssentialColumns(idColumns);
        Set<ColumnReference> freeTextColumns = getFreeTextColumns();
        Map<Set<ColumnReference>, ValueRestrictions> categoricalRestrictions = getCategoricalRestrictions();

        return TableSetSpecification.create()
            .addRequiredTables(metadataTables)
            .addRequiredColumns(essentialColumns)
            .addNonEmptyColumns(essentialColumns)
            .addValueRestriction(idColumns, ValueRestrictions.URL_SAFE())
            .addValueRestriction(freeTextColumns, ValueRestrictions.FREE_TEXT())
            .addAllValueRestrictions(categoricalRestrictions)
            .addUniqueColumns(uniqIdColumns)
            .addRelations(new HashSet<>(Arrays.asList(
                Relation.betweenTableKeys(
                    ColumnReference.of("metadata-patient.tsv", "patient_id" ),
                    ColumnReference.of("metadata-sample.tsv", "patient_id")),
                Relation.betweenTableKeys(
                    ColumnReference.of("metadata-sample.tsv", "model_id"),
                    ColumnReference.of("metadata-model.tsv", "model_id")),
                Relation.betweenTableKeys(
                    ColumnReference.of("metadata-model.tsv", "model_id"),
                    ColumnReference.of("metadata-sharing.tsv", "model_id")),
                Relation.betweenTableColumns(
                        Relation.validityType.one_to_many,
                        ColumnReference.of("metadata-sample.tsv", "sample_id"),
                        ColumnReference.of("metadata-sample.tsv", "patient_id")
                        ),
                Relation.betweenTableColumns(
                        Relation.validityType.one_to_one,
                        ColumnReference.of("metadata-sample.tsv", "sample_id"),
                        ColumnReference.of("metadata-sample.tsv", "model_id"))
            )));
    }

    private Set<String> getMetadataTables() {
        return columnReferences.stream()
                .map(ColumnReference::table)
                .filter(c -> c.contains("metadata"))
                .collect(Collectors.toSet());
    }

    private Set<ColumnReference> getUniqueColumns() {
        Set<ColumnReference> uniqIdColumns = new HashSet<>();
        uniqIdColumns.addAll(matchingColumnsFromTable(columnReferences, "metadata-patient.tsv", new String[]{"patient_id"}));
        uniqIdColumns.addAll(matchingColumnsFromTable(columnReferences, "metadata-sharing.tsv", new String[]{"model_id"}));
        return uniqIdColumns;
    }

    private Map<Set<ColumnReference>, ValueRestrictions> getCategoricalRestrictions() {
        Set<ColumnReference> tumourTypeColumn = matchingColumnsFromTable(columnReferences,
                "sample", new String[]{"tumour_type"});
        ValueRestrictions tumourTypeCategories = ValueRestrictions.of(Arrays.asList("primary",
                "metastatic", "recurrent", "refactory", NOTCOLLECTED, NOTPROVIDED), "Collected Tumour Type");


        Map<Set<ColumnReference>, ValueRestrictions> categoricalRestrictions = new HashMap<>();
        categoricalRestrictions.put(tumourTypeColumn, tumourTypeCategories);

        return categoricalRestrictions;
    }

    private Set<ColumnReference> getFreeTextColumns() {
        Set<ColumnReference> patientFreeTextColumns = matchingColumnsFromTable(columnReferences, "patient",
                new String[]{"history", "ethnicity", "initial_diagnosis" });
        Set<ColumnReference> sampleFreeTextColumns = matchingColumnsFromTable(columnReferences, "sample",
                new String[]{"diagnosis" , "primary_site", "collection_site"});
        Set<ColumnReference> modelFreeTextColumns = matchingColumnsFromTable(columnReferences, "model",
                new String[]{"diagnosis" , "primary_site", "collection_site", "sample_type", "sample_state"});
        Set<ColumnReference> modelValidationFreeTextColumns = matchingColumnsFromTable(columnReferences, "model_validation",
                new String[]{"validation_technique", "description"});
        Set<ColumnReference> sharingFreeTextColumns = matchingColumnsFromTable(columnReferences, "model_validation",
                new String[]{"provider_abbreviation", "project"});

        return TableSetUtilities.concatenate(
                patientFreeTextColumns,
                sampleFreeTextColumns,
                modelFreeTextColumns,
                modelValidationFreeTextColumns,
                sharingFreeTextColumns
        );
    }

    private Set<ColumnReference> getEssentialColumns(Set<ColumnReference> idColumns) {
        Set<ColumnReference> essentialSampleColumns = matchingColumnsFromTable(columnReferences, "sample",
                new String[]{"age_in_years", "diagnosis", "tumour_type", "primary_site", "collection_site"});
        Set<ColumnReference> essentialModelColumns = matchingColumnsFromTable(columnReferences, "model",
                new String[]{"host_strain_full", "engraftment_site","engraftment_type","sample_type", "passage_number"});
        Set<ColumnReference> essentialModelValidationColumns = matchingColumnsFromTable(columnReferences, "model_validation",
                new String[]{"validation_technique", "description", "passages_tested", "validation_host_strain_full"});
        Set<ColumnReference> essentialSharingColumns = matchingColumnsFromTable(columnReferences, "sharing",
                new String[]{"provider_type", "accessibility", "email", "name", "provider_name", "provider_abbreviation", "project"});
        Set<ColumnReference> essentialLoaderColumns = matchingColumnsFromTable(columnReferences, "loader",
                new String[]{"name", "abbreviation", "internal_url"});

        return TableSetUtilities.concatenate(
                idColumns,
                essentialSampleColumns,
                essentialModelColumns,
                essentialModelValidationColumns,
                essentialSharingColumns,
                essentialLoaderColumns
        );
    }

}
