package org.pdxfinder;

public class TSV {

    public enum Metadata {
        patient_id,
        model_id,
        sample_id,
        provider_group,
        sex,
        ethnicity,
        history,
        initial_diagnosis,
        age_at_initial_diagnosis,
        name,
        abbreviation,
        internal_url,
        collection_date,
        age_in_years_at_collection,
        collection_event,
        months_since_collection_1,
        diagnosis,
        tumour_type,
        primary_site,
        collection_site,
        stage,
        staging_system,
        grade,
        grading_system,
        virology_status,
        sharable,
        treatment_naive_at_collection,
        treated,
        prior_treatment,
        host_strain,
        host_strain_full,
        engraftment_site,
        engraftment_type,
        sample_type,
        sample_state,
        passage_number,
        publications,
        validation_technique,
        description,
        passages_tested,
        validation_host_strain_full,
        provider_name,
        provider_abbreviation,
        project,
        provider_type,
        accessibility,
        europdx_access_modality,
        email,
        form_url,
        database_url
    }

    public enum Mutation{
        model_id,
        sample_id,
        sample_origin,
        host_strain_nomenclature,
        passage,
        symbol,
        biotype,
        coding_sequence_change,
        variant_class,
        codon_change,
        amino_acid_change,
        consequence,
        functional_prediction,
        read_depth,
        allele_frequency,
        chromosome,
        seq_start_position,
        ref_allele,
        alt_allele,
        ucsc_gene_id,
        ncbi_gene_id,
        ncbi_transcript_id,
        ensembl_gene_id,
        ensembl_transcript_id,
        variation_id,
        genome_assembly,
        platform
    }

    public enum CopyNumberAlteration{
        model_id,
        sample_id,
        sample_origin,
        passage,
        host_strain_nomenclature,
        chromosome,
        seq_start_position,
        seq_end_position,
        symbol,
        ucsc_gene_id,
        ncbi_gene_id,
        ensembl_gene_id,
        log10r_cna,
        log2r_cna,
        fold_change,
        copy_number_status,
        gistic_value,
        picnic_value,
        genome_assembly,
        platform

    }

    public enum Expression{
        model_id,
        sample_id,
        sample_origin,
        passage,
        host_strain_nomenclature,
        chromosome,
        strand,
        seq_start_position,
        seq_end_position,
        symbol,
        ucsc_gene_id,
        ensembl_gene_id,
        ensembl_transcript_id,
        rnaseq_coverage,
        rnaseq_fpkm,
        rnaseq_tpm,
        rnaseq_count,
        affy_hgea_probe_id,
        affy_hgea_expression_value,
        illumina_hgea_probe_id,
        illumina_hgea_expression_value,
        z_score,
        genome_assembly,
        platform

    }

    public enum SamplePlatform{

        sample_id,
        sample_origin,
        passage,
        engrafted_tumor_collection_site,
        model_id,
        host_strain_name,
        host_strain_nomenclature,
        molecular_characterisation_type,
        platform,
        raw_data_file,
        internal_protocol_url

    }

    public enum Treatment{

        patient_id,
        treatment_name,
        treatment_dose,
        treatment_starting_date,
        treatment_duration,
        treatment_event,
        elapsed_time,
        treatment_response,
        response_classification,
        model_id
    }

    public enum providerFileNames{
        metadata,
        sampleplatform,
        cna,
        mut,
        expression,
        patienttreatment,
        cytogenetics,
        drugdosing
    }


    public enum metadataSheetNames{
        checklist,
        patient,
        sample,
        model,
        model_validation,
        sharing,
        loader
    }

    //Sample Platform sheet, these are the internal strings in the DB
    public enum molecular_characterisation_type{

        cyto("cytogenetics"),
        expression("expression"),
        mut("mutation"),
        cna("copy number alteration");

        public final String mcType;

        molecular_characterisation_type(String mcType) { this.mcType = mcType; }
    }

    public enum numberOfMetadataSheets{
        numberSheets(6);
        public final int count;
        numberOfMetadataSheets(int count) {
            this.count = count;
        }
    }

    public enum templateNames{
        metadata_template("metadata_template.xlsx"),
        sampleplatform_template("sampleplatform_template.xlsx"),
        patienttreatment_template("patienttreatment_template.xlsx"),
        cna_template("cna_template.xlsx"),
        expression_template("expression_template.xlsx"),
        cytogenetics_template("cytogenetics_template.xlsx"),
        mutation_template("mutation_template.xlsx"),
        drugdosing_template("drugdosing_template.xlsx");

        public final String fileName;

        templateNames(String fileName) {
            this.fileName = fileName;
        }

    }

}