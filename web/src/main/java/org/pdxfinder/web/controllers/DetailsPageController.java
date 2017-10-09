package org.pdxfinder.web.controllers;

import org.pdxfinder.dao.Specimen;
import org.pdxfinder.services.SearchService;
import org.pdxfinder.services.dto.DetailsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by csaba on 12/05/2017.
 */
@Controller
public class DetailsPageController {

    private SearchService searchService;

    @Autowired
    public DetailsPageController(SearchService searchService) {
        this.searchService = searchService;
    }

    @RequestMapping(value = "/pdx/{dataSrc}/{modelId}")
    public String details(@PathVariable String dataSrc,
                          @PathVariable String modelId,
                          @RequestParam(value="page", required = false) Integer page,Model model){

        int viewPage = (page == null || page < 1) ? 0 : page-1;

        DetailsDTO dto = searchService.searchForModel(dataSrc,modelId,viewPage);

        model.addAttribute("fullData",dto);

        model.addAttribute("modelId",modelId);
        model.addAttribute("dataSrc",dataSrc);

        model.addAttribute("externalId", dto.getExternalId());
        model.addAttribute("dataSource", dto.getDataSource());
        model.addAttribute("patientId", dto.getPatientId());
        model.addAttribute("gender", dto.getGender());
        model.addAttribute("age", dto.getAge());
        model.addAttribute("race", dto.getRace());
        model.addAttribute("ethnicity", dto.getEthnicity());
        model.addAttribute("diagnosis", dto.getDiagnosis());
        model.addAttribute("tumorType", dto.getTumorType());
        model.addAttribute("classification", dto.getClassification());
        model.addAttribute("originTissue", dto.getOriginTissue());
        model.addAttribute("sampleSite", dto.getSampleSite());

        model.addAttribute("sampleType", dto.getSampleType());
        model.addAttribute("strain", dto.getStrain());
        model.addAttribute("mouseSex", dto.getMouseSex());
        model.addAttribute("engraftmentSite", dto.getEngraftmentSite());
        model.addAttribute("markers", dto.getCancerGenomics());
        model.addAttribute("url", dto.getExternalUrl());
        model.addAttribute("urlText", dto.getExternalUrlText());

        //model.addAttribute("specimenId", dto.getSpecimenId());
        for (Specimen specimen : dto.getSpecimens()) {
            model.addAttribute("specimenId",specimen.getExternalId() );
        }

        model.addAttribute("technology", "");
        model.addAttribute("totalPages", dto.getTotalPages());
        model.addAttribute("presentPage", viewPage+1);
        model.addAttribute("totalRecords", dto.getVariationDataCount());

        model.addAttribute("variationData", dto.getMarkerAssociations());

        //TODO: return error page if sampleId does not exist
        return "details";
    }
}