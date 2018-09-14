package org.pdxfinder.web.controllers;

import org.pdxfinder.services.PatientService;
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

    public PatientController(PatientService patientService){
        this.patientService = patientService;
    }


    @GetMapping("/patient/{dataSrc}/{patientId}")
    public String patient(Model model,
                          @PathVariable String dataSrc,
                          @PathVariable String patientId){

        model.addAttribute("data", patientService.getPatientDetails(dataSrc, patientId));

        return "patients";
    }
}
