package org.pdxfinder.admin.controllers;

import org.pdxfinder.admin.pojos.MappingContainer;
import org.pdxfinder.services.MappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * Created by csaba on 09/07/2018.
 */
@RestController
public class AjaxController {

    private MappingService mappingService;

    @Value("${diagnosis.mappings.file}")
    private String savedDiagnosisMappingsFile;

    @Autowired
    public AjaxController(MappingService mappingService) {
        this.mappingService = mappingService;
    }

    @RequestMapping(value = "/getmissingdiagnosismappings")
    public MappingContainer getMissingMappings(){

        return mappingService.getSavedDiagnosisMappings(savedDiagnosisMappingsFile);
    }



}
