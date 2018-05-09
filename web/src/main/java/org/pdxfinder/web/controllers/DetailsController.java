package org.pdxfinder.web.controllers;


import org.pdxfinder.services.DetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

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
                          @RequestParam(value = "size", defaultValue = "15000") Integer size) {

        model.addAttribute("data", detailsService.getModelDetails(dataSrc, modelId, page, size, "", "", ""));
        return "details";
    }


    @RequestMapping(method = RequestMethod.GET, value = "/pdx/{dataSrc}/{modelId}/export")
    @ResponseBody
    public String download(HttpServletResponse response,
                           @PathVariable String dataSrc,
                           @PathVariable String modelId) {

        response.setContentType("text/csv;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=pdxfinder.org_variation" + dataSrc + "_" + modelId + ".csv");

        return detailsService.getVariationDataCSV(dataSrc, modelId);
    }


}

