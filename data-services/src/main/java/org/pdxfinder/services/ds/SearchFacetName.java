package org.pdxfinder.services.ds;

public enum SearchFacetName {
    query,
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
    system,
    cell_type;

    public String getName() {
        return name();
    }
}
