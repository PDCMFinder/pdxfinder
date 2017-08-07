package org.pdxfinder.web.controllers;

import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.services.GraphService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;
import java.util.Set;

/**
 * Created by jmason on 16/03/2017.
 */
@Controller
public class SearchController
{

        private GraphService graphService;
        public SearchController(GraphService graphService) {
            this.graphService = graphService;
        }

        @RequestMapping("/search")
        String index(Model model) throws JSONException
        {


                    //Cancers by system
                    JSONArray cancerBySystemDataSeriesArray = new JSONArray();

                    Map<String, Integer> cancerBySystem = graphService.getModelCountsBySystem();
                    for (String name : cancerBySystem.keySet())
                    {

                        JSONObject dataSeries = new JSONObject();
                            dataSeries.put("name", name);
                            dataSeries.put("y", cancerBySystem.get(name));

                            cancerBySystemDataSeriesArray.put(dataSeries);

                    }
                    model.addAttribute("cancerBySystem", cancerBySystemDataSeriesArray.toString());


                    /************************    Retrieve Mapped DO Terms Start   ************************/

                    Set<String> autoSuggestList = graphService.getMappedDOTerms();
                    model.addAttribute("mappedDOTerm", autoSuggestList);

                    /************************    Retrieve Mapped DO Terms End   ************************/



                    // Cancers by tissue
                    JSONArray cancerByTissueDataSeriesArray = new JSONArray();

                    Map<String, Integer> cancerByTissue = graphService.getModelCountsByTissue();
                    for (String name : cancerByTissue.keySet())
                    {

                            JSONObject dataSeries = new JSONObject();
                            dataSeries.put("name", name);
                            dataSeries.put("y", cancerByTissue.get(name));

                            cancerByTissueDataSeriesArray.put(dataSeries);

                    }
                    model.addAttribute("cancerByTissue", cancerByTissueDataSeriesArray.toString());
                    System.out.println("+++ cancerByTissue" + cancerByTissueDataSeriesArray);



                    JSONObject dataByCellType = new JSONObject();



                return "search";
        }


}





//        JSONArray cancerByCellTypeDataSeriesArray = new JSONArray();
//
//        Map<String, Integer> cancerByCellType = graphService.getModelCountsByCellType();
//        for (String name : cancerByCellType.keySet()) {
//
//            JSONObject dataSeries = new JSONObject();
//            dataSeries.put("name", name);
//            dataSeries.put("y", cancerByCellType.get(name));
//
//            cancerByCellTypeDataSeriesArray.put(dataSeries);
//
//        }
//
//        model.addAttribute("cancerByCellType", cancerByCellTypeDataSeriesArray.toString());
//        System.out.println("+++ cancerByCellType" + cancerByCellTypeDataSeriesArray);



// The data for producing a highcharts pie chart format
// [{
//        name: 'IE',
//                y: 56.33
//    }, {
//        name: 'Chrome',
//                y: 24.03
//    }]
