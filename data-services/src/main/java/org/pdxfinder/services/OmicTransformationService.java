package org.pdxfinder.services;

import org.pdxfinder.graph.dao.Marker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
public class OmicTransformationService {

    private final static Logger log = LoggerFactory.getLogger(OmicTransformationService.class);

    private DataImportService dataImportService;
    private Map<String, String> geneIdCache = new HashMap<>();

    @Autowired
    public OmicTransformationService(DataImportService dataImportService){
        this.dataImportService = dataImportService;
    }

    public String ncbiGeneIdtoHgncSymbol(String ncbiGene) {
        String hgncSymbol = geneIdCache.get(ncbiGene);
        if (hgncSymbol == null) {
            Marker marker = dataImportService.getMarkerbyNcbiGeneId(hgncSymbol);

            if (marker != null && marker.getHgncSymbol() != null && !marker.getHgncSymbol().isEmpty()) {
                hgncSymbol = marker.getHgncSymbol();
                geneIdCache.put(ncbiGene, hgncSymbol);
            } else { log.warn(String.format("No marker found for NCBI gene Id %s Cannot generate Hgnc symbol", ncbiGene)); }
        }
        return hgncSymbol;
    }
}
