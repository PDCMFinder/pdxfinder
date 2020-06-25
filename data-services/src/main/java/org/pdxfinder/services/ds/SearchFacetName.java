package org.pdxfinder.services.ds;

public enum SearchFacetName {
    query,
    diagnosis,
    datasource,
    patient_age,
    patient_treatment_status,
    patient_gender,
    sample_origin_tissue,
    sample_classification,
    sample_tumor_type,
    model_implantation_site,
    model_implantation_type,
    model_host_strain,
    organ,
    cancer_system,
    cell_type,
    mutation,
    drug,
    project,
    data_available,
    breast_cancer_markers,
    access_modalities,
    copy_number_alteration,
    patient_treatment,
    gene_expression,
    cytogenetics;

    public String getName() {
        return name();
    }
}
