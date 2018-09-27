package org.pdxfinder.web.controllers;

import org.pdxfinder.services.DetailsService;
import org.pdxfinder.services.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/*
 * Created by abayomi on 16/07/2018.
 */
@Controller
public class PatientController {

    private PatientService patientService;
    @Autowired
    private DetailsService detailsService;

    public PatientController(PatientService patientService){
        this.patientService = patientService;
    }


    @GetMapping("/patient/{dataSrc}/{patientId:.+}")
    public String patient(Model model,
                          @PathVariable String dataSrc,
                          @PathVariable String patientId){

        model.addAttribute("data", patientService.getPatientDetails(dataSrc, patientId));
        //model.addAttribute("data", detailsService.getModelDetails(dataSrc, patientId, 0, 15000, "", "", ""));

        return "patients";
    }
}
