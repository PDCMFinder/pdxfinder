package org.pdxfinder.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

/**
 * Created by csaba on 12/05/2017.
 */
@Controller
public class DetailsPageController {

    @RequestMapping(value = "/details/{sampleId}")
    public String details(@PathVariable String sampleId, Model model) {

        model.addAttribute("sampleId",sampleId);

        return "details";
    }
}
