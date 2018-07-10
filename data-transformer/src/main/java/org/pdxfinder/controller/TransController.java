package org.pdxfinder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pdxfinder.transcommands.DataTransformerService;
import org.pdxfinder.transdatamodel.PdmrPdxInfo;
import org.pdxfinder.transdatamodel.PdxInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@RestController
@RequestMapping("/transformer")
public class TransController {


    private RestTemplate restTemplate = new RestTemplate();
    ObjectMapper mapper = new ObjectMapper();
    private DataTransformerService dataTransformerService;

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



    private final static Logger log = LoggerFactory.getLogger(TransController.class);


    public TransController(DataTransformerService dataTransformerService){
        this.dataTransformerService = dataTransformerService;
    }


    @GetMapping("/view-data")
    public PdxInfo getAllPdmr()
    {
        List<PdmrPdxInfo> pdmrPdxInfos = dataTransformerService.getAllPdmr();

        PdxInfo pdxInfo = new PdxInfo();
        pdxInfo.setPdxInfo(pdmrPdxInfos);

        return pdxInfo;
    }



    @GetMapping("/load-data")
    public String connectPdmr(){

        dataTransformerService.transformDataAndSave(specimenSearchUrl, specimenUrl, tissueOriginsUrl, tumoGradeStateTypesUrl, mouseStrainsUrl,
                implantationSitesUrl, tissueTypeUrl, histologyUrl, tumorGradeUrl, samplesUrl,
                currentTherapyUrl, standardRegimensUrl, clinicalResponseUrl);
        return "success";

    }
















}





