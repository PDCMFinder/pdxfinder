package org.pdxfinder.services;

import org.pdxfinder.graph.dao.Marker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        String fileOut = "ncbiToHugoAccesions";

        FileWriter fstream = null;
        try {
            fstream = new FileWriter(fileOut, true);

            try(BufferedWriter out = new BufferedWriter(fstream)) {
                BufferedWriter finalOut = out;
                geneList.forEach(g ->
                        {
                            try {
                                String conversionRow = String.format("%s\t%s\n", g, ncbiGeneIdToHgncSymbol(g));
                                finalOut.write(conversionRow);
                            } catch (IOException e) {
                                log.error("Exception in conversion ",e);
                            }
                        }
                );
                out.flush();
            }
            catch(Exception e){
                log.error("Bufferedwriter exception ", e);
            }
        } catch(IOException e){
            log.error("Failure opening output file %n {}", e.toString());
        }
        finally{
            try {
                if (fstream != null) fstream.close();
            } catch (IOException ex) {
                log.error("Exception ", ex);
            }
        }
    }

    public String ncbiGeneIdToHgncSymbol(String ncbiGene) {
        String hgncSymbol = "";
        if (!ncbiGene.isEmpty()) {
            hgncSymbol = geneIdCache.get(ncbiGene);
            if (hgncSymbol == null) {
                Marker marker = dataImportService.getMarkerbyNcbiGeneId(ncbiGene);
                if (marker != null && marker.hasHgncSymbol()) {
                    hgncSymbol = marker.getHgncSymbol();
                    geneIdCache.put(ncbiGene, hgncSymbol);
                } else {
                    log.warn("No marker found for NCBI gene Id {} Cannot generate Hgnc symbol", ncbiGene);
                }
            }
        }
        return hgncSymbol;
    }

}
