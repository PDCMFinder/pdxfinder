package org.pdxfinder.services;

import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.repositories.MolecularCharacterizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/*
 * Created by csaba on 18/03/2019.
 */
public class MolcharTableTest extends BaseTest{


    private final static Logger log = LoggerFactory.getLogger(MolcharTableTest.class);

    @Autowired
    private MolecularCharacterizationRepository molecularCharacterizationRepository;

    List<Long> molcharNodeIds;

    @Before
    public void setUp(){
        //log.info("Getting molchar node ids");
        //molcharNodeIds = molecularCharacterizationRepository.getAllMolCharIDs();

        //log.info("Found "+molcharNodeIds.size()+ " molchar nodes");
    }



    @Test
    public void visitAllMolcharUrls(){

/*

        for(Long id: molcharNodeIds){

        }

        try{

            String url = "/getmoleculardata/"+molcharNodeIds.get(0);
            log.info("Getting data from: "+url);
            String response = getText(url);

            System.out.println(response);

        }
        catch (Exception e){
            e.printStackTrace();
        }



*/
    }


    public static String getText(String url) throws Exception {
        URL website = new URL(url);
        URLConnection connection = website.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            response.append(inputLine);

        in.close();

        return response.toString();

    }

}
