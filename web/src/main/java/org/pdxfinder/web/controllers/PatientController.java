package org.pdxfinder.web.controllers;

import org.pdxfinder.services.DetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/*
 * Created by abayomi on 16/07/2018.
 */
@Controller
public class PatientController {

    private DetailsService detailsService;

    public PatientController(DetailsService detailsService){
        this.detailsService = detailsService;
    }


    @GetMapping("/patient/{dataSrc}/{patientId}")
    public String patient(Model model,
                          @PathVariable String dataSrc,
                          @PathVariable String patientId,
                          @RequestParam(value = "page", defaultValue = "0") Integer page,
                          @RequestParam(value = "size", defaultValue = "15000") Integer size){

        model.addAttribute("data", detailsService.getModelDetails(dataSrc, patientId, page, size, "", "", ""));

        return "patients";
    }
}
