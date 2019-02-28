package org.pdxfinder.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.Str;
import org.pdxfinder.admin.zooma.ZoomaEntity;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.transcommands.DataTransformerService;
import org.pdxfinder.transdatamodel.PdmrPdxInfo;
import org.pdxfinder.transdatamodel.PdxInfo;
import org.pdxfinder.transdatamodel.Treatment;
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

import java.util.*;


@RestController
@RequestMapping("/transformer")
public class TransController {

    private final static Logger log = LoggerFactory.getLogger(TransController.class);

    private RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper mapper = new ObjectMapper();
    private DataTransformerService dataTransformerService;
    @Autowired
    private UtilityService utilityService;



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


    @GetMapping("/drugs")
    public String getAllPdmrDrugs()
    {
        List<PdmrPdxInfo> pdmrPdxInfos = dataTransformerService.getAllPdmr();

        String drugList = "";

        for (PdmrPdxInfo pdmrPdxInfo : pdmrPdxInfos){

            drugList += dataTransformerService.getDrugs(pdmrPdxInfo);
        }

        return drugList;
    }



    @GetMapping("/transform-pdmr-data")
    public List<Map> connectPdmr(){

        List<Map> mappingList = dataTransformerService.transformDataAndSave();
        return mappingList;

    }











}





