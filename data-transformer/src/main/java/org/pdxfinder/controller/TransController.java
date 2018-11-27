package org.pdxfinder.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pdxfinder.admin.zooma.ZoomaEntity;
import org.pdxfinder.transcommands.DataTransformerService;
import org.pdxfinder.transcommands.ZoomaTransform;
import org.pdxfinder.transdatamodel.PdmrPdxInfo;
import org.pdxfinder.transdatamodel.PdxInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/transformer")
public class TransController {


    private RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper mapper = new ObjectMapper();
    private DataTransformerService dataTransformerService;

    private final static Logger log = LoggerFactory.getLogger(TransController.class);

    private final String ZOOMA_URL = "http://scrappy.ebi.ac.uk:8080/annotations";

    @Value("${mydatasource.specimenSearchUrl}")
    private String specimenSearchUrl;

    @Value("${mydatasource.specimenUrl}")
    private String specimenUrl;

    @Value("${mydatasource.tissueOriginsUrl}")
    private String tissueOriginsUrl;

    @Value("${mydatasource.tumoGradeStateTypesUrl}")
    private String tumoGradeStateTypesUrl;

    @Value("${mydatasource.mouseStrainsUrl}")
    private String mouseStrainsUrl;

    @Value("${mydatasource.implantationSitesUrl}")
    private String implantationSitesUrl;

    @Value("${mydatasource.tissueTypeUrl}")
    private String tissueTypeUrl;

    @Value("${mydatasource.histologyUrl}")
    private String histologyUrl;

    @Value("${mydatasource.tumorGradeUrl}")
    private String tumorGradeUrl;

    @Value("${mydatasource.samplesUrl}")
    private String samplesUrl;

    @Value("${mydatasource.currentTherapyUrl}")
    private String currentTherapyUrl;

    @Value("${mydatasource.standardRegimensUrl}")
    private String standardRegimensUrl;

    @Value("${mydatasource.clinicalResponseUrl}")
    private String clinicalResponseUrl;

    @Value("${mydatasource.priorTherapyUrl}")
    private String priorTherapyUrl;


    @Value("${mydatasource.mappedTermUrl}")
    private String mappedTermUrl;


    @Autowired
    private ZoomaTransform zoomaTransform;

    public TransController(DataTransformerService dataTransformerService, RestTemplateBuilder restTemplateBuilder){
        this.dataTransformerService = dataTransformerService;
        this.restTemplate = restTemplateBuilder.build();
    }


    @GetMapping("/view-data")
    public PdxInfo getAllPdmr()
    {
        List<PdmrPdxInfo> pdmrPdxInfos = dataTransformerService.getAllPdmr();

        PdxInfo pdxInfo = new PdxInfo();
        pdxInfo.setPdxInfo(pdmrPdxInfos);

        return pdxInfo;
    }



    @GetMapping("/transform-pdmr-data")
    public String connectPdmr(){

        dataTransformerService.transformDataAndSave(specimenSearchUrl, specimenUrl, tissueOriginsUrl, tumoGradeStateTypesUrl, mouseStrainsUrl,
                implantationSitesUrl, tissueTypeUrl, histologyUrl, tumorGradeUrl, samplesUrl,
                currentTherapyUrl, standardRegimensUrl, clinicalResponseUrl, priorTherapyUrl);
        return "success";

    }



    @GetMapping("/transform-mappings")
    public ResponseEntity<?> transformMappingsForZooma(){

        List<ZoomaEntity> zoomaEntities = zoomaTransform.transformMappingsForZooma(mappedTermUrl);

        ZoomaEntity zoomaEntity = zoomaEntities.get(0);
        HttpEntity<String> entity = BuildHttpHeader();
        HttpEntity<Object> req = new HttpEntity<>(zoomaEntity, entity.getHeaders());

        ResponseEntity<ZoomaEntity> result = restTemplate.postForObject(ZOOMA_URL, req, ResponseEntity.class);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    public HttpEntity<String> BuildHttpHeader(){

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        return  entity;
    }













}





