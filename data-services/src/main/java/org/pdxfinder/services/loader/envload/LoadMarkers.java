package org.pdxfinder.services.loader.envload;

import org.pdxfinder.graph.dao.Marker;
import org.pdxfinder.services.DataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
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
        String line;
        BufferedReader reader = downloadDataFromURL(dataURL);
        try {
            while ((line = reader.readLine()) != null) {
                //HGNC_ID APPR_SYMBOL PREV_SYMBOLS SYNONYMS ENTREZ_ID ENSEMBL_ID
                //HGNC ID[0]	Approved symbol[1]	Approved name[2]	Status[3]	Previous symbols[4]	Synonyms[5]
                // Accession numbers[6]	RefSeq IDs[7]	Name synonyms[8]	Ensembl gene ID[9] NcbiGeneId[10]
                rowData = line.split("\t");
                symbol = parseHugoFile(rowData, 1);
                if (!symbol.equals("")) {

                    hgncId = parseHugoFile(rowData, 0);
                    prevSymbols = parseHugoFile(rowData, 4).split(",");
                    synonyms = parseHugoFile(rowData, 5).split(",");
                    ensemblId = parseHugoFile(rowData, 9);
                    ncbiId = parseHugoFile(rowData, 10);

                    Marker m = createMarker(symbol, ensemblId, hgncId, ncbiId, synonyms, prevSymbols);
                    dataImportService.saveMarker(m);
                    if (rows != 0 && rows % 200 == 0) log.info("Loaded {} markers", rows);
                }
                rows++;
            }
        } catch (Exception e) {
            log.error("{} %n Error wall parsing HGNC file", e.getMessage());
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

    private BufferedReader downloadDataFromURL(String dataURL){
        BufferedReader reader = null;
        try{
            URL url = new URL(dataURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                log.error("Failed : HTTP error code : {}", conn.getResponseCode());
            }
            reader = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reader;
    }

    private Marker createMarker(String symbol, String ensemblId, String hgncId, String ncbiId, String[] synonyms, String[] prevSymbols) {
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
        return m;
    }

    private String parseHugoFile(String[] rowData, int column){
        String cellValue = "";
        if (cellIsNotEmptyAndIsCorrectLen(rowData, column)) {
            cellValue = rowData[column];
        }
        return cellValue;
    }

    private boolean cellIsNotEmptyAndIsCorrectLen(String[] rowData, int column){
        return rowData.length > column && rowData[column] != null && !rowData[column].isEmpty();
    }

}
