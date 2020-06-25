package org.pdxfinder.mapping;

import org.pdxfinder.rdbms.dao.MappingEntity;
import org.pdxfinder.services.MappingService;
import org.pdxfinder.services.mapping.MappingEntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InitMappingDatabase {

    @Value("${data-dir}")
    private String finderRootDir;

    private MappingService mappingService;

    private Logger log = LoggerFactory.getLogger(InitMappingDatabase.class);

    public InitMappingDatabase(MappingService mappingService) {
        this.mappingService = mappingService;
    }

    public void run() {
        log.info("initializing mappings database");
        mappingService.purgeMappingDatabase();

        mappingFileToH2DB(MappingEntityType.diagnosis);
        mappingFileToH2DB(MappingEntityType.treatment);
    }

    private void mappingFileToH2DB(MappingEntityType type) {
        String jsonFile = finderRootDir + "/mapping/" + type + "_mappings.json";
        List<MappingEntity> mappingEntities = mappingService.loadMappingsFromFile(jsonFile);
        mappingService.saveMappedTerms(mappingEntities);
    }


}
