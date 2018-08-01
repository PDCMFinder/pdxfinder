package org.pdxfinder.admin.controllers;

import org.pdxfinder.admin.pojos.MappingContainer;
import org.pdxfinder.admin.pojos.MappingEntity;
import org.pdxfinder.services.MappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
 * Created by csaba on 09/07/2018.
 */
@RestController
public class AjaxController {

    private MappingService mappingService;



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


}
