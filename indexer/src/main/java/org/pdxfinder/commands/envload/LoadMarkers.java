package org.pdxfinder.commands.envload;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.pdxfinder.graph.dao.Marker;
import org.pdxfinder.services.DataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/*
 * Created by csaba on 26/02/2019.
 */
@Component
@Order(value = -70)
public class LoadMarkers implements CommandLineRunner{


    //private static final String DATA_FILE_URL = "http://www.genenames.org/cgi-bin/download?col=gd_hgnc_id&col=gd_app_sym&col=gd_prev_sym&col=gd_aliases&col=md_eg_id&col=md_ensembl_id&status=Approved&status_opt=2&where=&order_by=gd_app_sym_sort&format=text&limit=&hgnc_dbtag=on&submit=submit";
    private static final String DATA_FILE_URL = "https://www.genenames.org/cgi-bin/download/custom?col=gd_hgnc_id&col=gd_app_sym&col=gd_app_name&col=gd_status&col=gd_prev_sym&col=gd_aliases&col=gd_pub_acc_ids&col=gd_pub_refseq_ids&col=gd_name_aliases&col=gd_pub_ensembl_id&status=Approved&hgnc_dbtag=on&order_by=gd_app_sym_sort&format=text&submit=submit";
    private final static Logger log = LoggerFactory.getLogger(LoadMarkers.class);
    private DataImportService dataImportService;

    @Autowired
    public LoadMarkers(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadMarkers", "Create Markers");
        parser.accepts("loadALL", "Load all, including creating markers");
        parser.accepts("loadEssentials", "Loading essentials");
        OptionSet options = parser.parse(args);

        long startTime = System.currentTimeMillis();

        if (options.has("loadMarkers") || options.has("loadALL") || options.has("loadEssentials")) {


            log.info("******************************************************");
            log.info("* Creating Markers                                   *");
            log.info("******************************************************");

            loadMarkers();

            log.info("******************************************************");
            log.info("* Finished creating markers                          *");
            log.info("******************************************************");
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int seconds = (int) (totalTime / 1000) % 60;
        int minutes = (int) ((totalTime / (1000 * 60)) % 60);

        log.info(this.getClass().getSimpleName() + " finished after " + minutes + " minute(s) and " + seconds + " second(s)");

    }






    private void loadMarkers(){


        String[] rowData = new String[0];
        String[] prevSymbols;
        String[] synonyms;

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
                //HGNC ID[0]	Approved symbol[1]	Approved name[2]	Status[3]	Previous symbols[4]	Synonyms[5]	Accession numbers[6]	RefSeq IDs[7]	Name synonyms[8]	Ensembl gene ID[9]
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

                    if(rowData.length>4 && rowData[4] != null && !rowData[4].isEmpty()){
                        prevSymbols = rowData[4].split(", ");
                    }
                    else{
                        prevSymbols= new String[0];
                    }

                    if(rowData.length>5 && rowData[5] != null && !rowData[5].isEmpty()){
                        synonyms = rowData[5].split(", ");
                    }
                    else{
                        synonyms = new String[0];
                    }
/*
                    if(rowData.length>4 && rowData[4] != null && !rowData[4].isEmpty()){
                        entrezId = rowData[4];
                    }
                    else{
                        entrezId = "";
                    }
*/
                    if(rowData.length>9 && rowData[9] != null && !rowData[9].isEmpty()){
                        ensemblId = rowData[9];
                    }
                    else{
                        ensemblId = "";
                    }

                    //put it in the hashmap with all of its prev symbols


                    if(!symbol.isEmpty()){

                        Marker m = new Marker();
                        m.setHgncSymbol(symbol);

                        if(!ensemblId.isEmpty()){
                            m.setEnsemblGeneId(ensemblId);
                        }
                        if(!hgncId.isEmpty()){
                            m.setHgncId(hgncId);
                        }

                        if(synonyms.length>0){

                            m.setAliasSymbols(new HashSet<>(Arrays.asList(synonyms)));
                        }

                        if(prevSymbols.length>0){

                            m.setPrevSymbols(new HashSet<>(Arrays.asList(prevSymbols)));
                        }

                        dataImportService.saveMarker(m);

                    }
                    else{
                        log.error("Empty symbol found in row "+rows);
                    }

                    if(rows !=0 && rows%200 == 0) log.info("Loaded "+rows +" markers");



                }

                rows++;
            }

        }
        catch (Exception e){
            e.printStackTrace();
            log.error(rowData.toString());
        }



    }
}
