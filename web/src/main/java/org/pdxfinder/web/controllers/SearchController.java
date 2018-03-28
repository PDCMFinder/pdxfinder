package org.pdxfinder.web.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.services.AutoCompleteService;
import org.pdxfinder.services.GraphService;
import org.pdxfinder.services.MolCharService;
import org.pdxfinder.services.ds.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jmason on 16/03/2017.
 */
@Controller
public class SearchController {

    private final static Logger logger = LoggerFactory.getLogger(SearchController.class);

    private GraphService graphService;
    private AutoCompleteService autoCompleteService;
    private SearchDS searchDS;
    private Map<String, List<String>> facets = new HashMap<>();
    private MolCharService molCharService;

    List<String> patientAgeOptions = SearchDS.PATIENT_AGE_OPTIONS;
    List<String> datasourceOptions = SearchDS.DATASOURCE_OPTIONS;
    List<String> cancerBySystemOptions = SearchDS.CANCERS_BY_SYSTEM_OPTIONS;
    List<String> patientGenderOptions = SearchDS.PATIENT_GENDERS;
    List<String> sampleTumorTypeOptions = SearchDS.SAMPLE_TUMOR_TYPE_OPTIONS;
    List<String> diagnosisOptions = SearchDS.DIAGNOSIS_OPTIONS;

    public SearchController(GraphService graphService, SearchDS searchDS, AutoCompleteService autoCompleteService, MolCharService molCharService) {
        this.graphService = graphService;
        this.searchDS = searchDS;
        this.autoCompleteService = autoCompleteService;
        this.molCharService = molCharService;

        facets.put("datasource_options", datasourceOptions);
        facets.put("patient_age_options", patientAgeOptions);
        facets.put("patient_gender_options", patientGenderOptions);
        facets.put("cancer_system_options", cancerBySystemOptions);
        facets.put("sample_tumor_type_options", sampleTumorTypeOptions);

    }

    @RequestMapping("/search/export")
    @ResponseBody
    String export(HttpServletResponse response,
                  @RequestParam("query") Optional<String> query,
                  @RequestParam("datasource") Optional<List<String>> datasource,
                  @RequestParam("diagnosis") Optional<List<String>> diagnosis,
                  @RequestParam("patient_age") Optional<List<String>> patient_age,
                  @RequestParam("patient_treatment_status") Optional<List<String>> patient_treatment_status,
                  @RequestParam("patient_gender") Optional<List<String>> patient_gender,
                  @RequestParam("sample_origin_tissue") Optional<List<String>> sample_origin_tissue,
                  @RequestParam("cancer_system") Optional<List<String>> cancer_system,
                  @RequestParam("sample_tumor_type") Optional<List<String>> sample_tumor_type,
                  @RequestParam("mutation") Optional<List<String>> mutation
    ) {

        Map<SearchFacetName, List<String>> configuredFacets = getFacetMap(
                query,
                datasource,
                diagnosis,
                patient_age,
                patient_treatment_status,
                patient_gender,
                sample_origin_tissue,
                cancer_system,
                sample_tumor_type,
                mutation
        );

        Set<ModelForQuery> results = searchDS.search(configuredFacets);

        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(ModelForQuery.class).withHeader();
        String output = "CSV output for configured values " + configuredFacets.toString();
        try {
            output = mapper.writer(schema).writeValueAsString(results);
        } catch (JsonProcessingException e) {
            logger.error("Could not convert result set to CSV file. Facetes: {}", configuredFacets.toString(), e);
        }

        response.setContentType("text/csv;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=pdxfinder_search_export.csv");

        return output;

    }


    @RequestMapping("/search")
    String search(Model model,
                  @RequestParam("query") Optional<String> query,
                  @RequestParam("datasource") Optional<List<String>> datasource,
                  @RequestParam("diagnosis") Optional<List<String>> diagnosis,
                  @RequestParam("patient_age") Optional<List<String>> patient_age,
                  @RequestParam("patient_treatment_status") Optional<List<String>> patient_treatment_status,
                  @RequestParam("patient_gender") Optional<List<String>> patient_gender,
                  @RequestParam("sample_origin_tissue") Optional<List<String>> sample_origin_tissue,
                  @RequestParam("cancer_system") Optional<List<String>> cancer_system,
                  @RequestParam("sample_tumor_type") Optional<List<String>> sample_tumor_type,
                  @RequestParam("mutation") Optional<List<String>> mutation,

                  @RequestParam(value = "page", defaultValue = "1") Integer page,
                  @RequestParam(value = "size", defaultValue = "10") Integer size
    ) {

        Map<SearchFacetName, List<String>> configuredFacets = getFacetMap(
                query,
                datasource,
                diagnosis,
                patient_age,
                patient_treatment_status,
                patient_gender,
                sample_origin_tissue,
                cancer_system,
                sample_tumor_type,
                mutation
        );



        Set<ModelForQuery> results = searchDS.search(configuredFacets);


        List<FacetOption> patientAgeSelected = searchDS.getFacetOptions(SearchFacetName.patient_age, patientAgeOptions, results, patient_age.orElse(null));
        List<FacetOption> patientGenderSelected = searchDS.getFacetOptions(SearchFacetName.patient_gender, patientGenderOptions, results, patient_gender.orElse(null));
        List<FacetOption> datasourceSelected = searchDS.getFacetOptions(SearchFacetName.datasource, datasourceOptions, results, datasource.orElse(null));
        List<FacetOption> cancerSystemSelected = searchDS.getFacetOptions(SearchFacetName.cancer_system, cancerBySystemOptions, results, cancer_system.orElse(null));
        List<FacetOption> sampleTumorTypeSelected = searchDS.getFacetOptions(SearchFacetName.sample_tumor_type, sampleTumorTypeOptions, results, sample_tumor_type.orElse(null));
        List<FacetOption> mutationSelected = searchDS.getFacetOptions(SearchFacetName.mutation, null, results, mutation.orElse(null));


        // Only add diagnosisSelected if diagnosis has actually been specified
        List<FacetOption> diagnosisSelected = null;
        if (diagnosis.isPresent()) {
            diagnosisSelected = searchDS.getFacetOptions(SearchFacetName.diagnosis, null, results, diagnosis.orElse(null));
        }

        // Ensure to add the facet options to this list so the URL encoding retains the configured options
        String facetString = getFacetString(
                new HashSet<>(
                        Arrays.asList(
                                patientAgeSelected,
                                patientGenderSelected,
                                datasourceSelected,
                                cancerSystemSelected,
                                sampleTumorTypeSelected,
                                mutationSelected

                        )
                )
        );

        //logger.info("Before: "+facetString);
        // If there is a query, append the query parameter to any configured facet string
        if (query.isPresent() && !query.get().isEmpty()) {
            facetString = StringUtils.join(Arrays.asList("query=" + query.get(), facetString), "&");
        }

        // If there is a diagnosis, append the diagnosis parameters to any configured facet string
        if (diagnosis.isPresent() && !diagnosis.get().isEmpty()) {
            for (String diag : diagnosis.get()) {
                facetString = StringUtils.join(Arrays.asList("diagnosis=" + diag, facetString), "&");
            }
        }

        List<String> selectedMutatedMarkerOrder = new ArrayList<>();

        if (mutation.isPresent() && !mutation.get().isEmpty()) {
            List<String> mutList = new ArrayList<>();
            for (String mut : mutation.get()) {
                //logger.info(mut);
                mutList.add("mutation="+mut);
                //facetString = StringUtils.join(Arrays.asList("mutation=" + mut, facetString), "&");
            }

            if(facetString.length() != 0 && !facetString.endsWith("&")) {
                facetString += "&";
            }
            for(String mut: mutList){
                facetString += mut+"&";
            }
            //facetString += StringUtils.join(mutList, "&");

    }
        //logger.info("After: " +facetString);

        // Num pages is converted to an int using this formula int n = a / b + (a % b == 0) ? 0 : 1;
        int numPages = results.size() / size + (results.size() % size == 0 ? 0 : 1);

        // If there are no results, default to 1 page (instead of 0 pages)
        if (numPages < 1) {
            numPages = 1;
        }

        int current = page;
        int begin = Math.max(1, current - 4);
        int end = Math.min(begin + 7, numPages);
        String mutatedMarkers = molCharService.getMutatedMarkersAndVariants();;

        String textSearchDescription = getTextualDescription(facetString, results);

        boolean mutSelected = false;

        if(mutation.isPresent() && !mutation.get().isEmpty()){
            mutSelected = true;
        }

        List<AutoSuggestOption> autoSuggestList = autoCompleteService.getAutoSuggestions();
        List<ModelForQuery> resultSet = new ArrayList<>(results).subList((page - 1) * size, Math.min(((page - 1) * size) + size, results.size()));

        //auto suggestions for the search field
        model.addAttribute("autoCompleteList", autoSuggestList);

        model.addAttribute("numPages", numPages);
        model.addAttribute("beginIndex", begin);
        model.addAttribute("endIndex", end);
        model.addAttribute("currentIndex", current);
        model.addAttribute("totalResults", results.size());
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("facets_string", facetString);
        model.addAttribute("text_search_desc", textSearchDescription);

        model.addAttribute("patient_age_selected", patientAgeSelected);
        model.addAttribute("mutations_selected", mutationSelected);
        model.addAttribute("patient_gender_selected", patientGenderSelected);
        model.addAttribute("datasource_selected", datasourceSelected);
        model.addAttribute("cancer_system_selected", cancerSystemSelected);
        model.addAttribute("sample_tumor_type_selected",sampleTumorTypeSelected);
        model.addAttribute("diagnosis_selected", diagnosisSelected);
        model.addAttribute("isMutationSelected", mutSelected);
        model.addAttribute("query", query.orElse(""));

        model.addAttribute("facet_options", facets);
        model.addAttribute("results", resultSet);
        model.addAttribute("mutatedMarkersAndVariants", mutatedMarkers);
        //model.addAttribute("selectedMutatedMarkerOrder", selectedMutatedMarkerOrder);

        if (mutSelected == true){
            model.addAttribute("platformMap", getPlatformOrMutationFromMutatedVariants(resultSet,"platformMap"));
            model.addAttribute("mutationMap", getPlatformOrMutationFromMutatedVariants(resultSet,"mutationMap"));
        }






        ObjectMapper mapper = new ObjectMapper();
        Map<String, List<String>> mapObject = new HashMap<>();
        try{
            mapObject = mapper.readValue(mutatedMarkers, Map.class);
        }catch (Exception e){}

        String done = "";
        Map<String, List<String>> userChoice = new HashMap<>();
        Map<String, List<String>> allVariants = new LinkedHashMap<>();

        try {
            for (String markerReq : mutation.get()) {

                String marka = markerReq.split("___")[0];
                List<String> variantList = new ArrayList<>();
                String variant = "";

                if (!done.contains(marka)) { // New Marker

                    for (String markerReq2 : mutation.get()) {

                        if (marka.equals(markerReq2.split("___")[0])){

                            variant = markerReq2.split("___")[2];

                            if (variant.equals("ALL")){
                                variantList = mapObject.get(marka);
                            }else {
                                variantList.add(variant);
                            }
                        }

                    }
                    userChoice.put(marka,variantList);
                    allVariants.put(marka,mapObject.get(marka));
                }

                done += marka;
            }
            //System.out.println(userChoice);
        }catch (Exception e){}

        model.addAttribute("markerMap", userChoice);
        model.addAttribute("markerMapWithAllVariants", allVariants);

        return "search";
    }




    private Map<String, List<String>> getPlatformOrMutationFromMutatedVariants(List<ModelForQuery> resultSet, String whichMap){

        Map<String, List<String>> platformMap = new HashMap<>();
        Map<String, List<String>> mutationMap = new HashMap<>();

        for (ModelForQuery mfq : resultSet){

            List<String> dPlatforms = new ArrayList<>();
            List<String> dMutations = new ArrayList<>();

            for (String mutatedVariants : mfq.getMutatedVariants()){
                String[] mv = mutatedVariants.split("\\s+");  // e.g  [Truseq_JAX BRAF V600E, CTP BRAF V600E]
                dPlatforms.add(mv[0]);
                dMutations.add(mv[1]+" "+mv[2]);
            }

            platformMap.put(mfq.getExternalId(), dPlatforms);
            mutationMap.put(mfq.getExternalId(), dMutations);
        }

        if (whichMap.equals("platformMap")){
            return platformMap;
        }else{
            return mutationMap;
        }

    }



    /**
     * Get a string representation of all the configured facets
     *
     * @param allSelectedFacetOptions
     * @return
     */
    private String getFacetString(Set<List<FacetOption>> allSelectedFacetOptions) {
        List<String> pieces = new ArrayList<>();
        for (List<FacetOption> facetOptions : allSelectedFacetOptions) {
            pieces.add(facetOptions.stream()
                    .filter(x -> x.getSelected() != null)
                    .filter(FacetOption::getSelected)
                    .map(x -> x.getFacetType() + "=" + x.getName())
                    .collect(Collectors.joining("&")));
        }
        return pieces.stream().filter(x -> !x.isEmpty()).collect(Collectors.joining("&"));
    }

    public String getTextualDescription(String facetString, Set<ModelForQuery> results) {

        if (StringUtils.isEmpty(facetString)) {
            return null;
        }

        String textDescription = "Your filter for ";
        Map<String, Set<String>> filters = new LinkedHashMap<>();

        for (String urlParams : facetString.split("&")) {
            List<String> pieces = Arrays.asList(urlParams.split("="));
            String key = pieces.get(0);
            String value = pieces.get(1);
            String replacementValue;

            if (!filters.containsKey(key)) {
                filters.put(key, new TreeSet<>());
            }

            if(key.equals("mutation")){

                replacementValue = value.replace("___MUT___", " variant ");
                value = replacementValue;
            }

            filters.get(key).add(value);
        }

        Set<String> dataSourceCounter = new HashSet<>();
        for(ModelForQuery mfq :results){
            dataSourceCounter.add(mfq.getDatasource());
        }

        textDescription += StringUtils
                .join(filters.keySet()
                        .stream()
                        .map(x -> "<b>" + x + ":</b> (" + StringUtils.join(filters.get(x), ", ").replaceAll("\\[", "").replaceAll("\\]", "") + ")")
                        .collect(Collectors.toList()), ", ");

        textDescription += " returned " + results.size() + " result";
        textDescription += results.size() == 1 ? "" : "s";
        textDescription += " in "+dataSourceCounter.size()+" source";
        textDescription += dataSourceCounter.size() == 1 ? "" : "s";

        if(dataSourceCounter.size() != 0) textDescription += " "+dataSourceCounter.toString();

        return textDescription;

    }

    private Map<SearchFacetName, List<String>> getFacetMap(
            Optional<String> query,
            Optional<List<String>> datasource,
            Optional<List<String>> diagnosis,
            Optional<List<String>> patientAge,
            Optional<List<String>> patientTreatmentStatus,
            Optional<List<String>> patientGender,
            Optional<List<String>> sampleOriginTissue,
            Optional<List<String>> cancerSystem,
            Optional<List<String>> sampleTumorType,
            Optional<List<String>> mutation


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

        if (diagnosis.isPresent() && !diagnosis.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.diagnosis, new ArrayList<>());
            for (String s : diagnosis.get()) {
                configuredFacets.get(SearchFacetName.diagnosis).add(s);
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
            configuredFacets.put(SearchFacetName.cancer_system, new ArrayList<>());
            for (String s : cancerSystem.get()) {
                configuredFacets.get(SearchFacetName.cancer_system).add(s);
            }
        }

        if (sampleTumorType.isPresent() && !sampleTumorType.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.sample_tumor_type, new ArrayList<>());
            for (String s : sampleTumorType.get()) {
                configuredFacets.get(SearchFacetName.sample_tumor_type).add(s);
            }
        }

        if (mutation.isPresent() && !mutation.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.mutation, new ArrayList<>());
            for (String s : mutation.get()) {
                configuredFacets.get(SearchFacetName.mutation).add(s);
            }
        }


        return configuredFacets;
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
