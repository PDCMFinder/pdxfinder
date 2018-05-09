package org.pdxfinder.web.controllers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.commons.lang3.text.WordUtils;
import org.pdxfinder.services.DetailsService;
import org.pdxfinder.services.dto.VariationDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/*
 * Created by abayomi on 09/05/2018.
 */
@Controller
public class DetailsController {


    private DetailsService detailsService;


    @Autowired
    public DetailsController(DetailsService detailsService) {
        this.detailsService = detailsService;
    }


    @RequestMapping(value = "/pdx/{dataSrc}/{modelId:.+}")
    public String details(Model model,
                          @PathVariable String dataSrc,
                          @PathVariable String modelId,
                          @RequestParam(value = "page", defaultValue = "0") Integer page,
                          @RequestParam(value = "size", defaultValue = "15000") Integer size){

        model.addAttribute("data", detailsService.getModelDetails(dataSrc,modelId,page,size,"","","") );
        return "details";
    }



    @RequestMapping(method = RequestMethod.GET, value = "/pdx/{dataSrc}/{modelId}/export")
    @ResponseBody
    public String download(HttpServletResponse response,
                           @PathVariable String dataSrc,
                           @PathVariable String modelId){

        Set<String[]> variationDataDTOSet = new LinkedHashSet<>();

        String[] space = {""}; String nil = "";

        //Retreive Diagnosis Information
        String diagnosis = detailsService.getModelDetails(dataSrc,modelId,0,50000,"","","").getDiagnosis();

        // Retreive technology Information
        List platforms = new ArrayList();
        Map<String, Set<String>> modelTechAndPassages = detailsService.findModelPlatformAndPassages(dataSrc,modelId,"");
        for (String tech : modelTechAndPassages.keySet()) {
            platforms.add(tech);
        }

        // Retreive all Genomic Datasets
        VariationDataDTO variationDataDTO = detailsService.variationDataByPlatform(dataSrc,modelId,"","",0,50000,nil,1,"mAss.seqPosition",nil);
        for (String[] dData : variationDataDTO.moreData())
        {
            dData[2] = WordUtils.capitalize(diagnosis);   //Histology
            dData[3] = "Xenograft Tumor";                //Tumor type
            variationDataDTOSet.add(dData);
        }

        CsvMapper mapper = new CsvMapper();

        CsvSchema schema = CsvSchema.builder()
                .addColumn("Sample ID")
                .addColumn("Passage")
                .addColumn("Histology")
                .addColumn("Tumor type")
                .addColumn("Chromosome")
                .addColumn("Seq. Position")
                .addColumn("Ref Allele")
                .addColumn("Alt Allele")
                .addColumn("Consequence")
                .addColumn("Gene")
                .addColumn("Amino Acid Change")
                .addColumn("Read Depth")
                .addColumn("Allele Freq")
                .addColumn("RS Variant")
                .addColumn("Platform")
                .build().withHeader();


        String output = "CSV output";
        try {
            output = mapper.writer(schema).writeValueAsString(variationDataDTOSet);
        } catch (JsonProcessingException e) {}

        response.setContentType("text/csv;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=pdxfinder.org_variation"+dataSrc+"_"+modelId+".csv");

        return output;

    }


}

