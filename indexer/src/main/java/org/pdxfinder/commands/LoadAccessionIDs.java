package org.pdxfinder.commands;


import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.pdxfinder.accessionidtatamodel.AccessionData;
import org.pdxfinder.graph.dao.Marker;
import org.pdxfinder.services.DataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


/**
 * Created by csaba on 30/06/2017.
 */
@Component
@Order(value = 100)
public class LoadAccessionIDs implements CommandLineRunner {

    private static final String HGNC_URL = "http://rest.genenames.org/fetch/symbol/";
    private static final String ENSEMBL_URL = "http://rest.ensembl.org/xrefs/symbol/homo_sapiens/BRAF?content-type=application/json";
    private static final String DATA_FILE_URL = "http://www.genenames.org/cgi-bin/download?col=gd_hgnc_id&col=gd_app_sym&col=gd_prev_sym&col=gd_aliases&col=md_eg_id&col=md_ensembl_id&status=Approved&status_opt=2&where=&order_by=gd_app_sym_sort&format=text&limit=&hgnc_dbtag=on&submit=submit";


    private final static Logger log = LoggerFactory.getLogger(LoadAccessionIDs.class);
    private DataImportService dataImportService;

    @Autowired
    public LoadAccessionIDs(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }



    @Override
    @Transactional
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadAccessionIds", "Load Accession IDs");
        //parser.accepts("loadALL", "Load all, including Accession IDs");
        OptionSet options = parser.parse(args);

        long startTime = System.currentTimeMillis();

        if (options.has("loadAccessionIds") || options.has("loadALL")) {
            loadAccessionIds();
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int seconds = (int) (totalTime / 1000) % 60;
        int minutes = (int) ((totalTime / (1000 * 60)) % 60);

        log.info(this.getClass().getSimpleName() + " finished after " + minutes + " minute(s) and " + seconds + " second(s)");

    }


    private void loadAccessionIds(){

        String[] rowData;
        String[] prevSymbols;
        String[] synonyms;
        HashMap<String, AccessionData> hmap = new HashMap<>();
        String symbol, hgncId, entrezId, ensemblId = "";

        int rows = 0;
        int symbolConflicts = 0;
        String symbolConf = "";

        try{

            URL url = new URL(DATA_FILE_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            //conn.addRequestProperty("Enctype","application/x-www-form-urlencoded");

            if(conn.getResponseCode() != 200){
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String line = reader.readLine();
                while((line = reader.readLine()) != null){
                    //HGNC_ID APPR_SYMBOL PREV_SYMBOLS SYNONYMS ENTREZ_ID ENSEMBL_ID

                    rowData = line.split("\t");
                    //rowData[1] = symbol, if it is not empty and if it is not a withdrawn symbol
                    if(rowData[1] != null && !rowData[1].isEmpty() ){

                        symbol = rowData[1];

                        if(rowData[0] != null && !rowData[0].isEmpty()){
                            hgncId = rowData[0];
                        }
                        else{
                            hgncId = "";
                        }

                        if(rowData.length>2 && rowData[2] != null && !rowData[2].isEmpty()){
                            prevSymbols = rowData[2].split(", ");
                        }
                        else{
                            prevSymbols= new String[0];
                        }

                        if(rowData.length>3 && rowData[3] != null && !rowData[3].isEmpty()){
                            synonyms = rowData[3].split(", ");
                        }
                        else{
                            synonyms = new String[0];
                        }

                        if(rowData.length>4 && rowData[4] != null && !rowData[4].isEmpty()){
                            entrezId = rowData[4];
                        }
                        else{
                            entrezId = "";
                        }

                        if(rowData.length>5 && rowData[5] != null && !rowData[5].isEmpty()){
                            ensemblId = rowData[5];
                        }
                        else{
                            ensemblId = "";
                        }

                        //put it in the hashmap with all of its prev symbols

                        AccessionData ad = new AccessionData(symbol, hgncId, entrezId, ensemblId);

                        if(synonyms.length>0){
                            for(int i=0;i<synonyms.length;i++)
                            ad.addSynonym(synonyms[i]);
                        }

                        if(prevSymbols.length>0){
                            for(int i=0;i<prevSymbols.length;i++)
                                ad.addPrevSymbol(prevSymbols[i]);
                        }

                        System.out.println(symbol+" "+hgncId+" "+entrezId+" "+ ensemblId);
                        hmap.put(symbol,ad);

                    }
                    rows++;
                }

        }
        catch (Exception e){
            e.printStackTrace();
        }

        System.out.println("Symbol conflicts: "+symbolConflicts);
        System.out.println("Last conflict:"+symbolConf);
        System.out.println("Rows: "+rows);
        System.out.println("Symbols incl. prev symbols: "+hmap.size());

        updateMarkers(hmap);
    }

private void updateMarkers(HashMap hmap){

    int updatedMarkers = 0;
    List<String> notUpdatedMarkers = new ArrayList<>();
    List<String> entrezIdMismatch = new ArrayList<>();

    System.out.println("Loading all markers from Neo4j...");
    Collection<Marker> markers = dataImportService.getAllHumanMarkers();
    System.out.println(markers.size()+" markers were loaded from Neo4j.");
    for(Marker m:markers){

        if(hmap.containsKey(m.getHgncSymbol())){
            AccessionData ad = (AccessionData) hmap.get(m.getHgncSymbol());

            if(m.getNcbiGeneId().equals(ad.getEntrezId()) ){
                m.setHgncId(ad.getHgncId());
                m.setEnsemblGeneId(ad.getEnsemblId());

                if(ad.getPrevSymbols().size()>0){
                    for(String ps:ad.getPrevSymbols()){
                        m.addPrevSymbol(ps);
                    }
                }

                if(ad.getSynonyms().size()>0){
                    for(String s:ad.getSynonyms()){
                        m.addAliasSymbols(s);
                    }
                }
                updatedMarkers++;
                System.out.println("Updating marker:"+m.getHgncSymbol());
                dataImportService.saveMarker(m);
            }
            else{
                entrezIdMismatch.add(m.getHgncSymbol());
            }



        }
        else{
            notUpdatedMarkers.add(m.getHgncSymbol());
        }

    }
    System.out.println("Updated markers: "+updatedMarkers);
    System.out.println("Not updated markers: "+notUpdatedMarkers);
    System.out.println("EntrezId mismatch: "+entrezIdMismatch);


}



}
