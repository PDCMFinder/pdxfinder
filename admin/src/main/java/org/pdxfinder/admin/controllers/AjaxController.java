package org.pdxfinder.admin.controllers;

import org.pdxfinder.admin.pojos.MappingEntity;
import org.pdxfinder.services.MappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;


/*
 * Created by csaba on 09/07/2018.
 */
@RestController
public class AjaxController {

    private MappingService mappingService;
    private final static Logger log = LoggerFactory.getLogger(AjaxController.class);


    @Autowired
    public AjaxController(MappingService mappingService) {
        this.mappingService = mappingService;
    }

    @RequestMapping(value = "/api/missingmapping/diagnosis")
    @ResponseBody
    public Map<String, List<MappingEntity>> getMissingMappings(@RequestParam("ds") Optional<String> dataSource){

        String ds = null;
        if(dataSource.isPresent() && !dataSource.get().isEmpty()){
            ds = dataSource.get();
        }

        return mappingService.getMissingDiagnosisMappings(ds).getEntityList();
    }


    @RequestMapping(value = "/api/mapping/diagnosis")
    @ResponseBody
    public Map<String, List<MappingEntity>>  getDiagnosisMappings(@RequestParam("ds") Optional<String> dataSource){

        String ds = null;
        if(dataSource.isPresent() && !dataSource.get().isEmpty()){
            ds = dataSource.get();
        }

        return mappingService.getSavedDiagnosisMappings(ds).getEntityList();
    }


    @PostMapping("/api/diagnosis")
    public ResponseEntity<?> createDiagnosisMappings(@RequestBody List<MappingEntity> newMappings){

        log.info(newMappings.toString());
        return ResponseEntity.noContent().build();
    }


}
