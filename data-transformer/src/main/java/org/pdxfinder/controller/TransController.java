package org.pdxfinder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pdxfinder.transcommands.DataTransformerService;
import org.pdxfinder.transdatamodel.PdxInfo;
import org.pdxfinder.transdatamodel.PdmrPdxInfo;
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

    @Value("${mydatasource.url1}")
    private String url1;

    @Value("${mydatasource.url2}")
    private String url2;


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

        dataTransformerService.transformDataAndSave(url1,url2);
        return "success";

    }
















}





