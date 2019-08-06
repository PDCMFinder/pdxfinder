package org.pdxfinder.admin.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pdxfinder.rdbms.dao.MappingEntity;
import org.pdxfinder.admin.zooma.ZoomaEntity;
import org.pdxfinder.services.MappingService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.services.dto.PaginationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;


/*
 * Created by csaba on 09/07/2018.
 */
@CrossOrigin
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
    public AjaxController(MappingService mappingService,
                          RestTemplateBuilder restTemplateBuilder) {
        this.mappingService = mappingService;
        this.restTemplate = restTemplateBuilder.build();
    }


    /**
     * Provides entry point to query the MappingEntity data store
     * E.g : .../api/mappings?map-terms-only=true&mq=datasource:jax&entity-type=treatment
     *
     * @param mappingQuery    - Key value map of mappingValues e.g to filter by DataSource:jax, ...?mq=datasource:jax
     * @param mappedTermLabel - Filters the data for missing mappings e.g To find missing mappings, ...?mapped-term=-
     * @param entityType      - Search by entityType e.g find unmapped treatment entities ...?entity-type=treatment&mapped-term=-
     * @param mappedTermsOnly - Search for mapped terms only ... map-terms-only=true
     * @param mapType         - Search data by mapType e.g ...?map-type=direct
     * @param page            - Allows client to submit offset value e.g ...?page=10
     * @param size            - Allows client to submit size limit values e.g ...?size=5
     * @return - Mapping Entities with data count, offset and limit Values
     */
    @GetMapping("/mappings")
    public ResponseEntity<?> getMappings(@RequestParam("mq") Optional<String> mappingQuery,
                                         @RequestParam(value = "mapped-term", defaultValue = "") Optional<String> mappedTermLabel,
                                         @RequestParam(value = "map-terms-only", defaultValue = "") Optional<String> mappedTermsOnly,
                                         @RequestParam(value = "entity-type", defaultValue = "") Optional<String> entityType,
                                         @RequestParam(value = "map-type", defaultValue = "") Optional<String> mapType,

                                         @RequestParam(value = "page", defaultValue = "1") Integer page,
                                         @RequestParam(value = "size", defaultValue = "10") Integer size) {

        String mappingLabel = "";
        String mappingValue = "";

        try {
            String[] query = mappingQuery.get().split(":");
            mappingLabel = query[0];
            mappingValue = query[1].trim();

        } catch (Exception e) {
        }

        PaginationDTO result = mappingService.search(page, size, entityType.get(), mappingLabel,
                                                     mappingValue, mappedTermLabel.get(), mapType.get(), mappedTermsOnly.get());

        return new ResponseEntity<Object>(result, HttpStatus.OK);
        //Map<String, List<MappingEntity>> result =  mappingService.getMissingDiagnosisMappings(ds);
    }


    @GetMapping("/mappings/{entityId}")
    public ResponseEntity<?> getOneMapping(@PathVariable Optional<Integer> entityId) {

        if (entityId.isPresent()){

            MappingEntity result = mappingService.getMappingEntityById(entityId.get());

            return new ResponseEntity<Object>(result, HttpStatus.OK);
        }

        return new ResponseEntity<Object>(HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST);
    }



    @GetMapping("/mappings/summary")
    public ResponseEntity<?> getMappingStatSummary(@RequestParam(value = "entity-type", defaultValue = "") Optional<String> entityType) {

        List<Map> result = mappingService.getMappingSummary(entityType.get());

        return new ResponseEntity<Object>(result, HttpStatus.OK);
    }


    // Bulk update of Records
    @PutMapping("/mappings")
    public ResponseEntity<?> editListOfEntityMappings(@RequestBody List<MappingEntity> submittedEntities) {

        List data =  mapper.convertValue(submittedEntities, List.class);
        log.info(data.toString());

        List<Error> errors = validateEntities(submittedEntities);

        if (!errors.isEmpty()){
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
        }

        List<MappingEntity> updated = mappingService.updateRecords(submittedEntities);
        return new ResponseEntity<>(updated, HttpStatus.OK);

    }





    /****************************************************************
     *                   INTERACTIONS WITH ZOOMA                    *
     ****************************************************************/


    @GetMapping("/zooma/transform")
    public List<ZoomaEntity> transformAnnotationForZooma() {

        List<ZoomaEntity> zoomaEntities = mappingService.transformMappingsForZooma();
        return zoomaEntities; //new ResponseEntity<>(result, HttpStatus.OK);
    }


    @GetMapping("/zooma")
    public ResponseEntity<?> writeAllAnnotationsToZooma() {

        Map report = new LinkedHashMap();
        List<ZoomaEntity> zoomaEntities = mappingService.transformMappingsForZooma();

        int count = 0;
        List<ZoomaEntity> failedList = new ArrayList<>();
        for (ZoomaEntity zoomaEntity : zoomaEntities) {

            String entity = zoomaEntity.getBiologicalEntities().getBioEntity() + "__" + count++;
            if (writeToZooma(zoomaEntity)) {

                report.put(entity, "SUCCESS WRITE");
            } else {

                report.put(entity, "THIS ANNOTATION WAS NOT WRITTEN TO ZOOMA");
                failedList.add(zoomaEntity);
            }
        }

        /* LOG FAILED OBJECTS TO FILE */
        String failedReport = "";
        try {
            failedReport = mapper.writeValueAsString(failedList);
        } catch (Exception e) {
        }

        utilityService.writeToFile(failedReport, (new Date()) + "_failed.json");
        utilityService.writeToFile(this.errReport, (new Date()) + "_error.txt");

        return new ResponseEntity<>(report, HttpStatus.OK);
    }


    public Boolean writeToZooma(ZoomaEntity zoomaEntity) {

        HttpEntity<String> entity = BuildHttpHeader();
        HttpEntity<Object> req = new HttpEntity<>(zoomaEntity, entity.getHeaders());
        Map result = new HashMap();

        Boolean report = false;
        try {
            result = restTemplate.postForObject(ZOOMA_URL, req, Map.class);
            report = true;
        } catch (Exception e) {

            this.errReport += "\n\n ************************ NEW ERROR LOG " + (new Date()) + " *********************** \n" + e;
        }

        return report;
    }


    public HttpEntity<String> BuildHttpHeader() {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        return entity;
    }


    public String getCamelCase(String input) {

        String output = StringUtils.capitalize(input.split("-")[0]) + StringUtils.capitalize(input.split("-")[1]);

        return output;
    }


    public List validateEntities(List<MappingEntity> mappingEntities){

        List<Error> errors = new ArrayList<>();

        for (MappingEntity me : mappingEntities){

            if (!mappingService.checkExistence(me.getEntityId())){

                Error error = new Error("Entity " + me.getEntityId() + " Not Found", HttpStatus.NOT_FOUND);
                errors.add(error);
            }
        }
        return errors;
    }


}
