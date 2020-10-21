package org.pdxfinder.postload;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.pdxfinder.graph.repositories.MolecularCharacterizationRepository;
import org.pdxfinder.services.DetailsService;
import org.pdxfinder.services.dto.MolecularDataTableDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/*
 * Created by csaba on 18/03/2019.
 */
@Component
public class MolcharAPITest implements CommandLineRunner{

    private final static Logger log = LoggerFactory.getLogger(MolcharAPITest.class);

    List<Long> molcharNodeIds;

    @Autowired
    MolecularCharacterizationRepository molecularCharacterizationRepository;

    @Autowired
    DetailsService detailsService;

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("testMCUrls", "Testing mc urls");
        OptionSet options = parser.parse(args);


        if (options.has("testMCUrls")) {

            testMCUrls();
        }



    }


    private void testMCUrls(){

        log.info("Getting molchar node ids");
        molcharNodeIds = molecularCharacterizationRepository.getAllMolCharIDs();

        log.info("Found "+molcharNodeIds.size()+ " molchar nodes");

        for(Long id: molcharNodeIds){



            try{

                long startTime = System.currentTimeMillis();

                MolecularDataTableDTO dto = detailsService.getMolecularDataTable(id.toString(), false);

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;

                int size = dto.getMolecularDataRows().size();

                if(size == 0){
                    log.error("MC id: "+id.toString()+": Size: "+size+ " Time:"+totalTime);
                }
                if(totalTime > 600){
                    log.warn("MC id: "+id.toString()+": Size: "+size+ " Time:"+totalTime);
                }

            }
            catch(Exception e){
                e.printStackTrace();
                log.error("At MC:"+id.toString());
            }




        }

        //TreeMap results = new TreeMap();



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
