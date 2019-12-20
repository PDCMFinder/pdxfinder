package org.pdxfinder.dataloaders.updog;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import tech.tablesaw.api.Table;

import java.util.Map;

public class DomainObjectCreator {

    private Map<String, Table> pdxDataTables;
    private UtilityService utilityService;
    private DataImportService dataImportService;


    public DomainObjectCreator(DataImportService dataImportService, UtilityService utilityService,
                               Map<String, Table> pdxDataTables) {
        this.dataImportService = dataImportService;
        this.utilityService = utilityService;
        this.pdxDataTables = pdxDataTables;
    }






}
