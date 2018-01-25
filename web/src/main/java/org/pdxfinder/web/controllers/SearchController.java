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
    private Map<String, List<String>> facets = new HashMap<>();

    private List<String> patientAgeOptions = Arrays.asList(
            "0-9",
            "10-19",
            "20-29",
            "30-39",
            "40-49",
            "50-59",
            "60-69",
            "70-79",
            "80-89",
            "90+",
            "NA"
    );
    List<String> datasourceOptions = Arrays.asList(
            "JAX",
            "IRCC",
            "PDMR",
            "PDXNet-HCI-BCM",
            "PDXNet-MDAnderson",
            "PDXNet-WUSTL",
            "PDXNet-Wistar-MDAnderson-Penn"
    );

    List<String> cancerBySystemOptions = SearchDS.CANCERS_BY_SYSTEM;
    List<String> patientGenderOptions = SearchDS.PATIENT_GENDERS;

    public SearchController(GraphService graphService, SearchDS searchDS) {
        this.graphService = graphService;
        this.searchDS = searchDS;



        facets.put("datasource_options", datasourceOptions);
        facets.put("patient_age_options", patientAgeOptions);
        facets.put("cancer_system_options", cancerBySystemOptions);

    }

    @RequestMapping("/search")
    String search(Model model,
                  @RequestParam("query") Optional<String> query,
                  @RequestParam("datasource") Optional<List<String>> datasource,
                  @RequestParam("patient_age") Optional<List<String>> patient_age,
                  @RequestParam("patient_treatment_status") Optional<List<String>> patient_treatment_status,
                  @RequestParam("patient_gender") Optional<List<String>> patient_gender,
                  @RequestParam("sample_origin_tissue") Optional<List<String>> sample_origin_tissue,
                  @RequestParam("cancer_system") Optional<List<String>> cancer_system
    ) {

        Map<SearchFacetName, List<String>> configuredFacets = getFacetMap(
                query,
                datasource,
                patient_age,
                patient_treatment_status,
                patient_gender,
                sample_origin_tissue,
                cancer_system
        );


        Set<ModelForQuery> results = searchDS.search(configuredFacets);

        List<FacetOption> patientAgeSelected = getFacetOptions(SearchFacetName.patient_age, patientAgeOptions, results, patient_age.orElse(null));
        List<FacetOption> patientGenderSelected = getFacetOptions(SearchFacetName.patient_gender, patientGenderOptions, results, patient_gender.orElse(null));
        List<FacetOption> datasourceSelected = getFacetOptions(SearchFacetName.datasource, datasourceOptions, results, datasource.orElse(null));
        List<FacetOption> cancerSystemSelected = getFacetOptions(SearchFacetName.system, cancerBySystemOptions, results, cancer_system.orElse(null));

        model.addAttribute("patient_age_selected", patientAgeSelected);
        model.addAttribute("patient_gender_selected", patientGenderSelected);
        model.addAttribute("datasource_selected", datasourceSelected);
        model.addAttribute("cancer_system_selected", cancerSystemSelected);
        model.addAttribute("query", query);

        model.addAttribute("facet_options", facets);
        model.addAttribute("results", results);

        return "search";
    }

    private Map<SearchFacetName, List<String>> getFacetMap(
            Optional<String> query,
            Optional<List<String>> datasource,
            Optional<List<String>> patientAge,
            Optional<List<String>> patientTreatmentStatus,
            Optional<List<String>> patientGender,
            Optional<List<String>> sampleOriginTissue,
            Optional<List<String>> cancerSystem
    ) {

        Map<SearchFacetName, List<String>> configuredFacets = new HashMap<>();

        if (query.isPresent() && !query.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.query, new ArrayList<>());
            configuredFacets.get(SearchFacetName.query).add(query.get());
        }

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

        if (cancerSystem.isPresent() && !cancerSystem.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.system, new ArrayList<>());
            for (String s : cancerSystem.get()) {
                configuredFacets.get(SearchFacetName.system).add(s);
            }
        }

        return configuredFacets;
    }


    /**
     * Get the facet lists for a passed in facet argument
     *
     * @param facet
     * @param results
     * @param selected
     * @return
     */
    private List<FacetOption> getFacetOptions(SearchFacetName facet, List<String> options, Set<ModelForQuery> results, List<String> selected) {

        List<FacetOption> map = new ArrayList<>();

        // Initialise all facet option counts to 0 and set selected attribute on all options that the user has chosen
        for (String option : options) {
            map.add(new FacetOption(option, 0, selected != null && selected.contains(option) ? Boolean.TRUE : Boolean.FALSE));
        }

        // Iterate through results adding count to the appropriate option
        for (ModelForQuery mfq : results) {

            String s = mfq.getBy(facet);

            // List of ontology terms may come from the service.  These will by separated by "::" delimiter
            if (s.contains("::")) {

                for (String ss : s.split("::")) {

                    // There should be only one element per facet name
                    map.forEach(x -> {
                        if (x.getName().equals(ss)) {
                            x.increment();
                        }
                    });
                }

            } else {

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
        }


//        Collections.sort(map);

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
