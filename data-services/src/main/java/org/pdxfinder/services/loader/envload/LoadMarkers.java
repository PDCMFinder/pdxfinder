package org.pdxfinder.services.loader.envload;

import org.apache.commons.lang3.StringUtils;
import org.pdxfinder.graph.dao.Marker;
import org.pdxfinder.services.DataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.*;
import java.util.*;

@Service
public class LoadMarkers {

    private Logger log = LoggerFactory.getLogger(LoadMarkers.class);

    private DataImportService dataImportService;

    public LoadMarkers(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    public void loadGenes(String dataURL) {
        Instant start = Instant.now();

        BufferedReader reader = downloadDataFromURL(dataURL);
        List<Marker> markers = parseMarkers(reader);
        dataImportService.saveAllMarkers(markers);

        Instant finish = Instant.now();
        log.info("{} markers downloaded and saved in {} seconds",
            markers.size(),
            Duration.between(start, finish).getSeconds());
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
            log.error("There was an error attempting to connect to {}", dataURL, e);
        }
        return reader;
    }

    private List<Marker> parseMarkers(BufferedReader reader) {
        List<Marker> markers = new ArrayList<>();
        int rowNumber = 0;

        String line;
        String[] rowData = new String[0];
        String[] prevSymbols;
        String[] synonyms;

        String symbol;
        String hgncId;
        String ensemblId;
        String ncbiId;

        if (reader == null)
            throw new IllegalArgumentException("Buffered Reader cannot be null");

        try {
            while ((line = reader.readLine()) != null) {
                rowNumber ++;
                if (rowNumber == 1) continue; // Skip header

                rowData = line.split("\t");
                symbol = parseHugoFile(rowData, 1);
                if (StringUtils.isNotEmpty(symbol)) {

                    hgncId = parseHugoFile(rowData, 0);
                    prevSymbols = parseHugoFile(rowData, 4).split(",");
                    synonyms = parseHugoFile(rowData, 5).split(",");
                    ensemblId = parseHugoFile(rowData, 9);
                    ncbiId = parseHugoFile(rowData, 10);

                    markers.add(Marker.createMarker(symbol, ensemblId, hgncId, ncbiId, synonyms, prevSymbols));
                }
            }
        } catch (Exception e) {
            log.error("Error parsing HGNC file for row {}", Arrays.toString(rowData), e);
        }

        return markers;
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
