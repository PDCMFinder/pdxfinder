package org.pdxfinder.services;

import org.pdxfinder.graph.dao.Marker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class OmicTransformationService {

    private static final Logger log = LoggerFactory.getLogger(OmicTransformationService.class);
    private DataImportService dataImportService;

    @Autowired
    OmicTransformationService(DataImportService dataImportService){
        this.dataImportService = dataImportService;
    }

    private Map<String, String> geneIdCache = new HashMap<>();

    public void convertListOfNcbiToHgnc(List<String> geneList){
        BufferedWriter out;
        try {
            FileWriter fstream = new FileWriter("ncbiToHugoAccesions", true);
            out = new BufferedWriter(fstream);
            BufferedWriter finalOut = out;
            geneList.forEach(g ->
                    {
                        try {
                            finalOut.write(ncbiGeneIdtoHgncSymbol(g));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            );
        } catch(IOException e){
            log.error("Failure opening output file %n {}", e.toString());
        }
    }

    public String ncbiGeneIdtoHgncSymbol(String ncbiGene) {
        String hgncSymbol = geneIdCache.get(ncbiGene);
        if (hgncSymbol == null) {
            Marker marker = dataImportService.getMarkerbyNcbiGeneId(ncbiGene);
            if (isMarkerSymbolNullOrEmpty(marker)) {
                hgncSymbol = marker.getHgncSymbol();
                geneIdCache.put(ncbiGene, hgncSymbol);
            } else { log.warn("No marker found for NCBI gene Id {} Cannot generate Hgnc symbol", ncbiGene); }
        }
        return hgncSymbol;
    }

    private Boolean isMarkerSymbolNullOrEmpty(Marker marker){
        return marker.getHgncSymbol() != null && !marker.getHgncSymbol().isEmpty();
    }
}
