package org.pdxfinder.postload;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.pdxfinder.graph.dao.MolecularCharacterization;
import org.pdxfinder.services.DataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 * Created by csaba on 25/04/2019.
 */
@Component

@Order(value = 94)
public class SetDataVisibility implements CommandLineRunner{

    private final static Logger log = LoggerFactory.getLogger(SetDataVisibility.class);

    private DataImportService dataImportService;

    public SetDataVisibility(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }




    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("setDataVisibility", "Applying data visibility rules");
        parser.accepts("loadALL", "Load all then apply data visibility rules");
        parser.accepts("loadEssentials", "Load essentials then apply data visibility rules");

        OptionSet options = parser.parse(args);
        long startTime = System.currentTimeMillis();

        if (options.has("setDataVisibility") || options.has("loadALL")  || options.has("loadEssentials")) {

            log.info("Applying data visibility rules");


            applyDataVisibilityRules("IRCC-GC");
            applyDataVisibilityRules("CRL");
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int seconds = (int) (totalTime / 1000) % 60;
        int minutes = (int) ((totalTime / (1000 * 60)) % 60);

        log.info(this.getClass().getSimpleName() + " finished after " + minutes + " minute(s) and " + seconds + " second(s)");

    }


    private void applyDataVisibilityRules(String datasourceAbbrev){

        //get all charles river molchars
        List<MolecularCharacterization> molChars = dataImportService.findAllMolcharByDataSource(datasourceAbbrev);

        for(MolecularCharacterization mc:molChars){

            mc.setVisible(false);
            dataImportService.saveMolecularCharacterization(mc);
        }

    }


}
