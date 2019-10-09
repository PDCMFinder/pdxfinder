package org.pdxfinder;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.accessionidtatamodel.AccessionData;
import org.pdxfinder.graph.dao.ModelCreation;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Created by csaba on 21/08/2017.
 */
@Component
@Order(value = 200)
public class ValidateGeneSymbols implements CommandLineRunner{


    private static final String DATA_FILE_URL = "http://www.genenames.org/cgi-bin/download?col=gd_hgnc_id&col=gd_app_sym&col=gd_prev_sym&col=gd_aliases&col=md_eg_id&col=md_ensembl_id&status=Approved&status_opt=2&where=&order_by=gd_app_sym_sort&format=text&limit=&hgnc_dbtag=on&submit=submit";


    private final static Logger log = LoggerFactory.getLogger(ValidateGeneSymbols.class);
    private DataImportService dataImportService;
    private Set<String> cnvSymbols;
    private Set<String> rnaseqSymbols;
    private List<String> cnvSymbolsWithIssues;
    private List<String> rnaseqSymbolsWithIssues;

    @Autowired
    private UtilityService utilityService;

    @Value("${jaxpdx.cnv.url}")
    private String cnvURL;

    @Value("${jadpdx.rnaseq.url}")
    private String rnaSeqURL;

    @Autowired
    public ValidateGeneSymbols(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
        this.cnvSymbols = new HashSet<>();
        this.rnaseqSymbols = new HashSet<>();
        this.cnvSymbolsWithIssues = new ArrayList<>();
        this.rnaseqSymbolsWithIssues = new ArrayList<>();
    }

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("validateGeneSymbols", "Validate gene symbols");
        parser.accepts("loadALL", "Load all, including validating gene symbols");
        OptionSet options = parser.parse(args);

        long startTime = System.currentTimeMillis();

        if (options.has("validateGeneSymbols")) {

            validateSymbols();

        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int seconds = (int) (totalTime / 1000) % 60;
        int minutes = (int) ((totalTime / (1000 * 60)) % 60);

        log.info(this.getClass().getSimpleName() + " finished after " + minutes + " minute(s) and " + seconds + " second(s)");

    }


    private HashMap<String, AccessionData> loadHugoDatabase(){

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

        return hmap;
    }


    private void getCnvDataFromURL(String modelId){

        String json = utilityService.parseURL(cnvURL+modelId);

        JSONObject job = null;
        try {
            log.info("Loading CNV: "+modelId);
            job = new JSONObject(json);
            JSONObject pdxCNV = job.getJSONObject("pdxCNV");
            JSONArray samples = pdxCNV.getJSONArray("samples");

            if(samples.length()>0){
                JSONObject cnvObj = samples.getJSONObject(0);
                JSONArray cnv = cnvObj.getJSONArray("CNV");

                for (int i = 0; i < cnv.length(); i++) {

                    JSONObject geneObj = cnv.getJSONObject(i);
                    String symbol = geneObj.getString("gene");
                    this.cnvSymbols.add(symbol);

                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getRnaSeqDataFromURL(String modelId){

        String json = utilityService.parseURL(rnaSeqURL+modelId);

        JSONObject job = null;
        try {
            log.info("Loading rnaSeq: "+modelId);
            job = new JSONObject(json);
            JSONObject pdxExpression = job.getJSONObject("pdxExpression");
            JSONArray samples = pdxExpression.getJSONArray("samples");

            if(samples.length()>0){
                JSONObject rnaseqObj = samples.getJSONObject(0);
                JSONArray rnaseq = rnaseqObj.getJSONArray("Expression");

                for (int i = 0; i < rnaseq.length(); i++) {

                    JSONObject geneObj = rnaseq.getJSONObject(i);
                    String symbol = geneObj.getString("gene");
                    this.rnaseqSymbols.add(symbol);

                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void compareSymbols(HashMap<String, AccessionData> hugoDB){

        for(String cnvSymbol:this.cnvSymbols){

            if(!hugoDB.containsKey(cnvSymbol)){
                cnvSymbolsWithIssues.add(cnvSymbol);
            }
        }


        for(String rnaseqSymbol:this.rnaseqSymbols){

            if(!hugoDB.containsKey(rnaseqSymbol)){

                rnaseqSymbolsWithIssues.add(rnaseqSymbol);
            }
        }

    }

    private void printReport(){

        System.out.println("CNV symbols loaded: "+this.cnvSymbols.size());
        System.out.println("CNV symbol errors: "+this.cnvSymbolsWithIssues.size());

        for (String error:this.cnvSymbolsWithIssues){
            System.out.println(error);
        }

        System.out.println("RnaSeq symbols loaded: "+this.rnaseqSymbols.size());
        System.out.println("RnaSeq symbol errors: "+this.rnaseqSymbolsWithIssues.size());

        for (String error:this.rnaseqSymbolsWithIssues){
            System.out.println(error);
        }

    }


    private void validateSymbols(){

        Collection<ModelCreation> models = dataImportService.findAllModels();
        HashMap<String, AccessionData> hugoDB = loadHugoDatabase();

        for(ModelCreation m:models){

            getCnvDataFromURL(m.getSourcePdxId());
            getRnaSeqDataFromURL(m.getSourcePdxId());

        }

        compareSymbols(hugoDB);
        printReport();

        System.out.println("Done");

    }


}
