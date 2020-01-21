package org.pdxfinder.constants;

public enum Option {

    loadMarkers,                                 // Description: Create Markers
    reloadCache,                                 // Description: Catches Markers and Ontologies
    loadALL,                                     // Description: Load all, including creating markers  // Load all, including NCiT drug ontology  // Load all including creating markers
    loadEssentials,                              // Description: Loading essentials
    loadNCIT,                                    // Description: Load NCIT all ontology
    loadNCITPreDef,                              // Description: Load predefined NCIT ontology
    loadNCITDrugs,                               // Description: Load NCIT drugs
    loadSlim;                                    // Description: Load slim then link samples to NCIT terms
    
    public String get() {
        return name();
    }

}

