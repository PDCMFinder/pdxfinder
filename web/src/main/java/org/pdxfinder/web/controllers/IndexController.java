package org.pdxfinder.web.controllers;

import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.services.GraphService;
import org.pdxfinder.services.SearchService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.Map;


@Controller
public class IndexController {



    private GraphService graphService;
    private SearchService searchService;

    public IndexController(GraphService graphService, SearchService searchService) {
        this.graphService = graphService;
        this.searchService = searchService;
    }

    @RequestMapping("/")
    String index(Model model,HttpSession session)  throws JSONException
    {


        JSONArray dCancerBySystemDataSeriesArray = new JSONArray();

        Map<String, Integer> cancerBySystemData = graphService.getModelCountsBySystem();
        for (String name : cancerBySystemData.keySet()) {

            JSONObject indexData = new JSONObject();
            indexData.put("name", name);
            indexData.put("y", cancerBySystemData.get(name));

            dCancerBySystemDataSeriesArray.put(indexData);

        }

        model.addAttribute("cancerBySystem", dCancerBySystemDataSeriesArray.toString());

        model.addAttribute("modelCount", getNumModels());

        return "index";
    }


    @Cacheable
    private Integer getNumModels()
    {

        Integer numModels;
        int pdxCount = searchService.modelCount();
        pdxCount -= (pdxCount % 100);
        numModels = pdxCount;
        return numModels;
    }

}
