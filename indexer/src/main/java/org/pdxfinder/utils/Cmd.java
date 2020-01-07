package org.pdxfinder.utils;

public enum Cmd {

    loadMarkers,                                // Create Markers
    Create_Markers,                             // Create Markers Description

    reloadCache,                                // Catches Markers and Ontologies
    Catches_Markers_and_Ontologies,

    loadALL,                                    // Load all, including creating markers
    Load_all_including_creating_markers,
    Load_all_including_NCiT_ontology,
    Load_all_including_NCiT_drug_ontology,      // Loan Interest Rate

    loadEssentials,                             // Loading essentials
    Loading_essentials,

    loadNCIT,                                   // Loan Fee
    Load_NCIT_all_ontology,                     // Loan Application Fee

    loadNCITPreDef,                             // Loan Default Fine
    Load_predefined_NCIT_ontology,

    loadSlim,                                   // Loan Insurance
    Load_slim_then_link_samples_to_NCIT_terms;  // Loan Stationary
    
    public String get() {
        return name();
    }

}

