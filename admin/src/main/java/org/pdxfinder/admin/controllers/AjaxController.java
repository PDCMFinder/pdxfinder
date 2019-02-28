package org.pdxfinder.admin.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pdxfinder.rdbms.dao.MappingEntity;
import org.pdxfinder.admin.zooma.ZoomaEntity;
import org.pdxfinder.services.MappingService;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;


/*
 * Created by csaba on 09/07/2018.
 */
@RestController
@RequestMapping("/api")
public class AjaxController {

    private final static Logger log = LoggerFactory.getLogger(AjaxController.class);
    private ObjectMapper mapper = new ObjectMapper();
    private RestTemplate restTemplate;

    private final String ZOOMA_URL = "http://scrappy.ebi.ac.uk:8080/annotations";
    private String errReport = "";


    @Autowired
    private UtilityService utilityService;
    private MappingService mappingService;

    @Autowired
    public AjaxController(MappingService mappingService, RestTemplateBuilder restTemplateBuilder) {
        this.mappingService = mappingService;
        this.restTemplate = restTemplateBuilder.build();
    }



                        /****************************************************************
                         *          INTERACTIONS WITH PDX FINDER KNOWLEDGE BASE         *
                         ****************************************************************/


    @RequestMapping(value = "/missingmapping/diagnosis")
    @ResponseBody
    public Map<String, List<MappingEntity>> getMissingMappings(@RequestParam("ds") Optional<String> dataSource){

        String ds = null;
        if(dataSource.isPresent() && !dataSource.get().isEmpty()){
            ds = dataSource.get();
        }

        return mappingService.getMissingDiagnosisMappings(ds).getEntityList();
    }


    @RequestMapping(value = "/mapping/diagnosis")
    @ResponseBody
    public Map<String, List<MappingEntity>>  getDiagnosisMappings(@RequestParam("ds") Optional<String> dataSource){

        String ds = null;
        if(dataSource.isPresent() && !dataSource.get().isEmpty()){
            ds = dataSource.get();
        }

        return mappingService.getSavedDiagnosisMappings(ds).getEntityList();
    }


    @PostMapping("/diagnosis")
    public ResponseEntity<?> createDiagnosisMappings(@RequestBody List<MappingEntity> newMappings){

        log.info(newMappings.toString());
        return ResponseEntity.noContent().build();
    }






                        /****************************************************************
                         *                   INTERACTIONS WITH ZOOMA                    *
                         ****************************************************************/


    @GetMapping("/zooma/transform")
    public List<ZoomaEntity> transformAnnotationForZooma(){

        List<ZoomaEntity> zoomaEntities = mappingService.transformMappingsForZooma();
        return zoomaEntities; //new ResponseEntity<>(result, HttpStatus.OK);
    }



    @GetMapping("/zooma")
    public ResponseEntity<?> writeAllAnnotationsToZooma(){

        Map report = new LinkedHashMap();
        List<ZoomaEntity> zoomaEntities = mappingService.transformMappingsForZooma();

        int count = 0;
        List<ZoomaEntity> failedList = new ArrayList<>();
        for (ZoomaEntity zoomaEntity : zoomaEntities){

            String entity = zoomaEntity.getBiologicalEntities().getBioEntity()+"__"+count++;
            if (writeToZooma(zoomaEntity)){

                report.put(entity,"SUCCESS WRITE");
            }else{

                report.put(entity, "THIS ANNOTATION WAS NOT WRITTEN TO ZOOMA");
                failedList.add(zoomaEntity);
            }
        }

        /* LOG FAILED OBJECTS TO FILE */
        String failedReport = "";
        try{
            failedReport = mapper.writeValueAsString(failedList);
        }catch (Exception e){}

        utilityService.writeToFile(failedReport,(new Date())+"_failed.json");
        utilityService.writeToFile(this.errReport,(new Date())+"_error.txt");

        return new ResponseEntity<>(report, HttpStatus.OK);
    }





    public Boolean writeToZooma(ZoomaEntity zoomaEntity){

        HttpEntity<String> entity = BuildHttpHeader();
        HttpEntity<Object> req = new HttpEntity<>(zoomaEntity, entity.getHeaders());
        Map result =  new HashMap();

        Boolean report = false;
        try{
            result = restTemplate.postForObject(ZOOMA_URL, req, Map.class);
            report = true;
        }catch (Exception e){

            this.errReport += "\n\n ************************ NEW ERROR LOG "+(new Date())+" *********************** \n"+e;
        }

        return report;
    }






    public HttpEntity<String> BuildHttpHeader(){

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        return  entity;
    }



}
