package org.pdxfinder.commandline;

import org.pdxfinder.services.MappingService;
import org.pdxfinder.services.constants.DataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class FinderMappingExporter {

    @Autowired
    MappingService mappingService;

    public void run(File dataDirectory, List<DataProvider> dataProviders){

        System.out.println("OK");
        List<String> dataSourcesToExport = new ArrayList<>();

        for(DataProvider dp: dataProviders)
            dataSourcesToExport.add(dp.toString());

        String diagnosisFileName = String.format("%s/mappings_out/diagnosis_mappings.json", dataDirectory.getPath());
        String treatmentFileName = String.format("%s/mappings_out/treatment_mappings.json", dataDirectory.getPath());


        mappingService.saveMappingsToFile(
                diagnosisFileName,
                mappingService.getMappingsByDSAndType(dataSourcesToExport, "diagnosis").getEntityList()
        );

        mappingService.saveMappingsToFile(
                treatmentFileName,
                mappingService.getMappingsByDSAndType(dataSourcesToExport, "treatment").getEntityList()
        );

    }
}
