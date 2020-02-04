package org.pdxfinder.services.constants;

public enum DataUrl {


    //http://www.ebi.ac.uk/ols/api/ontologies/ncit/terms/http%253A%252F%252Fpurl.obolibrary.org%252Fobo%252FNCIT_C7057/hierarchicalChildren
    //http://www.ebi.ac.uk/ols/api/ontologies/doid/terms/http%253A%252F%252Fpurl.obolibrary.org%252Fobo%252FNCIT_C7057/hierarchicalChildren

    HUGO_FILE_URL("https://www.genenames.org/cgi-bin/download/custom?col=gd_hgnc_id&col=gd_app_sym&col=gd_app_name&col=gd_status&col=gd_prev_sym&col=gd_aliases&col=gd_pub_acc_ids&col=gd_pub_refseq_ids&col=gd_name_aliases&col=gd_pub_ensembl_id&status=Approved&hgnc_dbtag=on&order_by=gd_app_sym_sort&format=text&submit=submit"),
    DISEASES_BRANCH_URL("http://purl.obolibrary.org/obo/NCIT_C3262"),
    ONTOLOGY_URL("https://www.ebi.ac.uk/ols/api/ontologies/ncit/terms/");

    private String value;

    private DataUrl(String val) {
        value = val;
    }

    public String get() {
        return value;
    }

}
