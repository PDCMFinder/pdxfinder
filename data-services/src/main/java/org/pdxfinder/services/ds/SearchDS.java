package org.pdxfinder.services.ds;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.dao.DataProjection;
import org.pdxfinder.dao.OntologyTerm;
import org.pdxfinder.repositories.DataProjectionRepository;
import org.pdxfinder.services.search.WebFacetSection;
import org.pdxfinder.services.search.WebFacetContainer;
import org.pdxfinder.services.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Long.parseLong;


/*
 * Created by csaba on 19/01/2018.
 */

@Component
public class SearchDS {

    private final static Logger log = LoggerFactory.getLogger(SearchDS.class);
    private DataProjectionRepository dataProjectionRepository;

    /**
     * The DS is initialized if all filters and search objects are initialized
     */
    private boolean INITIALIZED = false;


    /**
     * A set of MFQ objects. These objects are being returned after performing a search.
     */
    private Set<ModelForQuery> models;


    /**
     * This container has the definition of the structure and the content of the filters as well as has info on what filter is selected
     */
    private WebFacetContainer webFacetContainer;


    // SEARCH OBJECTS:


    /**
     * A general one param search object that is being used when search is performed on a MFQ object field
     */
    private OneParamSearch oneParamSearch;

    /**
     * Three param search object for performing a search on gene mutations
     */
    private ThreeParamSearch geneMutationSearch;

    /**
     * Two param search object for performing a search on dosing studies
     */
    private TwoParamUnlinkedSearch dosingStudySearch;





    public SearchDS(DataProjectionRepository dataProjectionRepository) {
        Assert.notNull(dataProjectionRepository, "Data projection repository cannot be null");

        this.dataProjectionRepository = dataProjectionRepository;
        this.models = new HashSet<>();
    }

    public void init(){


        //INITIALIZE MODEL FOR QUERY OBJECTS FIRST
        initializeModels();
        //now we can use MFQ objects to get additional values for filters


        /****************************************************************
         *     INITIALIZE FILTER OPTIONS AND FILTER STRUCTURE           *
         ****************************************************************/

        webFacetContainer = new WebFacetContainer();

        WebFacetSection patientTumorSection = new WebFacetSection();
        patientTumorSection.setName("PATIENT / TUMOR");

        WebFacetSection pdxModelSection = new WebFacetSection();
        pdxModelSection.setName("PDX MODEL");

        WebFacetSection molecularDataSection = new WebFacetSection();
        molecularDataSection.setName("MOLECULAR DATA");

        WebFacetSection treatmentInfoSection = new WebFacetSection();
        treatmentInfoSection.setName("TREATMENT INFORMATION");

        //cancer by system filter def
        OneParamFilter cancerBySystem = new OneParamFilter("CANCER BY SYSTEM", "cancer_system",
                Arrays.asList(
                        "Breast Cancer",
                        "Cardiovascular Cancer",
                        "Connective and Soft Tissue Cancer",
                        "Digestive System Cancer",
                        "Endocrine Cancer",
                        "Eye Cancer",
                        "Head and Neck Cancer",
                        "Hematopoietic and Lymphoid System Cancer",
                        "Nervous System Cancer",
                        "Peritoneal and Retroperitoneal Cancer",
                        "Reproductive System Cancer",
                        "Respiratory Tract Cancer",
                        "Thoracic Cancer",
                        "Skin Cancer",
                        "Urinary System Cancer",
                        "Unclassified"),
                new ArrayList<>());
        patientTumorSection.addComponent(cancerBySystem);


        //tumor type filter def
        OneParamFilter tumorType = new OneParamFilter("TUMOR_TYPE", "sample_tumor_type",
                Arrays.asList(
                        "Primary",
                        "Metastatic",
                        "Recurrent",
                        "Refractory",
                        "Not Specified"
                ),
                new ArrayList<>());
        patientTumorSection.addComponent(tumorType);


        //sex filter def
        OneParamFilter sex = new OneParamFilter("SEX", "patient_gender",
                Arrays.asList(
                        "Male",
                        "Female",
                        "Not Specified"
                ),
                new ArrayList<>());
        patientTumorSection.addComponent(sex);


        //age filter def
        OneParamFilter age = new OneParamFilter("AGE", "patient_age",
                Arrays.asList(
                        "0-9",
                        "10-19",
                        "20-29",
                        "30-39",
                        "40-49",
                        "50-59",
                        "60-69",
                        "70-79",
                        "80-89",
                        "90",
                        "Not Specified"
                ),
                new ArrayList<>());
        patientTumorSection.addComponent(age);


        //datasource filter def
        Set<String> datasourceSet = models.stream()
                .map(ModelForQuery::getDatasource)
                .collect(Collectors.toSet());

        List<String> datasourceList = new ArrayList<>();
        datasourceList.addAll(datasourceSet);
        Collections.sort(datasourceList);

        OneParamFilter datasource = new OneParamFilter("DATASOURCE", "datasource", datasourceList, new ArrayList<>());
        pdxModelSection.addComponent(datasource);


        //project filter def
        Set<String> projectsSet = new HashSet<>();
        for(ModelForQuery mfk : models){

            if(mfk.getProjects() != null){
                for(String s: mfk.getProjects()){
                    projectsSet.add(s);
                }
            }
        }
        List<String> projectList = new ArrayList<>(projectsSet);
        Collections.sort(projectList);

        //TODO: skip filter if no projects were defined?

        OneParamFilter projects = new OneParamFilter("PROJECT", "project", projectList, new ArrayList<>());
        pdxModelSection.addComponent(projects);


        //dataset available filter def
        OneParamFilter datasetAvailable = new OneParamFilter("DATASET AVAILABLE", "data_available",
                Arrays.asList(
                        "Gene Mutation",
                        "Dosing Studies",
                        "Patient Treatment"),
                new ArrayList<>());

        pdxModelSection.addComponent(datasetAvailable);


        //gene mutation filter def
        //TODO: look up platforms, genes and variants
        ThreeParamFilter geneMutation = new ThreeParamFilter("GENE MUTATION", "mutation", new HashMap<>(), new HashMap<>());

        molecularDataSection.addComponent(geneMutation);


        //model dosing study def

        Map<String, Map<String, Set<Long>>> modelDrugResponses = getModelDrugResponsesFromDP();
        List<String> drugNames = new ArrayList<>(modelDrugResponses.keySet());

        TwoParamUnlinkedFilter modelDosingStudy = new TwoParamUnlinkedFilter("MODEL DOSING STUDY", "drug", drugNames, Arrays.asList(
                "Complete Response",
                "Partial Response",
                "Progressive Disease",
                "Stable Disease",
                "Stable Disease And Complete Response"
        ), new HashMap<>());
        treatmentInfoSection.addComponent(modelDosingStudy);


        webFacetContainer.addSection(patientTumorSection);
        webFacetContainer.addSection(pdxModelSection);
        webFacetContainer.addSection(molecularDataSection);
        webFacetContainer.addSection(treatmentInfoSection);

        /****************************************************************
         *            INITIALIZE SEARCH OBJECTS                         *
         ****************************************************************/


        //one general search object for searching on MFQ object fields
        oneParamSearch = new OneParamSearch(null, null);

        //drug search
        dosingStudySearch = new TwoParamUnlinkedSearch();
        dosingStudySearch.setData(getModelDrugResponsesFromDP());

        //gene mutation search
        geneMutationSearch = new ThreeParamSearch("geneMutation", "mutation");
        geneMutationSearch.setData(getMutationsFromDP());

        /*
        List dsTestList = new ArrayList();
        dsTestList.add("JAX");
        Set<ModelForQuery> results = oneParamSearch.searchOnString(Arrays.asList("JAX"), models, ModelForQuery::getDatasource);

        log.info("Searching for JAX DS");
        log.info(results.toString());
        */




        INITIALIZED = true;
    }


    public void updateSelectedFilters(SearchFacetName facetName, List<String> filters){

        for(WebFacetSection wfs :webFacetContainer.getWebFacetSections()){
            for(GeneralFilter filter: wfs.getFilterComponents()){

                if(filter.getUrlParam().equals(facetName.getName())){

                    if(filter instanceof OneParamFilter){

                        OneParamFilter f = (OneParamFilter)filter;
                        f.setSelected(filters);

                    }
                    else if(filter instanceof TwoParamUnlinkedFilter){

                        TwoParamUnlinkedFilter f = (TwoParamUnlinkedFilter) filter;
                        //TODO: Implement updating two and three parameter filters

                    }

                }

            }
        }
    }



    public Set<ModelForQuery> search(Map<SearchFacetName, List<String>> filters){

        synchronized (this){
            if(! INITIALIZED ) {
                init();
            }
        }

        Set<ModelForQuery> result = new HashSet<>(models);

        //empty previously set variants
        result.forEach(x -> x.setMutatedVariants(new ArrayList<>()));

        //empty previously set drugs
        result.forEach(x -> x.setDrugData(new ArrayList<>()));

        // If no filters have been specified, return the complete set
        if (filters == null) {
            return result;
        }

        OneParamSearch oneParamSearch = new OneParamSearch("search","search");

        for (SearchFacetName facet : filters.keySet()) {


            switch(facet){

                case query:
                    //List<String> searchParams, Set<ModelForQuery> mfqSet, Function<ModelForQuery, List<String>> searchFunc
                    List<String> ancestors = new ArrayList<>();
                    result = oneParamSearch.searchOnCollection(filters.get(SearchFacetName.query), result, ModelForQuery::getAllOntologyTermAncestors);
                    break;





            }



        }



        return new HashSet<>();
    }




    /*
     * DYNAMIC FACETS
     */

    //private Map<String, String> cancerSystemMap = new HashMap<>();

    //platform=> marker=> variant=>{set of model ids}
    //Map<String, Map<String, Map<String, Set<Long>>>> mutations = new HashMap<String, Map<String, Map<String, Set<Long>>>>();

    //"drugname"=>"response"=>"set of model ids"
    //private Map<String, Map<String, Set<Long>>> modelDrugResponses = new HashMap<>();

    //private List<String> projectOptions = new ArrayList<>();


    /**
     * Populate the complete set of models for searching when this object is instantiated
     */


    /*
    void initialize() {


        //this method loads the ModelForQuery Data Projection object and
        initializeModels();

        //loads the mutation map from Data Projection
        initializeMutations();


        //loads model drug response from Data Projection
        initializeModelDrugResponses();

        //loads projects
        initializeAdditionalOptions();


        List<String> padding = new ArrayList<>();
        padding.add("NO DATA");




        //PROJECT_OPTIONS =new ArrayList<>(models.stream().map(model -> model.getProjects()).flatMap(Collection::stream).collect(Collectors.toSet()));

        INITIALIZED = true;

    }
/*
    /**
     * Recursively get all ancestors starting from the supplied ontology term
     *
     * @param t the starting term in the ontology
     * @return a set of ontology terms corresponding to the ancestors of the term supplied
     */
    public Set<OntologyTerm> getAllAncestors(OntologyTerm t) {

        Set<OntologyTerm> retSet = new HashSet<>();

        // Store this ontology term in the set
        retSet.add(t);

        // If this term has parent terms
        if (t.getSubclassOf() != null && t.getSubclassOf().size() > 0) {

            // For each parent term
            for (OntologyTerm st : t.getSubclassOf()) {

                // Recurse and add all ancestor terms to the set
                retSet.addAll(getAllAncestors(st));
            }
        }

        // Return the full set
        return retSet;
    }

    public Set<ModelForQuery> getModels() {

        synchronized (this){
            if(! INITIALIZED ) {
                init();
            }
        }

        return models;
    }

    public void setModels(Set<ModelForQuery> models) {
        this.models = models;
    }




    /**
     * This method loads the ModelForQuery Data Projection object and initializes the models
     */
    void initializeModels() {


        String modelJson = dataProjectionRepository.findByLabel("ModelForQuery").getValue();

        try {
            JSONArray jarray = new JSONArray(modelJson);

            for (int i = 0; i < jarray.length(); i++) {

                JSONObject j = jarray.getJSONObject(i);

                ModelForQuery mfq = new ModelForQuery();

                mfq.setModelId(parseLong(j.getString("modelId")));
                mfq.setDatasource(j.getString("datasource"));
                mfq.setExternalId(j.getString("externalId"));
                mfq.setPatientAge(j.getString("patientAge"));
                mfq.setPatientGender(j.getString("patientGender"));

                if(j.has("patientEthnicity")){
                    mfq.setPatientEthnicity(j.getString("patientEthnicity"));
                }

                mfq.setSampleOriginTissue(j.getString("sampleOriginTissue"));
                mfq.setSampleSampleSite(j.getString("sampleSampleSite"));
                mfq.setSampleExtractionMethod(j.getString("sampleExtractionMethod"));
                //mfq.setSampleClassification(j.getString("sampleClassification"));
                mfq.setSampleTumorType(j.getString("sampleTumorType"));
                mfq.setDiagnosis(j.getString("diagnosis"));
                mfq.setMappedOntologyTerm(j.getString("mappedOntologyTerm"));
                mfq.setTreatmentHistory(j.getString("treatmentHistory"));


                JSONArray ja = j.getJSONArray("cancerSystem");
                List<String> cancerSystem = new ArrayList<>();
                for (int k = 0; k < ja.length(); k++) {

                    cancerSystem.add(ja.getString(k));
                }

                mfq.setCancerSystem(cancerSystem);

                ja = j.getJSONArray("allOntologyTermAncestors");
                Set<String> ancestors = new HashSet<>();

                for (int k = 0; k < ja.length(); k++) {

                    ancestors.add(ja.getString(k));
                }

                mfq.setAllOntologyTermAncestors(ancestors);

                if(j.has("dataAvailable")){
                    ja = j.getJSONArray("dataAvailable");
                    List<String> dataAvailable = new ArrayList<>();

                    for(int k=0; k<ja.length(); k++){

                        dataAvailable.add(ja.getString(k));
                    }

                    mfq.setDataAvailable(dataAvailable);
                }

                if(j.has("projects")){

                    ja = j.getJSONArray("projects");

                    for(int k = 0; k < ja.length(); k++){
                        mfq.addProject(ja.getString(k));
                    }

                }


                this.models.add(mfq);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private Map<String, Map<String, Map<String, Set<Long>>>> getMutationsFromDP(){

        log.info("Initializing mutations");
        //platform=> marker=> variant=>{set of model ids}
        Map<String, Map<String, Map<String, Set<Long>>>> mutations = new HashMap<>();

        String mut = dataProjectionRepository.findByLabel("PlatformMarkerVariantModel").getValue();

        try{

            ObjectMapper mapper = new ObjectMapper();

            mutations = mapper.readValue(mut, new TypeReference<Map<String, Map<String, Map<String, Set<Long>>>>>(){});

            //log.info("Lookup: "+mutations.get("TargetedNGS_MUT").get("RB1").get("N123D").toString());

        }
        catch(Exception e){

            e.printStackTrace();
        }

        return mutations;
    }


    Map<String, Map<String, Set<Long>>> getModelDrugResponsesFromDP(){

        log.info("Initializing model drug responses");

        Map<String, Map<String, Set<Long>>> modelDrugResponses = new HashMap<>();

        DataProjection dataProjection = dataProjectionRepository.findByLabel("ModelDrugData");
        String responses = "{}";

        if(dataProjection != null){

            responses = dataProjection.getValue();
        }

        try{

            ObjectMapper mapper = new ObjectMapper();

            modelDrugResponses = mapper.readValue(responses, new TypeReference<Map<String, Map<String, Set<Long>>>>(){});

            //log.info("Lookup: "+modelDrugResponses.get("doxorubicincyclophosphamide").get("progressive disease").toString());

        }
        catch(Exception e){

            e.printStackTrace();
        }

        return modelDrugResponses;
    }



        /**
         * Takes a marker and a variant. Looks up these variants in mutation.
         * Then creates a hashmap with modelids as keys and platform+marker+mutation as values
         *
         *
         * @param marker
         * @param variant
         * @return
         */

  /*
    private void getModelsByMutatedMarkerAndVariant(String marker, String variant, Map<Long, Set<String>> previouslyFoundModels){

        //platform=> marker=> variant=>{set of model ids}

        //marker with ALL variants
        if(variant.toLowerCase().equals("all")){

            for(Map.Entry<String, Map<String, Map<String, Set<Long>>>> platformEntry : mutations.entrySet()){

                String platformName = platformEntry.getKey();

                if(platformEntry.getValue().containsKey(marker)){

                    for(Map.Entry<String, Set<Long>> markerVariants : platformEntry.getValue().get(marker).entrySet()){

                        String variantName = markerVariants.getKey();
                        Set<Long> foundModels = markerVariants.getValue();

                        for(Long modelId : foundModels){

                            if(previouslyFoundModels.containsKey(modelId)){

                                previouslyFoundModels.get(modelId).add(platformName+" "+marker+" "+variantName);
                            }
                            else{

                                Set<String>  newSet = new HashSet<>();
                                newSet.add(platformName+" "+marker+" "+variantName);
                                previouslyFoundModels.put(modelId, newSet);
                            }

                        }

                    }


                }

            }


        }
        //a marker and a variant is given
        else{

            for(Map.Entry<String, Map<String, Map<String, Set<Long>>>> platformEntry : mutations.entrySet()){

                String platformName = platformEntry.getKey();

                if(platformEntry.getValue().containsKey(marker)){

                    if(platformEntry.getValue().get(marker).containsKey(variant)){

                        Set<Long> foundModels = platformEntry.getValue().get(marker).get(variant);

                        for(Long modelId : foundModels){

                            if(previouslyFoundModels.containsKey(modelId)){

                                previouslyFoundModels.get(modelId).add(platformName+" "+marker+" "+variant);
                            }
                            else{

                                Set<String>  newSet = new HashSet<>();
                                newSet.add(platformName+" "+marker+" "+variant);
                                previouslyFoundModels.put(modelId, newSet);
                            }

                        }

                    }
                }
            }
        }

    }



    private void getModelsByDrugAndResponse(String drug, String response, Map<Long, Set<String>> previouslyFoundModels,
                                            Map<Long, List<DrugSummaryDTO>> modelsDrugSummary){

        //drug => response => set of model ids

        //Cases
        //1. drug + no response selected
        //2. drug + ALL response
        //3. drug + one response selected
        //4. no drug + one response selected
        //5. no drug + ALL response


        //1. = 2.
        if(drug != null && response.toLowerCase().equals("all")){

            if(modelDrugResponses.containsKey(drug)){

                for(Map.Entry<String, Set<Long>> currResp: modelDrugResponses.get(drug).entrySet()){

                    String resp = currResp.getKey();
                    Set<Long> foundModels = currResp.getValue();

                    for(Long modelId: foundModels){

                        if(previouslyFoundModels.containsKey(modelId)){

                            previouslyFoundModels.get(modelId).add(drug+" "+response);

                            if(modelsDrugSummary.containsKey(modelId)){

                                modelsDrugSummary.get(modelId).add(new DrugSummaryDTO(drug, resp));
                            }
                            else{
                                List dr = new ArrayList();
                                dr.add(new DrugSummaryDTO(drug, resp));
                                modelsDrugSummary.put(modelId, dr);
                            }

                        }
                        else{

                            Set<String>  newSet = new HashSet<>();
                            newSet.add(drug+" "+response);
                            previouslyFoundModels.put(modelId, newSet);

                            if(modelsDrugSummary.containsKey(modelId)){

                                modelsDrugSummary.get(modelId).add(new DrugSummaryDTO(drug, resp));
                            }
                            else{
                                List dr = new ArrayList();
                                dr.add(new DrugSummaryDTO(drug, resp));
                                modelsDrugSummary.put(modelId, dr);
                            }
                        }
                    }
                }
            }
        }
        //3.
        else if(drug != null && response != null){

            if(modelDrugResponses.containsKey(drug)){

                if(modelDrugResponses.get(drug).containsKey(response)){

                    Set<Long> foundModels = modelDrugResponses.get(drug).get(response);

                    for(Long modelId: foundModels){

                        if(previouslyFoundModels.containsKey(modelId)){

                            previouslyFoundModels.get(modelId).add(drug+" "+response);

                            if(modelsDrugSummary.containsKey(modelId)){

                                modelsDrugSummary.get(modelId).add(new DrugSummaryDTO(drug, response));
                            }
                            else{
                                List dr = new ArrayList();
                                dr.add(new DrugSummaryDTO(drug, response));
                                modelsDrugSummary.put(modelId, dr);
                            }
                        }
                        else{

                            Set<String>  newSet = new HashSet<>();
                            newSet.add(drug+" "+response);
                            previouslyFoundModels.put(modelId, newSet);

                            if(modelsDrugSummary.containsKey(modelId)){

                                modelsDrugSummary.get(modelId).add(new DrugSummaryDTO(drug, response));
                            }
                            else{
                                List dr = new ArrayList();
                                dr.add(new DrugSummaryDTO(drug, response));
                                modelsDrugSummary.put(modelId, dr);
                            }
                        }
                    }
                }
            }
        }

        //4. 5.
        else if(drug == null && response != null){

            if(response.equals("ALL")){

                for(Map.Entry<String, Map<String, Set<Long>>> currDrug: modelDrugResponses.entrySet()){

                    String drugName = currDrug.getKey();

                    for(Map.Entry<String, Set<Long>> responses : currDrug.getValue().entrySet()){

                        String currResp = responses.getKey();
                        Set<Long> foundModels = responses.getValue();

                        for(Long modelId: foundModels){

                            if(previouslyFoundModels.containsKey(modelId)){

                                previouslyFoundModels.get(modelId).add(drugName+" "+currResp);

                                if(modelsDrugSummary.containsKey(modelId)){

                                    modelsDrugSummary.get(modelId).add(new DrugSummaryDTO(drugName, currResp));
                                }
                                else{
                                    List dr = new ArrayList();
                                    dr.add(new DrugSummaryDTO(drugName, currResp));
                                    modelsDrugSummary.put(modelId, dr);
                                }
                            }
                            else{

                                Set<String>  newSet = new HashSet<>();
                                newSet.add(drugName+" "+currResp);
                                previouslyFoundModels.put(modelId, newSet);

                                if(modelsDrugSummary.containsKey(modelId)){

                                    modelsDrugSummary.get(modelId).add(new DrugSummaryDTO(drugName, currResp));
                                }
                                else{
                                    List dr = new ArrayList();
                                    dr.add(new DrugSummaryDTO(drugName, currResp));
                                    modelsDrugSummary.put(modelId, dr);
                                }
                            }
                        }

                    }
                }


            }
            else{

                for(Map.Entry<String, Map<String, Set<Long>>> drugs : modelDrugResponses.entrySet()){

                    String drugName = drugs.getKey();

                    if(drugs.getValue().containsKey(response)){

                        Set<Long> foundModels = drugs.getValue().get(response);

                        for(Long modelId: foundModels){

                            if(previouslyFoundModels.containsKey(modelId)){

                                previouslyFoundModels.get(modelId).add(drugName+" "+response);

                                if(modelsDrugSummary.containsKey(modelId)){

                                    modelsDrugSummary.get(modelId).add(new DrugSummaryDTO(drugName, response));
                                }
                                else{
                                    List dr = new ArrayList();
                                    dr.add(new DrugSummaryDTO(drugName, response));
                                    modelsDrugSummary.put(modelId, dr);
                                }
                            }
                            else{

                                Set<String>  newSet = new HashSet<>();
                                newSet.add(drugName+" "+response);
                                previouslyFoundModels.put(modelId, newSet);

                                if(modelsDrugSummary.containsKey(modelId)){

                                    modelsDrugSummary.get(modelId).add(new DrugSummaryDTO(drugName, response));
                                }
                                else{
                                    List dr = new ArrayList();
                                    dr.add(new DrugSummaryDTO(drugName, response));
                                    modelsDrugSummary.put(modelId, dr);
                                }
                            }
                        }
                    }
                }

            }




        }



    }
*/


    /**
     * Search function accespts a Map of key value pairs
     * key = what facet to search
     * list of values = what values to filter on (using OR)
     * <p>
     * EX of expected data structure:
     * <p>
     * patient_age -> { 5-10, 20-40 },
     * patient_gender -> { Male },
     * sample_origin_tissue -> { Lung, Liver }
     * <p>
     * would yield results for male patients between 5-10 OR between 20-40 AND that had cancers in the lung OR liver
     *
     * @param filters
     * @return set of models derived from filtering the complete set according to the
     * filters passed in as arguments
     */

    /*
    public Set<ModelForQuery> search(Map<SearchFacetName, List<String>> filters) {

        synchronized (this){
            if(! INITIALIZED ) {
                init();
            }
        }

        Set<ModelForQuery> result = new HashSet<>(models);

        //empty previously set variants
        result.forEach(x -> x.setMutatedVariants(new ArrayList<>()));

        //empty previously set drugs
        result.forEach(x -> x.setDrugData(new ArrayList<>()));

        // If no filters have been specified, return the complete set
        if (filters == null) {
            return result;
        }

        for (SearchFacetName facet : filters.keySet()) {

            Predicate predicate;

            switch (facet) {

                case query:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.query));

                    Set<ModelForQuery> accumulate = new HashSet<>();
                    for (ModelForQuery r : result) {

                        Set<String> i = r.getAllOntologyTermAncestors().stream().filter(x -> predicate.test(x)).collect(Collectors.toSet());
                        if (i != null && i.size() > 0) {
                            r.setQueryMatch(i);
                            accumulate.add(r);
                        }

                    }

                    result = accumulate;
                    break;

                case datasource:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.datasource));
                    result = result.stream().filter(x -> predicate.test(x.getDatasource())).collect(Collectors.toSet());
                    break;

                case diagnosis:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.diagnosis));
                    result = result.stream().filter(x -> predicate.test(x.getMappedOntologyTerm())).collect(Collectors.toSet());
                    break;

                case patient_age:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.patient_age));
                    result = result.stream().filter(x -> predicate.test(x.getPatientAge())).collect(Collectors.toSet());
                    break;

                case patient_treatment_status:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.patient_treatment_status));
                    result = result.stream().filter(x -> predicate.test(x.getPatientTreatmentStatus())).collect(Collectors.toSet());
                    break;

                case patient_gender:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.patient_gender));
                    result = result.stream().filter(x -> predicate.test(x.getPatientGender())).collect(Collectors.toSet());
                    break;

                case sample_origin_tissue:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.sample_origin_tissue));
                    result = result.stream().filter(x -> predicate.test(x.getSampleOriginTissue())).collect(Collectors.toSet());
                    break;

                case sample_classification:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.sample_classification));
                    result = result.stream().filter(x -> predicate.test(x.getSampleClassification())).collect(Collectors.toSet());
                    break;

                case sample_tumor_type:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.sample_tumor_type));
                    result = result.stream().filter(x -> predicate.test(x.getSampleTumorType())).collect(Collectors.toSet());
                    break;

                case model_implantation_site:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.model_implantation_site));
                    result = result.stream().filter(x -> predicate.test(x.getModelImplantationSite())).collect(Collectors.toSet());
                    break;

                case model_implantation_type:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.model_implantation_type));
                    result = result.stream().filter(x -> predicate.test(x.getModelImplantationType())).collect(Collectors.toSet());
                    break;

                case model_host_strain:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.model_host_strain));
                    result = result.stream().filter(x -> predicate.test(x.getModelHostStrain())).collect(Collectors.toSet());
                    break;

                case cancer_system:

                    Set<ModelForQuery> toRemove = new HashSet<>();
                    for (ModelForQuery res : result) {
                        Boolean keep = Boolean.FALSE;
                        for (String s : filters.get(SearchFacetName.cancer_system)) {
                            if (res.getCancerSystem().contains(s)) {
                                keep = Boolean.TRUE;
                            }
                        }
                        if (!keep) {
                            toRemove.add(res);
                        }
                    }

                    result.removeAll(toRemove);
                    break;

                case organ:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.organ));
                    result = result.stream().filter(x -> predicate.test(x.getCancerOrgan())).collect(Collectors.toSet());
                    break;

                case cell_type:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.cell_type));
                    result = result.stream().filter(x -> predicate.test(x.getCancerCellType())).collect(Collectors.toSet());
                    break;

                case mutation:
                    // mutation=KRAS___MUT___V600E, mutation=NRAS___WT
                    // if the String has ___ (three underscores) twice, it is mutated, if it has only one, it is WT

                    Map<Long, Set<String>> modelsWithMutatedMarkerAndVariant = new HashMap<>();

                    for(String mutation: filters.get(SearchFacetName.mutation)){

                        if(mutation.split("___").length == 3){

                            String[] mut = mutation.split("___");
                            String marker = mut[0];
                            String variant = mut[2];
                            getModelsByMutatedMarkerAndVariant(marker, variant, modelsWithMutatedMarkerAndVariant);

                        }
                        else if(mutation.split("___").length == 2){

                            //TODO: add wt lookup when we have data

                        }
                    }
                    // applies the mutation filters
                    result = result.stream().filter(x -> modelsWithMutatedMarkerAndVariant.containsKey(x.getModelId())).collect(Collectors.toSet());
                    // updates the remaining modelforquery objects with platform+marker+variant info
                    result.forEach(x -> x.setMutatedVariants(new ArrayList<>(modelsWithMutatedMarkerAndVariant.get(x.getModelId()))));
                    break;

                case drug:
                    Map<Long, Set<String>> modelsWithDrug = new HashMap<>();
                    Map<Long, List<DrugSummaryDTO>> modelsDrugSummary = new HashMap<>();

                    for(String filt : filters.get(SearchFacetName.drug)){

                        String[] drugAndResponse = filt.split("___");
                        String drug = drugAndResponse[0];
                        String response = drugAndResponse[1];

                        if(drug.isEmpty()) drug = null;
                        getModelsByDrugAndResponse(drug,response, modelsWithDrug, modelsDrugSummary);
                    }

                    result = result.stream().filter(x -> modelsWithDrug.containsKey(x.getModelId())).collect(Collectors.toSet());
                    // updates the remaining modelforquery objects with drug and response info

                    //result.forEach(x -> x.setMutatedVariants(new ArrayList<>(modelsWithDrug.get(x.getModelId()))));
                    result.forEach(x -> x.setDrugData(modelsDrugSummary.get(x.getModelId())));
                    break;

                case project:
                    Set<ModelForQuery> projectsToRemove = new HashSet<>();
                    for (ModelForQuery res : result) {
                        Boolean keep = Boolean.FALSE;
                        for (String s : filters.get(SearchFacetName.project)) {
                            try{
                                if (res.getProjects() != null && res.getProjects().contains(s)) {
                                    keep = Boolean.TRUE;
                                }
                            } catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                        if (!keep) {
                            projectsToRemove.add(res);
                        }
                    }

                    result.removeAll(projectsToRemove);
                    break;
                case data_available:
                    Set<ModelForQuery> mfqToRemove = new HashSet<>();
                    for (ModelForQuery res : result) {
                        Boolean keep = Boolean.FALSE;
                        for (String s : filters.get(SearchFacetName.data_available)) {
                            try{
                                if (res.getDataAvailable() != null && res.getDataAvailable().contains(s)) {
                                    keep = Boolean.TRUE;
                                }
                            } catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                        if (!keep) {
                            mfqToRemove.add(res);
                        }
                    }

                    result.removeAll(mfqToRemove);
                    break;

                default:
                    // default case is an unexpected filter option
                    // Do not filter anything
                    log.info("Unrecognised facet {} passed to search, skipping.", facet);
                    break;
            }
        }

        return result;
    }

    */

    /**
     * getExactMatchDisjunctionPredicate returns a composed predicate with all the supplied filters "OR"ed together
     * using an exact match
     * <p>
     * NOTE: This is a case sensitive match!
     *
     * @param filters the set of strings to match against
     * @return a composed predicate case insensitive matching the supplied filters using disjunction (OR)
     */
    Predicate<String> getExactMatchDisjunctionPredicate(List<String> filters) {
        List<Predicate<String>> preds = new ArrayList<>();

        // Iterate through the filter options passed in for this facet
        for (String filter : filters) {

            // Create a filter predicate for each option
            Predicate<String> pred = s -> s.equals(filter);

            // Store all filter options in a list
            preds.add(pred);
        }

        // Create a "combination" predicate containing sub-predicates "OR"ed together
        return preds.stream().reduce(Predicate::or).orElse(x -> false);
    }

    /**
     * getContainsMatchDisjunctionPredicate returns a composed predicate with all the supplied filters "OR"ed together
     * using a contains match
     * <p>
     * NOTE: This is a case insensitive match!
     *
     * @param filters the set of strings to match against
     * @return a composed predicate case insensitive matching the supplied filters using disjunction (OR)
     */
    Predicate<String> getContainsMatchDisjunctionPredicate(List<String> filters) {
        List<Predicate<String>> preds = new ArrayList<>();

        // Iterate through the filter options passed in for this facet
        for (String filter : filters) {

            // Create a filter predicate for each option
            Predicate<String> pred = s -> s.toLowerCase().contains(filter.toLowerCase());

            // Store all filter options in a list
            preds.add(pred);
        }

        // Create a "combination" predicate containing sub-predicates "OR"ed together
        return preds.stream().reduce(Predicate::or).orElse(x -> false);
    }


    public List<FacetOption> getFacetOptions(SearchFacetName facet,List<String> options, Map<SearchFacetName, List<String>> configuredFacets){

        List<FacetOption> facetOptions = new ArrayList<>();

        for(String s : options){

            FacetOption fo = new FacetOption(s, 0);
            fo.setSelected(false);
            facetOptions.add(fo);
        }

        if(configuredFacets.containsKey(facet)){

            List<String> selectedFacets = configuredFacets.get(facet);
            for(String sf : selectedFacets){

                for(FacetOption fo : facetOptions){

                    if(fo.getName().equals(sf)){
                        fo.setSelected(true);
                    }
                }


            }

        }

        return facetOptions;
    }


    /**
     * Get the count of models for a supplied facet.
     * <p>
     * This method will return counts of facet options for the supplied facet
     *
     * @param facet    the facet to count
     * @param results  set of models already filtered
     * @param selected what facets have been filtered already
     * @return a list of {@link FacetOption} indicating counts and selected state
     */
    @Cacheable("facet_counts")
    public List<FacetOption> getFacetOptions(SearchFacetName facet, List<String> options, Set<ModelForQuery> results, List<String> selected) {


        Set<ModelForQuery> allResults = models;

        List<FacetOption> map = new ArrayList<>();

        // Initialise all facet option counts to 0 and set selected attribute on all options that the user has chosen
        if (options != null) {
            for (String option : options) {
                Long count = allResults
                        .stream()
                        .filter(x ->
                                Stream.of(x.getBy(facet).split("::"))
                                        .collect(Collectors.toSet())
                                        .contains(option))
                        .count();
                map.add(new FacetOption(option, selected != null ? 0 : count.intValue(), count.intValue(), selected != null && selected.contains(option) ? Boolean.TRUE : Boolean.FALSE, facet));
            }
        }

        // We want the counts on the facets to look something like:
        // Gender
        //   [ ] Male (1005 of 1005)
        //   [ ] Female (840 of 840)
        //   [ ] Not specified (31 of 31)
        // Then when a facet is clicked:
        // Gender
        //   [X] Male (1005 of (1005)
        //   [ ] Female (0 of 840)
        //   [ ] Not specified (2 of 31)
        //


        // Iterate through results adding count to the appropriate option
        for (ModelForQuery mfq : results) {

            String s = mfq.getBy(facet);

            // Skip empty facets
            if (s == null || s.equals("")) {
                continue;
            }

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
                    map.add(new FacetOption(s, 0, 0, selected != null && selected.contains(s) ? Boolean.TRUE : Boolean.FALSE, facet));
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


    /**
     * Get the count of models for each diagnosis (including children).
     * <p>
     * This method will return counts of facet options for the supplied facet
     *
     * @return a Map of k: diagnosis v: count
     */
    @Cacheable("diagnosis_counts")
    public Map<String, Integer> getDiagnosisCounts() {

        Set<ModelForQuery> allResults = models;

        Map<String, Integer> map = new HashMap<>();


        // Get the list of diagnoses
        Set<String> allDiagnoses = allResults.stream().map(ModelForQuery::getMappedOntologyTerm).collect(Collectors.toSet());

        //  For each diagnosis, match all results using the same search technique as "query"
        for (String diagnosis : allDiagnoses) {
            Predicate<String> predicate = getContainsMatchDisjunctionPredicate(Arrays.asList(diagnosis));
//            Long i = allResults.stream().map(x -> x.getAllOntologyTermAncestors().stream().filter(predicate).collect(Collectors.toSet())).map(x->((Set)x)).filter(x->x.size()>0).distinct().count();
            Long i = allResults.stream()
                    .filter(x -> x.getAllOntologyTermAncestors().stream().filter(predicate).collect(Collectors.toSet()).size() > 0)
                    .distinct().count();
//            Long i = allResults.stream().filter(x -> x.getAllOntologyTermAncestors().contains(diagnosis)).distinct().count();
            map.put(diagnosis, i.intValue());
        }

        return map;
    }


}