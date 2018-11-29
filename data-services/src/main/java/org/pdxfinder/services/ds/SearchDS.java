package org.pdxfinder.services.ds;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.dao.DataProjection;
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
     * Four param search object for performing a search on gene mutations
     */
    private FourParamLinkedSearch geneMutationSearch;

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
        ThreeParamLinkedFilter geneMutation = new ThreeParamLinkedFilter("GENE MUTATION", "mutation", new HashMap<>(), new HashMap<>());

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
        //the gene mutation is a ThreeParamFilter component, but a FourParamLinkedSearch must be used because of the hidden platform value
        geneMutationSearch = new FourParamLinkedSearch("geneMutation", "mutation");

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


    public WebFacetContainer getUpdatedSelectedFilters(Map<SearchFacetName, List<String>> filters){


        //use a clone to avoid keeping filters from previous iterations
        WebFacetContainer webFacetContainerClone = new WebFacetContainer();
        List<WebFacetSection> sections = new ArrayList<>(webFacetContainer.getWebFacetSections());
        webFacetContainerClone.setWebFacetSections(sections);


        for(Map.Entry<SearchFacetName, List<String>> facet: filters.entrySet()){

            String facetName = facet.getKey().getName();
            List<String> selected = facet.getValue();


            for(WebFacetSection wfs :webFacetContainerClone.getWebFacetSections()){
                for(GeneralFilter filter: wfs.getFilterComponents()){


                    if(filter.getUrlParam().equals(facetName)){

                        if(filter instanceof OneParamFilter){

                            OneParamFilter f = (OneParamFilter)filter;
                            f.setSelected(selected);

                        }
                        else if(filter instanceof TwoParamUnlinkedFilter){

                            TwoParamUnlinkedFilter f = (TwoParamUnlinkedFilter) filter;
                            //TODO: Implement updating two and three parameter filters

                        }

                    }

                }
            }


        }

        return webFacetContainerClone;
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
        result.forEach(x -> x.setDrugWithResponse(new ArrayList<>()));

        // If no filters have been specified, return the complete set
        if (filters == null) {
            return result;
        }

        OneParamSearch oneParamSearch = new OneParamSearch("search","search");

        for (SearchFacetName facet : filters.keySet()) {

            log.info("Models:"+result.size()+" before applying filter: "+facet.getName());

            switch(facet){

                case query:
                    //List<String> searchParams, Set<ModelForQuery> mfqSet, Function<ModelForQuery, List<String>> searchFunc
                    result = oneParamSearch.searchOnCollection(filters.get(SearchFacetName.query), result, ModelForQuery::getAllOntologyTermAncestors);
                    break;

                case datasource:
                    result = oneParamSearch.searchOnString(filters.get(SearchFacetName.datasource), result, ModelForQuery::getDatasource);
                    break;

                case diagnosis:
                    result = oneParamSearch.searchOnString(filters.get(SearchFacetName.diagnosis), result, ModelForQuery::getMappedOntologyTerm);
                    break;

                case patient_age:
                    result = oneParamSearch.searchOnString(filters.get(SearchFacetName.patient_age), result, ModelForQuery::getPatientAge);
                    break;
                case patient_treatment_status:
                    result = oneParamSearch.searchOnString(filters.get(SearchFacetName.patient_treatment_status), result, ModelForQuery::getPatientTreatmentStatus);
                    break;

                case patient_gender:
                    result = oneParamSearch.searchOnString(filters.get(SearchFacetName.patient_gender), result, ModelForQuery::getPatientGender);
                    break;

                case sample_origin_tissue:
                    result = oneParamSearch.searchOnString(filters.get(SearchFacetName.sample_origin_tissue), result, ModelForQuery::getSampleOriginTissue);
                    break;

                case sample_classification:
                    result = oneParamSearch.searchOnString(filters.get(SearchFacetName.sample_classification), result, ModelForQuery::getSampleClassification);
                    break;

                case sample_tumor_type:
                    result = oneParamSearch.searchOnString(filters.get(SearchFacetName.sample_tumor_type), result, ModelForQuery::getSampleTumorType);
                    break;

                case model_implantation_site:
                    result = oneParamSearch.searchOnString(filters.get(SearchFacetName.model_implantation_site), result, ModelForQuery::getModelImplantationSite);
                    break;

                case model_implantation_type:
                    result = oneParamSearch.searchOnString(filters.get(SearchFacetName.model_implantation_site), result, ModelForQuery::getModelImplantationSite);
                    break;

                case model_host_strain:
                    result = oneParamSearch.searchOnCollection(filters.get(SearchFacetName.model_host_strain), result, ModelForQuery::getModelHostStrain);
                    break;

                case cancer_system:
                    result = oneParamSearch.searchOnCollection(filters.get(SearchFacetName.cancer_system), result, ModelForQuery::getCancerSystem);
                    break;

                case organ:
                    result = oneParamSearch.searchOnString(filters.get(SearchFacetName.organ), result, ModelForQuery::getCancerOrgan);
                    break;

                case cell_type:
                    result = oneParamSearch.searchOnString(filters.get(SearchFacetName.cell_type), result, ModelForQuery::getCancerCellType);
                    break;

                case project:
                    result = oneParamSearch.searchOnCollection(filters.get(SearchFacetName.project), result, ModelForQuery::getProjects);
                    break;

                case data_available:
                    result = oneParamSearch.searchOnCollection(filters.get(SearchFacetName.data_available), result, ModelForQuery::getDataAvailable);
                    break;

                case mutation:
                    result = geneMutationSearch.search(filters.get(SearchFacetName.mutation), result, ModelForQuery::addMutatedVariant);
                    break;

                case drug:
                    result = dosingStudySearch.search(filters.get(SearchFacetName.drug), result, ModelForQuery::addDrugWithResponse);




                default:
                    //undexpected filter option
                    log.warn("Unrecognised facet {} passed to search, skipping", facet.getName());
                    break;


            }

            log.info("After applying filter: "+result.size());

        }



        return result;
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
    private void initializeModels() {


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


    private Map<String, Map<String, Map<String, Map<String, Set<Long>>>>> getMutationsFromDP(){

        log.info("Initializing mutations");
        //platform=> marker=> variant=>{set of model ids}
        Map<String, Map<String, Map<String, Map<String, Set<Long>>>>> mutations = new HashMap<>();

        String mut = dataProjectionRepository.findByLabel("PlatformMarkerVariantModel").getValue();

        try{

            ObjectMapper mapper = new ObjectMapper();

            //mutations = mapper.readValue(mut, new TypeReference<Map<String, Map<String, Map<String, Map<String, Set<Long>>>>>>(){});

            //log.info("Lookup: "+mutations.get("TargetedNGS_MUT").get("RB1").get("N123D").toString());

        }
        catch(Exception e){

            e.printStackTrace();
        }

        return mutations;
    }


    private Map<String, Map<String, Set<Long>>> getModelDrugResponsesFromDP(){

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