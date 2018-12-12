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

    //facet key => option
    private Map<String, List<FacetOption>> facetOptionMap;

    // SEARCH OBJECTS:


    /**
     * A general one param search object that is being used when search is performed on a MFQ object field
     */
    private OneParamSearch oneParamSearch;

    /**
     * Four param search object for performing a search on gene mutations
     */
    private ThreeParamLinkedSearch geneMutationSearch;

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
        facetOptionMap = new HashMap<>();

        WebFacetSection patientTumorSection = new WebFacetSection();
        patientTumorSection.setName("PATIENT / TUMOR");

        WebFacetSection pdxModelSection = new WebFacetSection();
        pdxModelSection.setName("PDX MODEL");

        WebFacetSection molecularDataSection = new WebFacetSection();
        molecularDataSection.setName("MOLECULAR DATA");

        WebFacetSection treatmentInfoSection = new WebFacetSection();
        treatmentInfoSection.setName("TREATMENT INFORMATION");

        //cancer by system filter def
        List<FacetOption> cancerBySystemOptions = new ArrayList<>();
        cancerBySystemOptions.add(new FacetOption("Breast Cancer", "Breast_Cancer"));
        cancerBySystemOptions.add(new FacetOption("Cardiovascular Cancer", "Cardiovascular_Cancer"));
        cancerBySystemOptions.add(new FacetOption("Connective and Soft Tissue Cancer", "Connective_and_Soft_Tissue_Cancer"));
        cancerBySystemOptions.add(new FacetOption("Digestive System Cancer", "Digestive_System_Cancer"));
        cancerBySystemOptions.add(new FacetOption("Endocrine Cancer", "Endocrine_Cancer"));
        cancerBySystemOptions.add(new FacetOption("Eye Cancer", "Eye_Cancer"));
        cancerBySystemOptions.add(new FacetOption("Head and Neck Cancer", "Head_and_Neck_Cancer"));
        cancerBySystemOptions.add(new FacetOption("Hematopoietic and Lymphoid System Cancer", "Hematopoietic_and_Lymphoid_System_Cancer"));
        cancerBySystemOptions.add(new FacetOption("Nervous System Cancer", "Nervous_System_Cancer"));
        cancerBySystemOptions.add(new FacetOption("Peritoneal and Retroperitoneal Cancer", "Peritoneal_and_Retroperitoneal_Cancer"));
        cancerBySystemOptions.add(new FacetOption("Reproductive System Cancer", "Reproductive_System_Cancer"));
        cancerBySystemOptions.add(new FacetOption("Respiratory Tract Cancer", "Respiratory_Tract_Cancer"));
        cancerBySystemOptions.add(new FacetOption("Thoracic Cancer", "Thoracic_Cancer"));
        cancerBySystemOptions.add(new FacetOption("Skin Cancer", "Skin_Cancer"));
        cancerBySystemOptions.add(new FacetOption("Urinary System Cancer", "Urinary_System_Cancer"));
        cancerBySystemOptions.add(new FacetOption("Unclassified", "Unclassified"));

        OneParamFilter cancerBySystem = new OneParamFilter("CANCER BY SYSTEM", "cancer_system", false,
                cancerBySystemOptions, new ArrayList<>());
        patientTumorSection.addComponent(cancerBySystem);
        facetOptionMap.put("cancer_system", cancerBySystemOptions);

        //tumor type filter def
        List<FacetOption> tumorTypeOptions = new ArrayList<>();
        tumorTypeOptions.add(new FacetOption("Primary", "Primary"));
        tumorTypeOptions.add(new FacetOption("Metastatic", "Metastatic"));
        tumorTypeOptions.add(new FacetOption("Recurrent", "Recurrent"));
        tumorTypeOptions.add(new FacetOption("Refractory", "Refractory"));
        tumorTypeOptions.add(new FacetOption("Not Specified", "Not_Specified"));

        OneParamFilter tumorType = new OneParamFilter("TUMOR_TYPE", "sample_tumor_type", false,
              tumorTypeOptions, new ArrayList<>());
        patientTumorSection.addComponent(tumorType);
        facetOptionMap.put("sample_tumor_type", tumorTypeOptions);

        //sex filter def
        List<FacetOption> patientSexOptions = new ArrayList<>();
        patientSexOptions.add(new FacetOption("Male", "Male"));
        patientSexOptions.add(new FacetOption("Female", "Female"));
        patientSexOptions.add(new FacetOption("Not Specified", "Not_Specified"));
        OneParamFilter sex = new OneParamFilter("SEX", "patient_gender", false,
        patientSexOptions, new ArrayList<>());
        patientTumorSection.addComponent(sex);
        facetOptionMap.put("patient_gender", patientSexOptions);

        //age filter def
        List<FacetOption> ageOptions = new ArrayList<>();
        ageOptions.add(new FacetOption("0-9", "0-9"));
        ageOptions.add(new FacetOption("10-19", "10-19"));
        ageOptions.add(new FacetOption("20-29", "20-29"));
        ageOptions.add(new FacetOption("30-39", "30-39"));
        ageOptions.add(new FacetOption("40-49", "40-49"));
        ageOptions.add(new FacetOption("50-59", "50-59"));
        ageOptions.add(new FacetOption("60-69", "60-69"));
        ageOptions.add(new FacetOption("70-79", "70-79"));
        ageOptions.add(new FacetOption("80-89", "80-89"));
        ageOptions.add(new FacetOption("90", "90"));
        ageOptions.add(new FacetOption("Not Specified", "Not_Specified"));

        OneParamFilter age = new OneParamFilter("AGE", "patient_age", false,
        ageOptions, new ArrayList<>());
        patientTumorSection.addComponent(age);
        facetOptionMap.put("patient_age",ageOptions);

        //datasource filter def
        Set<String> datasourceSet = models.stream()
                .map(ModelForQuery::getDatasource)
                .collect(Collectors.toSet());

        List<String> datasourceList = new ArrayList<>();
        datasourceList.addAll(datasourceSet);
        Collections.sort(datasourceList);

        List<FacetOption> datasourceOptions = new ArrayList<>();
        for(String ds : datasourceList){
            datasourceOptions.add(new FacetOption(ds, ds));
        }

        OneParamFilter datasource = new OneParamFilter("DATASOURCE", "datasource", false, datasourceOptions, new ArrayList<>());
        pdxModelSection.addComponent(datasource);
        facetOptionMap.put("datasource", datasourceOptions);

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

        List<FacetOption> projectOptions = new ArrayList<>();

        for(String p: projectList){
            projectOptions.add(new FacetOption(p, p));
        }
        OneParamFilter projects = new OneParamFilter("PROJECT", "project", false, projectOptions, new ArrayList<>());
        pdxModelSection.addComponent(projects);
        facetOptionMap.put("project", projectOptions);

        //dataset available filter def
        List<FacetOption> datasetAvailableOptions = new ArrayList<>();
        datasetAvailableOptions.add(new FacetOption("Gene Mutation", "Gene_Mutation"));
        datasetAvailableOptions.add(new FacetOption("Dosing Studies", "Dosing_Studies"));
        datasetAvailableOptions.add(new FacetOption("Patient Treatment", "Patient_Treatment"));

        OneParamFilter datasetAvailable = new OneParamFilter("DATASET AVAILABLE", "data_available", false,
        datasetAvailableOptions, new ArrayList<>());

        pdxModelSection.addComponent(datasetAvailable);
        facetOptionMap.put("data_available", datasetAvailableOptions);

        //gene mutation filter def
        //TODO: look up platforms, genes and variants
        ThreeParamLinkedFilter geneMutation = new ThreeParamLinkedFilter("GENE MUTATION", "mutation", false,
                "GENE", "TYPE", "VARIANT", new HashMap<>(), new HashMap<>());

        molecularDataSection.addComponent(geneMutation);


        //Breast cancer markers
        List<FacetOption> breastCancerMarkerOptions = new ArrayList<>();
        breastCancerMarkerOptions.add(new FacetOption("HER+ PR+ ER+", "HERpos_PRneg_ERneg"));
        breastCancerMarkerOptions.add(new FacetOption("HER+ PR+ ER-", "HERpos_PRpos_ERneg"));
        breastCancerMarkerOptions.add(new FacetOption("HER+ PR- ER+", "HERpos_PRneg_ERpos"));
        breastCancerMarkerOptions.add(new FacetOption("HER+ PR- ER-", "HERpos_PRneg_ERneg"));
        breastCancerMarkerOptions.add(new FacetOption("HER- PR+ ER+", "HERneg_PRpos_ERpos"));
        breastCancerMarkerOptions.add(new FacetOption("HER- PR+ ER-", "HERneg_PRpos_ERneg"));
        breastCancerMarkerOptions.add(new FacetOption("HER- PR- ER+", "HERneg_PRneg_ERpos"));
        breastCancerMarkerOptions.add(new FacetOption("HER- PR- ER-", "HERneg_PRneg_ERneg"));

        OneParamFilter breastCancerMarkers = new OneParamFilter("BREAST CANCER BIOMARKERS", "breast_cancer_marker", false,
                breastCancerMarkerOptions, new ArrayList<>());
        molecularDataSection.addComponent(breastCancerMarkers);
        facetOptionMap.put("breast_cancer_marker",breastCancerMarkerOptions);


        //model dosing study def

        Map<String, Map<String, Set<Long>>> modelDrugResponses = getModelDrugResponsesFromDP();
        List<String> drugNames = new ArrayList<>(modelDrugResponses.keySet());

        TwoParamUnlinkedFilter modelDosingStudy = new TwoParamUnlinkedFilter("MODEL DOSING STUDY", "drug", false, "DRUG", "RESPONSE", drugNames, Arrays.asList(
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
        //the gene mutation is a ThreeParamFilter component, but a FourParamLinkedSearch must be used because of the hidden platform labelId
        geneMutationSearch = new ThreeParamLinkedSearch("geneMutation", "mutation");

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

            List<String> decodedSelected = new ArrayList<>();
            //if there is an overwrite rule for the filter, replace the selected values with the replacement
            if(facetOptionMap.get(facetName) != null){

                for(FacetOption fo: facetOptionMap.get(facetName)){

                    if(selected.contains(fo.getLabelId()))
                    decodedSelected.add(fo.getLabelId());
                }
            }
            //no overwrite rule
            else{
                decodedSelected = selected;
            }


            for(WebFacetSection wfs :webFacetContainerClone.getWebFacetSections()){
                for(GeneralFilter filter: wfs.getFilterComponents()){


                    if(filter.getUrlParam().equals(facetName)){
                        filter.setActive(true);

                        if(filter instanceof OneParamFilter){

                            OneParamFilter f = (OneParamFilter)filter;
                            f.setSelected(decodedSelected);

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
                    result = oneParamSearch.searchOnCollection(facetOptionMap.get("query"), filters.get(SearchFacetName.query), result, ModelForQuery::getAllOntologyTermAncestors);
                    break;

                case datasource:
                    result = oneParamSearch.searchOnString(facetOptionMap.get("datasource"), filters.get(SearchFacetName.datasource), result, ModelForQuery::getDatasource);
                    break;

                case diagnosis:
                    result = oneParamSearch.searchOnString(facetOptionMap.get("diagnosis"), filters.get(SearchFacetName.diagnosis), result, ModelForQuery::getMappedOntologyTerm);
                    break;

                case patient_age:
                    result = oneParamSearch.searchOnString(facetOptionMap.get("patient_age"), filters.get(SearchFacetName.patient_age), result, ModelForQuery::getPatientAge);
                    break;
                case patient_treatment_status:
                    result = oneParamSearch.searchOnString(facetOptionMap.get("patient_treatment_status"), filters.get(SearchFacetName.patient_treatment_status), result, ModelForQuery::getPatientTreatmentStatus);
                    break;

                case patient_gender:
                    result = oneParamSearch.searchOnString(facetOptionMap.get("patient_gender"), filters.get(SearchFacetName.patient_gender), result, ModelForQuery::getPatientGender);
                    break;

                case sample_origin_tissue:
                    result = oneParamSearch.searchOnString(facetOptionMap.get("sample_origin_tissue"), filters.get(SearchFacetName.sample_origin_tissue), result, ModelForQuery::getSampleOriginTissue);
                    break;

                case sample_classification:
                    result = oneParamSearch.searchOnString(facetOptionMap.get("sample_classification"), filters.get(SearchFacetName.sample_classification), result, ModelForQuery::getSampleClassification);
                    break;

                case sample_tumor_type:
                    result = oneParamSearch.searchOnString(facetOptionMap.get("sample_tumor_type"), filters.get(SearchFacetName.sample_tumor_type), result, ModelForQuery::getSampleTumorType);
                    break;

                case model_implantation_site:
                    result = oneParamSearch.searchOnString(facetOptionMap.get("model_implantation_site"), filters.get(SearchFacetName.model_implantation_site), result, ModelForQuery::getModelImplantationSite);
                    break;

                case model_implantation_type:
                    result = oneParamSearch.searchOnString(facetOptionMap.get("model_implantation_type"), filters.get(SearchFacetName.model_implantation_type), result, ModelForQuery::getModelImplantationSite);
                    break;

                case model_host_strain:
                    result = oneParamSearch.searchOnCollection(facetOptionMap.get("model_host_strain"), filters.get(SearchFacetName.model_host_strain), result, ModelForQuery::getModelHostStrain);
                    break;

                case cancer_system:
                    result = oneParamSearch.searchOnCollection(facetOptionMap.get("cancer_system"), filters.get(SearchFacetName.cancer_system), result, ModelForQuery::getCancerSystem);
                    break;

                case organ:
                    result = oneParamSearch.searchOnString(facetOptionMap.get("organ"), filters.get(SearchFacetName.organ), result, ModelForQuery::getCancerOrgan);
                    break;

                case cell_type:
                    result = oneParamSearch.searchOnString(facetOptionMap.get("cell_type"), filters.get(SearchFacetName.cell_type), result, ModelForQuery::getCancerCellType);
                    break;

                case project:
                    result = oneParamSearch.searchOnCollection(facetOptionMap.get("project"), filters.get(SearchFacetName.project), result, ModelForQuery::getProjects);
                    break;

                case data_available:
                    result = oneParamSearch.searchOnCollection(facetOptionMap.get("data_available"), filters.get(SearchFacetName.data_available), result, ModelForQuery::getDataAvailable);
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

/*
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

*/


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