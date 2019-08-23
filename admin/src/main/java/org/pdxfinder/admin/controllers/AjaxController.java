package org.pdxfinder.admin.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.pdxfinder.rdbms.dao.MappingEntity;
import org.pdxfinder.admin.zooma.ZoomaEntity;
import org.pdxfinder.services.MappingService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.services.dto.PaginationDTO;
import org.pdxfinder.services.mapping.CSVHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
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
    private String homeDir = System.getProperty("user.home");

    private final String ZOOMA_URL = "http://scrappy.ebi.ac.uk:8080/annotations";
    private String errReport = "";


    @Autowired
    private UtilityService utilityService;
    private MappingService mappingService;
    private CSVHandler csvHandler;

    @Autowired
    public AjaxController(MappingService mappingService,
                          RestTemplateBuilder restTemplateBuilder,
                          CSVHandler csvHandler) {

        this.csvHandler = csvHandler;
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
     * @param status          - Search data by mapping status e.g ...?status=unmapped
     * @param page            - Allows client to submit offset value e.g ...?page=10
     * @param size            - Allows client to submit size limit values e.g ...?size=5
     * @return - Mapping Entities with data count, offset and limit Values
     */
    @GetMapping("/mappings")
    public ResponseEntity<?> getMappings(@RequestParam("mq") Optional<String> mappingQuery,
                                         @RequestParam(value = "mapped-term", defaultValue = "") Optional<String> mappedTermLabel,
                                         @RequestParam(value = "map-terms-only", defaultValue = "") Optional<String> mappedTermsOnly,
                                         @RequestParam(value = "entity-type", defaultValue = "0") Optional<List<String>> entityType,
                                         @RequestParam(value = "map-type", defaultValue = "") Optional<String> mapType,
                                         @RequestParam(value = "status", defaultValue = "0") Optional<List<String>> status,

                                         @RequestParam(value = "page", defaultValue = "1") Integer page,
                                         @RequestParam(value = "size", defaultValue = "10") Integer size) {

        String mappingLabel = "";
        List<String> mappingValue = Arrays.asList("0");

        try {
            String[] query = mappingQuery.get().split(":");
            mappingLabel = query[0];
            mappingValue = Arrays.asList(query[1].trim());


        } catch (Exception e) {
        }

        PaginationDTO result = mappingService.search(page, size, entityType.get(), mappingLabel,
                                                     mappingValue, mappedTermLabel.get(), mapType.get(), mappedTermsOnly.get(), status.get());

        return new ResponseEntity<Object>(result, HttpStatus.OK);
    }


    @GetMapping("/mappings/{entityId}")
    public ResponseEntity<?> getOneMapping(@PathVariable Optional<Integer> entityId) {

        if (entityId.isPresent()) {

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


    /**
     * Bulk update of Records
     */
    @PutMapping("/mappings")
    public ResponseEntity<?> editListOfEntityMappings(@RequestBody List<MappingEntity> submittedEntities) {

        List data = mapper.convertValue(submittedEntities, List.class);
        log.info(data.toString());

        List<Error> errors = validateEntities(submittedEntities);

        if (submittedEntities.size() < 1 || !errors.isEmpty()) {
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
        }

        List<MappingEntity> updated = mappingService.updateRecords(submittedEntities);
        return new ResponseEntity<>(updated, HttpStatus.OK);

    }


    @PostMapping("/mappings/uploads")
    public ResponseEntity<?> uploadData(@RequestParam("uploads") Optional<MultipartFile> uploads,
                                        @RequestParam(value = "entity-type", defaultValue = "") Optional<String> entityType) {

        Object responseBody = "";
        HttpStatus responseStatus = HttpStatus.OK;

        if (uploads.isPresent()) {

            /*
             * Send Data for Serialization
             */
            List<Map<String, String>> csvData = utilityService.serializeMultipartFile(uploads.get());

            /*
             * Validate CSV for emptiness, correct column Headers and valid contents
             */
            List report = new ArrayList();
            try {

                report = csvHandler.validateUploadedCSV(csvData);
            }catch (Exception e){

                report.add(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase());
            }

            // If all rows passed submit data for processing
            if (report.isEmpty()) {

                List<MappingEntity> updatedData = mappingService.processUploadedCSV(csvData);

                responseBody = updatedData;
                responseStatus = HttpStatus.OK;
            }else {

                responseBody = report;
                responseStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            }

            // For this operation, create record in uploadedFileTable, For each of this back up in the validatedData Table
            // Further read uploadedFile Table for getting history.
        }

        return new ResponseEntity<>(responseBody, responseStatus);
    }


    @GetMapping("/mappings/export")
    @ResponseBody
    public Object exportMappingData(HttpServletResponse response,
                                    @RequestParam("mq") Optional<String> mappingQuery,
                                    @RequestParam(value = "mapped-term", defaultValue = "") Optional<String> mappedTermLabel,
                                    @RequestParam(value = "map-terms-only", defaultValue = "") Optional<String> mappedTermsOnly,
                                    @RequestParam(value = "entity-type", defaultValue = "0") Optional<List<String>> entityType,
                                    @RequestParam(value = "map-type", defaultValue = "") Optional<String> mapType,
                                    @RequestParam(value = "status", defaultValue = "0") Optional<List<String>> status,
                                    @RequestParam(value = "page", defaultValue = "1") Integer page) {

        String mappingLabel = "";
        List<String> mappingValue = Arrays.asList("0");

        try {
            String[] query = mappingQuery.get().split(":");
            mappingLabel = mappingQuery.get().split(":")[0];
            mappingValue = Arrays.asList(query[1].trim());
        } catch (Exception e) {
        }

        int size = 1000;
        PaginationDTO result = mappingService.search(page, size, entityType.get(), mappingLabel,
                                                     mappingValue, mappedTermLabel.get(), mapType.get(), mappedTermsOnly.get(), status.get());

        List<MappingEntity> mappingEntities = mapper.convertValue(result.getAdditionalProperties().get("mappings"), List.class);

        /*
         *  Get Mapping Entity CSV Header
         */
        MappingEntity me = mappingEntities.get(0);
        List<String> csvHead = csvHandler.getMappingEntityCSVHead(mappingEntities.get(0));

        /*
         *  Get Mapping Entity CSV Data Body
         */
        List<List<String>> mappingDataCSV = csvHandler.prepareMappingEntityForCSV(mappingEntities);


        CsvMapper mapper = new CsvMapper();
        CsvSchema.Builder builder = CsvSchema.builder();

        for (String head : csvHead) {
            builder.addColumn(head);
        }
        CsvSchema schema = builder.build().withHeader();

        String csvReport = "CSV Report";
        try {
            csvReport = mapper.writer(schema).writeValueAsString(mappingDataCSV);
        } catch (JsonProcessingException e) {
        }

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=pdxAdmin-" + me.getStatus() + ".csv");
        try {
            response.getOutputStream().flush();
        } catch (Exception e) {

        }

        return csvReport;
    }


    /****************************************************************
     *                   INTERACTIONS WITH ZOOMA                    *
     ****************************************************************/


    @GetMapping("/zooma/transform")
    public ResponseEntity<?> transformAnnotationForZooma() {

        List<ZoomaEntity> zoomaEntities = mappingService.transformMappingsForZooma();

        return new ResponseEntity<>(zoomaEntities, HttpStatus.OK);
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

        String failedReportFile = homeDir + "/Documents/" + (new Date()) + "_failed.json";
        String errorReportFile = homeDir + "/Documents/" + (new Date()) + "_error.txt";

        utilityService.writeToFile(failedReport, failedReportFile, true);
        utilityService.writeToFile(this.errReport, errorReportFile, true);

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


    public List validateEntities(List<MappingEntity> mappingEntities) {

        List<Error> errors = new ArrayList<>();

        for (MappingEntity me : mappingEntities) {

            if (!mappingService.checkExistence(me.getEntityId())) {

                Error error = new Error("Entity " + me.getEntityId() + " Not Found", HttpStatus.NOT_FOUND);
                errors.add(error);
            }
        }
        return errors;
    }


}
