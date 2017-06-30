package org.pdxfinder.web.controllers;

import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.services.GraphService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * Created by abayomi on 27/06/2017.
 */


@Controller
public class IndexController {

    private GraphService graphService;

    public IndexController(GraphService graphService) {
        this.graphService = graphService;
    }

    @RequestMapping("/")
    String index(Model model)  throws JSONException
    {

        //
        // Cancers by system
        //
        JSONArray cancerBySystemDataSeriesArray = new JSONArray();

        Map<String, Integer> cancerBySystem = graphService.getModelCountsBySystem();
        for (String name : cancerBySystem.keySet()) {

            JSONObject dataSeries = new JSONObject();
            dataSeries.put("name", name);
            dataSeries.put("y", cancerBySystem.get(name));

            cancerBySystemDataSeriesArray.put(dataSeries);

        }

        model.addAttribute("cancerBySystem", cancerBySystemDataSeriesArray.toString());



        return "index";
    }

}
