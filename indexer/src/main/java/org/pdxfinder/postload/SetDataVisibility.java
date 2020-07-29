package org.pdxfinder.postload;

import org.pdxfinder.graph.dao.MolecularCharacterization;
import org.pdxfinder.services.DataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Order(value = 94)
public class SetDataVisibility {

    private final static Logger log = LoggerFactory.getLogger(SetDataVisibility.class);

    private DataImportService dataImportService;

    public SetDataVisibility(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }


    public void run() {

        long startTime = System.currentTimeMillis();

        log.info("Applying data visibility rules");

        applyDataVisibilityRules("CRL");

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int seconds = (int) (totalTime / 1000) % 60;
        int minutes = (int) ((totalTime / (1000 * 60)) % 60);

        log.info(this.getClass().getSimpleName() + " finished after " + minutes + " minute(s) and " + seconds + " second(s)");

    }


    public void applyDataVisibilityRules(String datasourceAbbrev){

        log.info("Disabling data visibility for "+datasourceAbbrev);

        int molcharCounter = dataImportService.findMolcharNumberByDataSource(datasourceAbbrev);

        for(int i=0; i < molcharCounter; i+=50){
            List<MolecularCharacterization> molChars = dataImportService.findMolcharByDataSourceSkipLimit(datasourceAbbrev, i, 50);
            disableVisibility(molChars);
            dataImportService.saveMolecularCharacterization(molChars);
        }
    }


    public void disableVisibility(List<MolecularCharacterization> molChars){
        for(MolecularCharacterization mc:molChars){
            mc.setVisible(false);
        }
    }

}
