package org.pdxfinder.services.loader.envload;

import org.pdxfinder.graph.dao.Marker;
import org.pdxfinder.services.DataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;


@Service
public class LoadMarkers {

    Logger log = LoggerFactory.getLogger(LoadMarkers.class);

    private DataImportService dataImportService;

    public LoadMarkers(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }


    public void loadGenes(String dataURL) {

        long startTime = System.currentTimeMillis();

        log.info("************************* Creating Markers ***************************** ");

        String[] rowData = new String[0];
        String[] prevSymbols;
        String[] synonyms;

        String symbol;
        String hgncId;
        String ensemblId;
        String ncbiId;


        int rows = 0;

        try {

            URL url = new URL(dataURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                log.error("Failed : HTTP error code : {}", conn.getResponseCode());
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String line;
            while ((line = reader.readLine()) != null) {
                //HGNC_ID APPR_SYMBOL PREV_SYMBOLS SYNONYMS ENTREZ_ID ENSEMBL_ID
                //HGNC ID[0]	Approved symbol[1]	Approved name[2]	Status[3]	Previous symbols[4]	Synonyms[5]	Accession numbers[6]	RefSeq IDs[7]	Name synonyms[8]	Ensembl gene ID[9]
                rowData = line.split("\t");
                //rowData[1] = symbol, if it is not empty and if it is not a withdrawn symbol
                if (rowData[1] != null && !rowData[1].isEmpty()) {

                    symbol = rowData[1];

                    if (rowData[0] != null && !rowData[0].isEmpty()) {
                        hgncId = rowData[0];
                    } else {
                        hgncId = "";
                    }

                    if (rowData.length > 4 && rowData[4] != null && !rowData[4].isEmpty()) {
                        prevSymbols = rowData[4].split(", ");
                    } else {
                        prevSymbols = new String[0];
                    }

                    if (rowData.length > 5 && rowData[5] != null && !rowData[5].isEmpty()) {
                        synonyms = rowData[5].split(", ");
                    } else {
                        synonyms = new String[0];
                    }

                    if (rowData.length > 7 && rowData[7] != null && !rowData[7].isEmpty()) {
                        ncbiId= rowData[7];
                    } else {
                        ncbiId = "";
                    }

                    if (rowData.length > 9 && rowData[9] != null && !rowData[9].isEmpty()) {
                        ensemblId = rowData[9];
                    } else {
                        ensemblId = "";
                    }

                    //put it in the hashmap with all of its prev symbols

                    Marker m = new Marker();
                    m.setHgncSymbol(symbol);

                    if (!ensemblId.isEmpty()) {
                        m.setEnsemblGeneId(ensemblId);
                    }
                    if (!hgncId.isEmpty()) {
                        m.setHgncId(hgncId);
                    }

                    if (!ncbiId.isEmpty())
                        m.setNcbiGeneId(ncbiId);

                    if (synonyms.length > 0) {

                        m.setAliasSymbols(new HashSet<>(Arrays.asList(synonyms)));
                    }

                    if (prevSymbols.length > 0) {

                        m.setPrevSymbols(new HashSet<>(Arrays.asList(prevSymbols)));
                    }

                    dataImportService.saveMarker(m);

                    if (rows != 0 && rows % 200 == 0) log.info("Loaded {} markers", rows);

                }

                rows++;
            }

        } catch (Exception e) {
            log.warn(e.getMessage());
            log.error(Arrays.toString(rowData));
        }

        log.info("******************************************************");
        log.info("* Finished creating markers                          *");
        log.info("******************************************************");


        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int seconds = (int) (totalTime / 1000) % 60;
        int minutes = (int) ((totalTime / (1000 * 60)) % 60);

        log.info(" {} finished after {} minute(s) and {} second(s)", this.getClass().getSimpleName(), minutes, seconds);

    }
}
