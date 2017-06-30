package org.pdxfinder.commands;

import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.dao.Marker;
import org.pdxfinder.utilities.LoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

/**
 * Created by csaba on 30/06/2017.
 */
@Component
@Order(value = Ordered.LOWEST_PRECEDENCE)
public class LoadAccessionIDs implements CommandLineRunner {

    private static final String HGNC_URL = "http://rest.genenames.org/fetch/symbol/";
    private static final String ENSEMBL_URL = "http://rest.ensembl.org/xrefs/symbol/homo_sapiens/BRAF?content-type=application/json";


    private final static Logger log = LoggerFactory.getLogger(LoadAccessionIDs.class);
    private LoaderUtils loaderUtils;

    @Autowired
    public LoadAccessionIDs(LoaderUtils loaderUtils) {
        this.loaderUtils = loaderUtils;
    }



    @Override
    public void run(String... args) throws Exception {

        log.info(args[0]);

        if ("loadAccessionIds".equals(args[0]) || "-loadAccessionIds".equals(args[0])) {

            log.info("Looking up HUGO ids for markers");
            long startTime = System.currentTimeMillis();
            loadAccessionIds();
            long endTime   = System.currentTimeMillis();
            long totalTime = endTime - startTime;

            int seconds = (int) (totalTime / 1000) % 60 ;
            int minutes = (int) ((totalTime / (1000*60)) % 60);

            System.out.println("Loading finished after "+minutes+" minute(s) and "+seconds+" second(s)");
        }
        else{
            log.info("Missing command");
        }

    }


    private void loadAccessionIds(){

        System.out.println("Loading all markers from Neo4j");
        Collection<Marker> markers = loaderUtils.getAllMarkers();

        String mUrl = "";

        for(Marker m:markers){

            if(m.getSymbol() != null && m.getSymbol() != ""){

                mUrl =   HGNC_URL+m.getSymbol();
                System.out.println("Fetching data from: "+mUrl);
                try {
                    String jString = "";
                    jString = getJson(mUrl);
                    JSONObject job = new JSONObject(jString);
                    JSONObject response = job.getJSONObject("response");

                    if(response.has("docs")){

                        JSONArray docs = response.getJSONArray("docs");

                        System.out.println(jString);
                        if(docs.length() == 0) continue;
                        JSONObject ids = docs.getJSONObject(0);


                        if (ids.getString("hgnc_id") != null){
                            m.setHugoId(ids.getString("hgnc_id"));
                        }
                        if (ids.getString("ensembl_gene_id") != null){
                            m.setEnsemblId(ids.getString("ensembl_gene_id"));
                        }
                        System.out.println(ids.getString("hgnc_id"));

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                loaderUtils.saveMarker(m);

            }




        }

    }



    private String getJson(String mUrl){

        StringBuilder sb = new StringBuilder();

        try {
            URL url = new URL(mUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.addRequestProperty("Accept","application/json");

            if(conn.getResponseCode() != 200){
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }

            conn.disconnect();

        }
        catch(MalformedURLException e){
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }

        return sb.toString();
    }

}
