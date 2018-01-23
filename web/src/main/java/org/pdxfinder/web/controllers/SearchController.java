package org.pdxfinder.web.controllers;

import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.services.GraphService;
import org.pdxfinder.services.ds.ModelForQuery;
import org.pdxfinder.services.ds.SearchDS;
import org.pdxfinder.services.ds.SearchFacetName;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

/**
 * Created by jmason on 16/03/2017.
 */
@Controller
public class SearchController {

    private GraphService graphService;
    private SearchDS searchDS;

    public SearchController(GraphService graphService, SearchDS searchDS) {
        this.graphService = graphService;
        this.searchDS = searchDS;
    }

    @RequestMapping("/search")
    String search(Model model,
                  @RequestParam("datasource") Optional<List<String>> datasource,
                  @RequestParam("patient_age") Optional<List<String>> patient_age,
                  @RequestParam("patient_treatment_status") Optional<List<String>> patient_treatment_status,
                  @RequestParam("patient_gender") Optional<List<String>> patient_gender,
                  @RequestParam("sample_origin_tissue") Optional<List<String>> sample_origin_tissue
    ) {

//        Map<SearchFacetName, List<String>> configuredFacets = null; //getFacetsFromString(facets);
        Map<SearchFacetName, List<String>> configuredFacets = getFacetMap(
                datasource,
                patient_age,
                patient_treatment_status,
                patient_gender,
                sample_origin_tissue
        );

        System.out.println(datasource);


        Set<ModelForQuery> results = searchDS.search(configuredFacets);

        List<FacetOption> patientAgeOptions = getPatientAgeOptions(results);

        model.addAttribute("patient_age_options", patientAgeOptions);

        return "search";
    }

    private Map<SearchFacetName, List<String>> getFacetMap(
            Optional<List<String>> datasource,
            Optional<List<String>> patientAge,
            Optional<List<String>> patientTreatmentStatus,
            Optional<List<String>> patientGender,
            Optional<List<String>> sampleOriginTissue
    ) {
        Map<SearchFacetName, List<String>> configuredFacets = new HashMap<>();

        if (datasource.isPresent() && !datasource.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.datasource, new ArrayList<>());
            for (String s : datasource.get()) {
                configuredFacets.get(SearchFacetName.datasource).add(s);
            }
        }

        if (patientAge.isPresent() && !patientAge.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.patient_age, new ArrayList<>());
            for (String s : patientAge.get()) {
                configuredFacets.get(SearchFacetName.patient_age).add(s);
            }
        }

        if (patientTreatmentStatus.isPresent() && !patientTreatmentStatus.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.patient_treatment_status, new ArrayList<>());
            for (String s : patientTreatmentStatus.get()) {
                configuredFacets.get(SearchFacetName.patient_treatment_status).add(s);
            }
        }

        if (patientGender.isPresent() && !patientGender.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.patient_gender, new ArrayList<>());
            for (String s : patientGender.get()) {
                configuredFacets.get(SearchFacetName.patient_gender).add(s);
            }
        }

        if (sampleOriginTissue.isPresent() && !sampleOriginTissue.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.sample_origin_tissue, new ArrayList<>());
            for (String s : sampleOriginTissue.get()) {
                configuredFacets.get(SearchFacetName.sample_origin_tissue).add(s);
            }
        }

        return configuredFacets;
    }


    private Map<SearchFacetName, List<String>> getFacetsFromString(Optional<String> facets) {
        Map<SearchFacetName, List<String>> configuredFacets = new HashMap<>();


        if (facets.isPresent() && !facets.get().isEmpty()) {
            // parse the facets string into map of lists of facet options selected


        }

        return configuredFacets;
    }

    private List<FacetOption> getPatientAgeOptions(Set<ModelForQuery> results) {

        List<FacetOption> map = new ArrayList<>();

        for (ModelForQuery mfq : results) {
            String s = mfq.getPatientAge();

            // Initialise on the first time we see this facet name
            if (map.stream().noneMatch(x -> x.getName().equals(s))) {
                map.add(new FacetOption(s, 0));
            }

            // There should be only one element per facet name
            map.forEach(x -> {
                if (x.getName().equals(s)) {
                    x.increment();
                }
            });
        }
        Collections.sort(map);

        return map;
    }

    @RequestMapping("/search2")
    String index(Model model) throws JSONException {


        //Cancers by system
        JSONArray cancerBySystemDataSeriesArray = new JSONArray();

        Map<String, Integer> cancerBySystem = graphService.getModelCountsBySystem();
        for (String name : cancerBySystem.keySet()) {

            JSONObject dataSeries = new JSONObject();
            dataSeries.put("name", name);
            dataSeries.put("y", cancerBySystem.get(name));

            cancerBySystemDataSeriesArray.put(dataSeries);
        }
        model.addAttribute("cancerBySystem", cancerBySystemDataSeriesArray.toString());


        /**
         * Retrieve Mapped NCIT Terms
         *
         */

        Set<String> autoSuggestList = graphService.getMappedNCITTerms();
        model.addAttribute("mappedDOTerm", autoSuggestList);


        // Cancers by tissue
        JSONArray cancerByTissueDataSeriesArray = new JSONArray();

        Map<String, Integer> cancerByTissue = graphService.getModelCountsByTissue();
        for (String name : cancerByTissue.keySet()) {

            JSONObject dataSeries = new JSONObject();
            dataSeries.put("y", cancerByTissue.get(name));
            dataSeries.put("name", name);

            cancerByTissueDataSeriesArray.put(dataSeries);

        }
        model.addAttribute("cancerByTissue", cancerByTissueDataSeriesArray.toString());
        System.out.println("+++ cancerByTissue" + cancerByTissueDataSeriesArray);


        JSONObject dataByCellType = new JSONObject();


             /*
             Get datasource abbreviations dynamically
              */

        List<String> dataSources = graphService.getDataSourceAbbreviations();
        model.addAttribute("dataSources", dataSources);


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
