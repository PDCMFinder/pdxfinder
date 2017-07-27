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


        JSONArray dCancerBySystemDataSeriesArray = new JSONArray();

        Map<String, Integer> cancerBySystemData = graphService.getModelCountsBySystem();
        for (String name : cancerBySystemData.keySet())
        {

            JSONObject indexData = new JSONObject();
            indexData.put("name", name);
            indexData.put("y", cancerBySystemData.get(name));

            dCancerBySystemDataSeriesArray.put(indexData);

        }

        model.addAttribute("cancerBySystem", dCancerBySystemDataSeriesArray.toString());



        return "index";
    }

}
