package org.pdxfinder.dataloaders.updog.validation;

import org.pdxfinder.dataloaders.updog.TableSetUtilities;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class OmicValidationRuleset extends ValidationRuleCreator {

    private Set<ColumnReference> createColumns(String tableName) {
        Set<ColumnReference> columnReferences = new HashSet<>();
        if (tableName.contains("cna")) {
            Arrays.asList(
                "model_id",
                "sample_id",
                "sample_origin",
                "passage",
                "host_strain_nomenclature",
                "chromosome",
                "seq_start_position",
                "seq_end_position",
                "symbol",
                "ucsc_gene_id",
                "ncbi_gene_id",
                "ensembl_gene_id",
                "log10r_cna",
                "log2r_cna",
                "fold_change",
                "copy_number_status",
                "gistic_value",
                "picnic_value",
                "genome_assembly"
            ).forEach(s -> columnReferences.add(ColumnReference.of(tableName, s)));
        }
        if (tableName.contains("mut")) {
            Arrays.asList(
                "model_id",
                "sample_id",
                "sample_origin",
                "host_strain_nomenclature",
                "passage",
                "symbol",
                "biotype",
                "coding_sequence_change",
                "variant_class",
                "codon_change",
                "amino_acid_change",
                "consequence",
                "functional_prediction",
                "read_depth",
                "allele_frequency",
                "chromosome",
                "seq_start_position",
                "ref_allele",
                "alt_allele",
                "ucsc_gene_id",
                "ncbi_gene_id",
                "ncbi_transcript_id",
                "ensemble_gene_id",
                "ensemble_transcript_id",
                "variation_id",
                "genome_assembly",
                "platform"
            ).forEach(s -> columnReferences.add(ColumnReference.of(tableName, s)));
        }
        if (tableName.contains("cyto")) {
            Arrays.asList(
                "sample_id",
                "sample_origin",
                "passage",
                "host_strain_nomenclature",
                "model_id",
                "marker_name",
                "marker_status",
                "essential_or_additional_marker",
                "platform",
                "protocol_file_name",
                "result_file_name"
            ).forEach(s -> columnReferences.add(ColumnReference.of(tableName, s)));
        }
        return columnReferences;
    }

    @Override
    public TableSetSpecification generate(String provider){
        return TableSetSpecification.create();
    }

    public TableSetSpecification generateForOmicTable(String tableName, String provider) {
        Set<ColumnReference> tableColumns = createColumns(tableName);

        Set<ColumnReference> modelColumns = matchingColumnsFromAnyTable(tableColumns, "model_id");
        Set<ColumnReference> sampleColumns = matchingColumnsFromAnyTable(tableColumns, "sample_");
        Set<ColumnReference> hostStrainColumns = matchingColumnsFromAnyTable(tableColumns, "host_strain");
        Set<ColumnReference> passageColumns = matchingColumnsFromAnyTable(tableColumns, "passage");
        Set<ColumnReference> symbolColumns = matchingColumnsFromAnyTable(tableColumns, "symbol");

        Set<ColumnReference> essentialCytogeneticsColumns = matchingColumnsFromTable(tableColumns, "cyto",
            new String[]{"marker_", "essential_or_additional_marker", "platform"});

        Set<ColumnReference> essentialColumns = TableSetUtilities.concatenate(
            modelColumns,
            sampleColumns,
            hostStrainColumns,
            passageColumns,
            symbolColumns,
            essentialCytogeneticsColumns
        );

        return TableSetSpecification.create()
            .addRequiredColumns(essentialColumns)
            .addNonEmptyColumns(essentialColumns)
            .setProvider(provider);
    }

}
