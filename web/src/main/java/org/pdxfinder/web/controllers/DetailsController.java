package org.pdxfinder.web.controllers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.commons.lang3.text.WordUtils;
import org.pdxfinder.services.*;
import org.pdxfinder.services.dto.DetailsDTO;
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
                          @RequestParam(value="page", required = false) Integer page,
                          @RequestParam(value="size", required = false) Integer size){

        int viewPage = (page == null || page < 1) ? 0 : page-1;
        int viewSize = (size == null || size < 1) ? 15000 : size;

        DetailsDTO dto = detailsService.searchForModel(dataSrc,modelId,viewPage,viewSize,"","","");
        model.addAttribute("modelDetails",dto);

        model.addAttribute("mappedTerm", dto.getAutoSuggestList());
        model.addAttribute("nonjsVariationdata", dto.getVariationDataDTOList());
        model.addAttribute("modelId",modelId);
        model.addAttribute("dataSrc",dataSrc);
        model.addAttribute("externalId", dto.getExternalId());
        model.addAttribute("dataSource", dto.getDataSource());
        model.addAttribute("patientId", dto.getPatientId());
        model.addAttribute("gender", dto.getGender());
        model.addAttribute("age", dto.getAgeAtCollection());
        model.addAttribute("race", dto.getRace());
        model.addAttribute("ethnicity", dto.getEthnicity());
        model.addAttribute("diagnosis", dto.getDiagnosis());
        model.addAttribute("tumorType", dto.getTumorType());
        model.addAttribute("class", dto.getClassification());
        model.addAttribute("originTissue", dto.getOriginTissue());
        model.addAttribute("sampleSite", dto.getSampleSite());

        model.addAttribute("sampleType", dto.getSampleType());
        model.addAttribute("strain", dto.getStrain());
        model.addAttribute("mouseSex", dto.getMouseSex());
        model.addAttribute("engraftmentSite", dto.getEngraftmentSite());
        model.addAttribute("markers", dto.getCancerGenomics());
        model.addAttribute("url", dto.getExternalUrl());
        model.addAttribute("urlText", dto.getExternalUrlText());
        model.addAttribute("mappedOntology", dto.getMappedOntology());

        model.addAttribute("totalPages", dto.getTotalPages());
        model.addAttribute("presentPage", dto.getPresentPage());
        model.addAttribute("totalRecords", dto.getVariationDataCount());

        model.addAttribute("variationData", dto.getMarkerAssociations());

        model.addAttribute("modelInfo", dto.getModelTechAndPassages());
        model.addAttribute("patientInfo", dto.getPatientTech());

        model.addAttribute("relatedModels", dto.getRelatedModels());

        model.addAttribute("qualityAssurace", dto.getQualityAssurances());

        model.addAttribute("sampleIdMap",dto.getTechNPassToSampleId());

        model.addAttribute("drugSummary", dto.getDrugSummary());
        model.addAttribute("drugSummaryRowNumber", dto.getDrugSummaryRowNumber());
        model.addAttribute("drugProtocolUrl", dto.getDrugProtocolUrl());
        model.addAttribute("platformsAndUrls", dto.getPlatformsAndUrls());


        Map<String, String> sorceDesc = new HashMap<>();
        sorceDesc.put("JAX","The Jackson Laboratory");
        sorceDesc.put("PDXNet-HCI-BCM","HCI-Baylor College of Medicine");
        sorceDesc.put("PDXNet-Wistar-MDAnderson-Penn","Melanoma PDX established by the Wistar/MD Anderson/Penn");
        sorceDesc.put("PDXNet-WUSTL","Washington University in St. Louis");
        sorceDesc.put("PDXNet-MDAnderson","University of Texas MD Anderson Cancer Center");
        sorceDesc.put("PDMR","NCI Patient-Derived Models Repository");
        sorceDesc.put("IRCC","Candiolo Cancer Institute");


        model.addAttribute("sourceDescription", sorceDesc.get(dto.getDataSource()));
        model.addAttribute("contacts",dto.getContacts());


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
        String diagnosis = detailsService.searchForModel(dataSrc,modelId,0,50000,"","","").getDiagnosis();

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

