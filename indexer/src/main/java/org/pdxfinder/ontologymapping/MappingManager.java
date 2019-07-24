package org.pdxfinder.ontologymapping;

import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.MappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Created by csaba on 05/06/2019.
 */
public class MappingManager {


    private DataImportService dataImportService;
    private MappingService mappingService;

    private String mappingFile;
    private final static Logger log = LoggerFactory.getLogger(MappingManager.class);

    public MappingManager(DataImportService dataImportService, MappingService mappingService, String mappingFile) {
        this.dataImportService = dataImportService;
        this.mappingService = mappingService;
        this.mappingFile = mappingFile;
    }


    //implement mapping methods
}
