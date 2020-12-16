package org.pdxfinder.dataloaders.updog.tablevalidation.rules;

import static org.pdxfinder.dataloaders.updog.tablevalidation.rules.PdxValueRestrictions.getCollectionDateFormat;
import static org.pdxfinder.dataloaders.updog.tablevalidation.rules.PdxValueRestrictions.getCollectionEventFormat;
import static org.pdxfinder.dataloaders.updog.tablevalidation.rules.PdxValueRestrictions.getEthnicityAssessmentCategories;
import static org.pdxfinder.dataloaders.updog.tablevalidation.rules.PdxValueRestrictions.getFreeTextCharset;
import static org.pdxfinder.dataloaders.updog.tablevalidation.rules.PdxValueRestrictions.getNumericalCharset;
import static org.pdxfinder.dataloaders.updog.tablevalidation.rules.PdxValueRestrictions.getPmidFormat;
import static org.pdxfinder.dataloaders.updog.tablevalidation.rules.PdxValueRestrictions.getPriorTreatmentCategories;
import static org.pdxfinder.dataloaders.updog.tablevalidation.rules.PdxValueRestrictions.getProviderTypeCategories;
import static org.pdxfinder.dataloaders.updog.tablevalidation.rules.PdxValueRestrictions.getSexCategories;
import static org.pdxfinder.dataloaders.updog.tablevalidation.rules.PdxValueRestrictions.getShareCategories;
import static org.pdxfinder.dataloaders.updog.tablevalidation.rules.PdxValueRestrictions.getTreatmentNaiveAtCollectionCategories;
import static org.pdxfinder.dataloaders.updog.tablevalidation.rules.PdxValueRestrictions.getTumourTypeCategories;
import static org.pdxfinder.dataloaders.updog.tablevalidation.rules.PdxValueRestrictions.getUrlSafeCharset;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.pdxfinder.dataloaders.updog.TableSetUtilities;
import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;
import org.pdxfinder.dataloaders.updog.tablevalidation.Relation;
import org.pdxfinder.dataloaders.updog.tablevalidation.TableSetSpecification;
import org.pdxfinder.dataloaders.updog.tablevalidation.ValueRestrictions;

public class PdxValidationRuleset extends ValidationRuleCreator {

    private Set<ColumnReference> metadataColumnReferences;
    private Set<ColumnReference> idColumns;
    static final String NOTCOLLECTED = "not collected";
    static final String NOTPROVIDED = "not provided";


    public PdxValidationRuleset() {
        this.metadataColumnReferences = getMetadataColumns();
    }

    private Set<ColumnReference> matchingColumnFromMetadata(String tableName, String... columnName){
        return matchingColumnFromMetadata( tableName, columnName);
    }

    private Set<ColumnReference> matchingColumnsFromMetadata(String tableName, String... columns){
        return matchingColumnsFromTable(metadataColumnReferences, tableName, columns);
    }

    @Override
    public TableSetSpecification generate(String provider) {
        Set<String> metadataTables = getMetadataTables();
        idColumns = matchingColumnsFromAnyTable(metadataColumnReferences, "_id");
        Set<ColumnReference> uniqIdColumns = getUniqueColumns();
        Set<ColumnReference> essentialColumns = getEssentialColumns();
        Map<Set<ColumnReference>, ValueRestrictions> customRegexRestrictions = regexRestrictions();
        Map<Set<ColumnReference>, ValueRestrictions> categoricalRestrictions = getCategoricalRestrictions();

        return TableSetSpecification.create()
            .addRequiredTables(metadataTables)
            .addRequiredColumns(essentialColumns)
            .addNonEmptyColumns(essentialColumns)
            .addAllValueRestrictions(customRegexRestrictions)
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
                        validityType.ONE_TO_MANY,
                        ColumnReference.of("metadata-sample.tsv", "sample_id"),
                        ColumnReference.of("metadata-sample.tsv", "patient_id")
                        ),
                Relation.betweenTableColumns(
                        validityType.ONE_TO_ONE,
                        ColumnReference.of("metadata-sample.tsv", "sample_id"),
                        ColumnReference.of("metadata-sample.tsv", "model_id"))
            )));
    }

    private Map<Set<ColumnReference>, ValueRestrictions> regexRestrictions() {
        Map<Set<ColumnReference>, ValueRestrictions> regexRestrictions = new HashMap<>();
        Set<ColumnReference> freeTextColumns = getFreeTextColumns();

        Set<ColumnReference> collectionEventColumn = matchingColumnFromMetadata(
            "sample",
            "collection_event");

        Set<ColumnReference> collectionDateColumn = matchingColumnFromMetadata(
            "sample",
            "collection_date");

        Set<ColumnReference> pmidColumn = matchingColumnFromMetadata(
            "model",
            "publications");

        Set<ColumnReference> numericalColumn = matchingColumnFromMetadata()

        regexRestrictions.put(freeTextColumns, getFreeTextCharset());
        regexRestrictions.put(idColumns, getUrlSafeCharset());
        regexRestrictions.put(collectionEventColumn, getCollectionEventFormat());
        regexRestrictions.put(collectionDateColumn, getCollectionDateFormat());
        regexRestrictions.put(pmidColumn, getPmidFormat());
        return regexRestrictions;
    }

    private Set<ColumnReference> getNumericalColumns(){
        Set<ColumnReference> columnReferences = new HashSet<>();
        matchingColumnFromTable()

    }


    private Set<String> getMetadataTables() {
        return metadataColumnReferences.stream()
                .map(ColumnReference::table)
                .filter(c -> c.contains("metadata"))
                .collect(Collectors.toSet());
    }

    private Set<ColumnReference> getUniqueColumns() {
        Set<ColumnReference> uniqIdColumns = new HashSet<>();
        uniqIdColumns.addAll(matchingColumnFromMetadata( "metadata-patient.tsv", "patient_id"));
        uniqIdColumns.addAll(matchingColumnFromMetadata( "metadata-sharing.tsv", "model_id"));
        return uniqIdColumns;
    }

    private Map<Set<ColumnReference>, ValueRestrictions> getCategoricalRestrictions() {
        Set<ColumnReference> tumourTypeColumn = matchingColumnFromMetadata(
            "sample",
            "tumour_type");
        Set<ColumnReference> sexColumn = matchingColumnFromMetadata(
                "patient",
            "sex");
        ValueRestrictions sexCategories = ValueRestrictions.of(Arrays.asList(
                "male",
                "female",
                "other",
                NOTCOLLECTED,
                NOTPROVIDED
            ));
        Set<ColumnReference> shareColumn = matchingColumnFromMetadata(
            "sample",
            "sharable");
        ValueRestrictions shareColumnCategories = ValueRestrictions.of(Arrays.asList(
                "yes",
                "no",
                NOTPROVIDED
        ));
        Set<ColumnReference> treatmentNaiveAtCollectionColumn = matchingColumnFromMetadata(
            "sample",
            "treatment_naive_at_collection");
        ValueRestrictions treatementNaiveAtCollectionCategories = ValueRestrictions.of(Arrays.asList(
                "treatment naive" ,
                "not treatment naive",
                NOTCOLLECTED,
                NOTPROVIDED
        ));
        Set<ColumnReference> priorTreatmentColumn = matchingColumnFromMetadata(
            "sample",
            "prior_treatment");
        ValueRestrictions priorTreatmentCategories = ValueRestrictions.of(
            Arrays.asList(
                "yes",
                "no",
                NOTPROVIDED,
                NOTCOLLECTED));
        Set<ColumnReference> providerTypeColumn = matchingColumnFromMetadata(
            "sharing",
            "provider_type");
        ValueRestrictions providerTypeCategories = ValueRestrictions.of(
            Arrays.asList(
                "academia",
                "industry",
                "academia and industry",
                "CRO",
                "pharma"
            ));

        Map<Set<ColumnReference>, ValueRestrictions> categoricalRestrictions = new HashMap<>();
        categoricalRestrictions.put(tumourTypeColumn, tumourTypeCategories);
        categoricalRestrictions.put(sexColumn, sexCategories);
        categoricalRestrictions.put(shareColumn, shareColumnCategories);
        categoricalRestrictions.put(treatmentNaiveAtCollectionColumn, treatementNaiveAtCollectionCategories);
        categoricalRestrictions.put(priorTreatmentColumn,priorTreatmentCategories);
        categoricalRestrictions.put(providerTypeColumn, providerTypeCategories);
        return categoricalRestrictions;
    }

    private Set<ColumnReference> getFreeTextColumns() {
        Set<ColumnReference> patientFreeTextColumns = matchingColumnsFromMetadata( "patient",
                "history", "ethnicity", "initial_diagnosis" );
        Set<ColumnReference> sampleFreeTextColumns = matchingColumnsFromMetadata( "sample",
                "diagnosis" , "primary_site", "collection_site");
        Set<ColumnReference> modelFreeTextColumns = matchingColumnsFromMetadata( "model",
                "diagnosis" , "primary_site", "collection_site", "sample_type", "sample_state");
        Set<ColumnReference> modelValidationFreeTextColumns = matchingColumnsFromMetadata( "model_validation",
                "validation_technique", "description");
        Set<ColumnReference> sharingFreeTextColumns = matchingColumnsFromMetadata( "model_validation",
                "provider_abbreviation", "project");

        return TableSetUtilities.concatenate(
                patientFreeTextColumns,
                sampleFreeTextColumns,
                modelFreeTextColumns,
                modelValidationFreeTextColumns,
                sharingFreeTextColumns
        );
    }

    private Set<ColumnReference> getEssentialColumns() {
        Set<ColumnReference> essentialSampleColumns = matchingColumnsFromMetadata( "sample",
                "age_in_years", "diagnosis", "tumour_type", "primary_site", "collection_site");
        Set<ColumnReference> essentialModelColumns = matchingColumnsFromMetadata( "model",
                "host_strain_full", "engraftment_site","engraftment_type","sample_type", "passage_number");
        Set<ColumnReference> essentialModelValidationColumns = matchingColumnsFromMetadata( "model_validation",
                "validation_technique", "description", "passages_tested", "validation_host_strain_full");
        Set<ColumnReference> essentialSharingColumns = matchingColumnsFromMetadata( "sharing",
                "provider_type", "accessibility", "email", "name", "provider_name", "provider_abbreviation", "project");
        Set<ColumnReference> essentialLoaderColumns = matchingColumnsFromMetadata( "loader",
                "name", "abbreviation", "internal_url");

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
