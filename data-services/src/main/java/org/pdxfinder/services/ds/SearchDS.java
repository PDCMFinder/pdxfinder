package org.pdxfinder.services.ds;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.graph.dao.DataProjection;
import org.pdxfinder.graph.repositories.DataProjectionRepository;
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
    private OneParamCheckboxSearch oneParamCheckboxSearch;

    /**
     * A general one param search object that is being used when search is performed on a MFQ object field
     */
    private OneParamTextSearch copyNumberAlterationSearch;

    /**
     * Three param search object for performing a search on gene mutations
     */
    private ThreeParamLinkedSearch geneMutationSearch;

    /**
     * Two param search object for performing a search on dosing studies
     */
    private TwoParamUnlinkedSearch dosingStudySearch;


    /**
     * Two param linked search to perform search on breastCancerBioMarkers
     */
    private TwoParamLinkedSearch breastCancerMarkersSearch;


    public SearchDS(DataProjectionRepository dataProjectionRepository) {
        Assert.notNull(dataProjectionRepository, "Data projection repository cannot be null");

        this.dataProjectionRepository = dataProjectionRepository;
        this.models = new HashSet<>();
    }

    /**
     * Initializes the searchDS, creates filter structure and links search objects to them
     */
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

        OneParamCheckboxFilter cancerBySystem = new OneParamCheckboxFilter("CANCER BY SYSTEM", "cancer_system", false, FilterType.OneParamCheckboxFilter.get(),
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

        OneParamCheckboxFilter tumorType = new OneParamCheckboxFilter("TUMOR_TYPE", "sample_tumor_type", false, FilterType.OneParamCheckboxFilter.get(),
              tumorTypeOptions, new ArrayList<>());
        patientTumorSection.addComponent(tumorType);
        facetOptionMap.put("sample_tumor_type", tumorTypeOptions);

        //sex filter def
        List<FacetOption> patientSexOptions = new ArrayList<>();
        patientSexOptions.add(new FacetOption("Male", "Male"));
        patientSexOptions.add(new FacetOption("Female", "Female"));
        patientSexOptions.add(new FacetOption("Not Specified", "Not_Specified"));
        OneParamCheckboxFilter sex = new OneParamCheckboxFilter("SEX", "patient_gender", false, FilterType.OneParamCheckboxFilter.get(),
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

        OneParamCheckboxFilter age = new OneParamCheckboxFilter("AGE", "patient_age", false, FilterType.OneParamCheckboxFilter.get(),
        ageOptions, new ArrayList<>());
        patientTumorSection.addComponent(age);
        facetOptionMap.put("patient_age",ageOptions);

        //treatment status filter
        List<FacetOption> patientTreatmentStatusOptions = new ArrayList<>();
        patientTreatmentStatusOptions.add(new FacetOption("Treatment Naive", "treatment_naive"));
        patientTreatmentStatusOptions.add(new FacetOption("Not Treatment Naive", "not_treatment_naive"));
        patientTreatmentStatusOptions.add(new FacetOption("Not Specified", "Not_Specified"));

        OneParamCheckboxFilter patientTreatmentStatus = new OneParamCheckboxFilter("TREATMENT STATUS", "patient_treatment_status", false,
                FilterType.OneParamCheckboxFilter.get(), patientTreatmentStatusOptions, new ArrayList<>());
        patientTumorSection.addComponent(patientTreatmentStatus);
        facetOptionMap.put("patient_treatment_status", patientTreatmentStatusOptions);

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

        //dataset available filter def
        List<FacetOption> datasetAvailableOptions = new ArrayList<>();
        datasetAvailableOptions.add(new FacetOption("Gene Mutation", "Gene_Mutation"));
        datasetAvailableOptions.add(new FacetOption("Cytogenetics", "Cytogenetics"));
        datasetAvailableOptions.add(new FacetOption("Copy Number Alteration", "Copy_Number_Alteration"));
        datasetAvailableOptions.add(new FacetOption("Dosing Studies", "Dosing_Studies"));
        datasetAvailableOptions.add(new FacetOption("Patient Treatment", "Patient_Treatment"));

        OneParamCheckboxFilter datasetAvailable = new OneParamCheckboxFilter("DATASET AVAILABLE", "data_available", false, FilterType.OneParamCheckboxFilter.get(),
                datasetAvailableOptions, new ArrayList<>());

        pdxModelSection.addComponent(datasetAvailable);
        facetOptionMap.put("data_available", datasetAvailableOptions);

        OneParamCheckboxFilter datasource = new OneParamCheckboxFilter("DATASOURCE", "datasource", false, FilterType.OneParamCheckboxFilter.get(), datasourceOptions, new ArrayList<>());
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
        OneParamCheckboxFilter projects = new OneParamCheckboxFilter("PROJECT", "project", false,
                FilterType.OneParamCheckboxFilter.get(), projectOptions, new ArrayList<>());
        pdxModelSection.addComponent(projects);
        facetOptionMap.put("project", projectOptions);

        //gene mutation filter def
        //TODO: look up platforms, genes and variants
        TwoParamLinkedFilter geneMutation = new TwoParamLinkedFilter("GENE MUTATION", "mutation", false, FilterType.TwoParamLinkedFilter.get(),
                 "GENE", "VARIANT",getMutationOptions(), getMutationAndVariantOptions(), new HashMap<>());

        molecularDataSection.addComponent(geneMutation);

        OneParamTextFilter copyNumberAlteration= new OneParamTextFilter("COPY NUMBER ALTERATION", "copy_number_alteration",
                false, FilterType.OneParamTextFilter.get(), "GENE", getCopyNumberAlterationOptions(), new ArrayList<>());


        molecularDataSection.addComponent(copyNumberAlteration);


        //Breast cancer markers
        //labelIDs should be alphabetically ordered(ER, HER, PR) as per dataprojection requirement
        List<FacetOption> breastCancerMarkerOptions = new ArrayList<>();
                                                                                    //DP> ERBB2(HER2)--ESR1(ER)--PGR(PR)
        //breastCancerMarkerOptions.add(new FacetOption("HER2- ER+ PR+", "ERBB2neg_ESR1neg_PGRpos"));
        //breastCancerMarkerOptions.add(new FacetOption("HER2- ER- PR-", "ERBB2neg_ESR1neg_PGRneg"));
        //breastCancerMarkerOptions.add(new FacetOption("HER2- ER+ PR-", "ERBB2neg_ESR1pos_PGRneg"));
        //breastCancerMarkerOptions.add(new FacetOption("HER2+ ER+ PR+", "ERBB2pos_ESR1pos_PGRpos"));
        //breastCancerMarkerOptions.add(new FacetOption("HER2+ ER- PR-", "ERBB2pos_ESR1neg_PGRneg"));

        //breastCancerMarkerOptions.add(new FacetOption("HER2+ ER- PR+", "ERneg_HER2pos_PRpos"));
        //breastCancerMarkerOptions.add(new FacetOption("HER2+ ER+ PR-", "ERpos_HER2pos_PRneg"));
        //breastCancerMarkerOptions.add(new FacetOption("HER2- ER- PR+", "ERneg_HER2neg_PRpos"));
        breastCancerMarkerOptions.add(new FacetOption("HER2/ERBB2 negative", "ERBB2neg"));
        breastCancerMarkerOptions.add(new FacetOption("HER2/ERBB2 positive", "ERBB2pos"));
        breastCancerMarkerOptions.add(new FacetOption("ER/ESR1 negative", "ESR1neg"));
        breastCancerMarkerOptions.add(new FacetOption("ER/ESR1 positive", "ESR1pos"));
        breastCancerMarkerOptions.add(new FacetOption("PR/PGR negative", "PGRneg"));
        breastCancerMarkerOptions.add(new FacetOption("PR/PGR positive", "PGRpos"));

        OneParamCheckboxFilter breastCancerMarkers = new OneParamCheckboxFilter("BREAST CANCER BIOMARKERS", "breast_cancer_markers", false, FilterType.OneParamCheckboxFilter.get(),
                breastCancerMarkerOptions, new ArrayList<>());
        molecularDataSection.addComponent(breastCancerMarkers);
        facetOptionMap.put("breast_cancer_markers",breastCancerMarkerOptions);


        //model dosing study def

        Map<String, Map<String, Set<Long>>> modelDrugResponses = getModelDrugResponsesFromDP();
        List<String> drugNames = new ArrayList<>(modelDrugResponses.keySet());

        TwoParamUnlinkedFilter modelDosingStudy = new TwoParamUnlinkedFilter("MODEL DOSING STUDY", "drug", false, FilterType.TwoParamUnlinkedFilter.get(), "DRUG", "RESPONSE", drugNames, Arrays.asList(
                "Complete Response",
                "Partial Response",
                "Progressive Disease",
                "Stable Disease",
                "Stable Disease And Complete Response"
        ), new HashMap<>());
        treatmentInfoSection.addComponent(modelDosingStudy);


        webFacetContainer.addSection(pdxModelSection);
        webFacetContainer.addSection(molecularDataSection);
        webFacetContainer.addSection(treatmentInfoSection);
        webFacetContainer.addSection(patientTumorSection);

        /****************************************************************
         *            INITIALIZE SEARCH OBJECTS                         *
         ****************************************************************/


        //one general search object for searching on MFQ object fields
        oneParamCheckboxSearch = new OneParamCheckboxSearch(null, null);

        //drug search
        dosingStudySearch = new TwoParamUnlinkedSearch();
        dosingStudySearch.setData(getModelDrugResponsesFromDP());

        //gene mutation search
        //the gene mutation is a ThreeParamFilter component, but a FourParamLinkedSearch must be used because of the hidden platform labelId
        geneMutationSearch = new ThreeParamLinkedSearch("geneMutation", "mutation");

        geneMutationSearch.setData(getMutationsFromDP());


        //breast cancer markers search initialization
        breastCancerMarkersSearch = new TwoParamLinkedSearch("breastCancerMarkers", "breast_cancer_markers");
        breastCancerMarkersSearch.setData(getBreastCancerMarkersFromDP());

        copyNumberAlterationSearch = new OneParamTextSearch("copyNumberAlteration", "copy_number_alteration", getCopyNumberAlterationDP());


        INITIALIZED = true;
    }


    public WebFacetContainer getUpdatedSelectedFilters(Map<SearchFacetName, List<String>> filters){


        //use a clone to avoid keeping filters from previous iterations
        WebFacetContainer webFacetContainerClone = new WebFacetContainer();
        List<WebFacetSection> sections = new ArrayList<>(webFacetContainer.getWebFacetSections());
        webFacetContainerClone.setWebFacetSections(sections);

        //reset all previously selected fields and make the component inactive
        for(WebFacetSection wfs :webFacetContainerClone.getWebFacetSections()){
            for(GeneralFilter filter: wfs.getFilterComponents()){
                filter.setActive(false);

                if(filter instanceof OneParamCheckboxFilter){

                    OneParamCheckboxFilter f = (OneParamCheckboxFilter)filter;
                    f.setSelected(new ArrayList<>());

                }
                else if(filter instanceof OneParamTextFilter){

                    OneParamTextFilter f = (OneParamTextFilter) filter;
                    f.setSelected(new ArrayList<>());


                }
                else if(filter instanceof TwoParamUnlinkedFilter){

                    TwoParamUnlinkedFilter f = (TwoParamUnlinkedFilter) filter;
                    f.setSelected(new HashMap<>());

                }
                else if(filter instanceof TwoParamLinkedFilter){
                    TwoParamLinkedFilter f = (TwoParamLinkedFilter) filter;
                    f.setSelected(new HashMap<>());
                }
            }
        }


        //loop through the selected filters, make them active and initialize their selected list/map
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

                        if(filter instanceof OneParamCheckboxFilter){

                            OneParamCheckboxFilter f = (OneParamCheckboxFilter)filter;
                            f.setSelected(decodedSelected);

                        }
                        else if(filter instanceof OneParamTextFilter){

                            OneParamTextFilter f = (OneParamTextFilter) filter;
                            f.setSelected(decodedSelected);

                        }
                        else if(filter instanceof TwoParamUnlinkedFilter){

                            TwoParamUnlinkedFilter f = (TwoParamUnlinkedFilter) filter;

                            Map<String,List<String>> selectedMap = new HashMap<>();

                            for(String opt:decodedSelected){

                                String[] optArr = opt.split("___");

                                if(selectedMap.containsKey(optArr[0])){
                                    selectedMap.get(optArr[0]).add(optArr[1]);
                                }
                                else{
                                    List<String> arrList = new ArrayList<>();
                                    arrList.add(optArr[1]);
                                    selectedMap.put(optArr[0], arrList);

                                }

                            }

                            f.setSelected(selectedMap);

                        }
                        else if(filter instanceof TwoParamLinkedFilter){
                            TwoParamLinkedFilter f = (TwoParamLinkedFilter) filter;

                            Map<String,List<String>> selectedMap = new HashMap<>();

                            for(String opt:decodedSelected){

                                String[] optArr = opt.split("___");

                                if(selectedMap.containsKey(optArr[0])){
                                    selectedMap.get(optArr[0]).add(optArr[1]);
                                }
                                else{
                                    List<String> arrList = new ArrayList<>();
                                    arrList.add(optArr[1]);
                                    selectedMap.put(optArr[0], arrList);

                                }

                            }

                            f.setSelected(selectedMap);
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

        //reset breast cancer markers
        result.forEach(x ->x.setBreastCancerMarkers(new ArrayList<>()));

        //reset copy number alteration values
        result.forEach(x -> x.setCnaMarkers(new ArrayList<>()));

        // If no filters have been specified, return the complete set
        if (filters == null) {
            return result;
        }

        OneParamCheckboxSearch oneParamCheckboxSearch = new OneParamCheckboxSearch("search","search");

        for (SearchFacetName facet : filters.keySet()) {

            log.info("Models:"+result.size()+" before applying filter: "+facet.getName());

            switch(facet){

                case query:
                    //List<String> searchParams, Set<ModelForQuery> mfqSet, Function<ModelForQuery, List<String>> searchFunc
                    result = oneParamCheckboxSearch.searchOnCollection(facetOptionMap.get("query"), filters.get(SearchFacetName.query), result, ModelForQuery::getAllOntologyTermAncestors);
                    break;

                case datasource:
                    result = oneParamCheckboxSearch.searchOnString(facetOptionMap.get("datasource"), filters.get(SearchFacetName.datasource), result, ModelForQuery::getDatasource);
                    break;

                case diagnosis:
                    result = oneParamCheckboxSearch.searchOnString(facetOptionMap.get("diagnosis"), filters.get(SearchFacetName.diagnosis), result, ModelForQuery::getMappedOntologyTerm);
                    break;

                case patient_age:
                    result = oneParamCheckboxSearch.searchOnString(facetOptionMap.get("patient_age"), filters.get(SearchFacetName.patient_age), result, ModelForQuery::getPatientAge);
                    break;
                case patient_treatment_status:
                    result = oneParamCheckboxSearch.searchOnString(facetOptionMap.get("patient_treatment_status"), filters.get(SearchFacetName.patient_treatment_status), result, ModelForQuery::getPatientTreatmentStatus);
                    break;

                case patient_gender:
                    result = oneParamCheckboxSearch.searchOnString(facetOptionMap.get("patient_gender"), filters.get(SearchFacetName.patient_gender), result, ModelForQuery::getPatientGender);
                    break;

                case sample_origin_tissue:
                    result = oneParamCheckboxSearch.searchOnString(facetOptionMap.get("sample_origin_tissue"), filters.get(SearchFacetName.sample_origin_tissue), result, ModelForQuery::getSampleOriginTissue);
                    break;

                case sample_classification:
                    result = oneParamCheckboxSearch.searchOnString(facetOptionMap.get("sample_classification"), filters.get(SearchFacetName.sample_classification), result, ModelForQuery::getSampleClassification);
                    break;

                case sample_tumor_type:
                    result = oneParamCheckboxSearch.searchOnString(facetOptionMap.get("sample_tumor_type"), filters.get(SearchFacetName.sample_tumor_type), result, ModelForQuery::getSampleTumorType);
                    break;

                case model_implantation_site:
                    result = oneParamCheckboxSearch.searchOnString(facetOptionMap.get("model_implantation_site"), filters.get(SearchFacetName.model_implantation_site), result, ModelForQuery::getModelImplantationSite);
                    break;

                case model_implantation_type:
                    result = oneParamCheckboxSearch.searchOnString(facetOptionMap.get("model_implantation_type"), filters.get(SearchFacetName.model_implantation_type), result, ModelForQuery::getModelImplantationSite);
                    break;

                case model_host_strain:
                    result = oneParamCheckboxSearch.searchOnCollection(facetOptionMap.get("model_host_strain"), filters.get(SearchFacetName.model_host_strain), result, ModelForQuery::getModelHostStrain);
                    break;

                case cancer_system:
                    result = oneParamCheckboxSearch.searchOnCollection(facetOptionMap.get("cancer_system"), filters.get(SearchFacetName.cancer_system), result, ModelForQuery::getCancerSystem);
                    break;

                case organ:
                    result = oneParamCheckboxSearch.searchOnString(facetOptionMap.get("organ"), filters.get(SearchFacetName.organ), result, ModelForQuery::getCancerOrgan);
                    break;

                case cell_type:
                    result = oneParamCheckboxSearch.searchOnString(facetOptionMap.get("cell_type"), filters.get(SearchFacetName.cell_type), result, ModelForQuery::getCancerCellType);
                    break;

                case project:
                    result = oneParamCheckboxSearch.searchOnCollection(facetOptionMap.get("project"), filters.get(SearchFacetName.project), result, ModelForQuery::getProjects);
                    break;

                case data_available:
                    result = oneParamCheckboxSearch.searchOnCollection(facetOptionMap.get("data_available"), filters.get(SearchFacetName.data_available), result, ModelForQuery::getDataAvailable);
                    break;

                case mutation:
                    result = geneMutationSearch.search(filters.get(SearchFacetName.mutation), result, ModelForQuery::addMutatedVariant);
                    break;

                case drug:
                    result = dosingStudySearch.search(filters.get(SearchFacetName.drug), result, ModelForQuery::addDrugWithResponse);
                    break;

                case breast_cancer_markers:
                    result = breastCancerMarkersSearch.search(filters.get(SearchFacetName.breast_cancer_markers), result, ModelForQuery::addBreastCancerMarkers, ComparisonOperator.AND);
                    break;

                case copy_number_alteration:
                    result = copyNumberAlterationSearch.search(filters.get(SearchFacetName.copy_number_alteration), result, ModelForQuery::addCnaMarker, ComparisonOperator.OR);
                    break;

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
                if(j.has("patientAge")){
                    mfq.setPatientAge(j.getString("patientAge"));
                }
                else{
                    mfq.setPatientAge("Not Specified");
                }

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

                if(j.has("patientTreatmentStatus")){
                    mfq.setPatientTreatmentStatus(j.getString("patientTreatmentStatus"));
                }



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

                if(j.has("accessModalities")){
                    mfq.setAccessModalities(j.getString("accessModalities"));
                }
                else{
                    mfq.setAccessModalities("");
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

    private Map<String, List<String>> getMutationAndVariantOptions(){

        Map<String,Set<String>> tempResults = getMutationOptionsFromDP();

        Map<String, List<String>> resultMap = new HashMap<>();

        for(Map.Entry<String, Set<String>> entry : tempResults.entrySet()){

            resultMap.put(entry.getKey(), new ArrayList<>(new TreeSet<>(entry.getValue())));
        }

        return resultMap;
    }


    private List<String> getMutationOptions(){

        Map<String,Set<String>> tempResults = getMutationOptionsFromDP();

        List<String> resultList = new ArrayList<>();

        for(Map.Entry<String, Set<String>> entry : tempResults.entrySet()){

            resultList.add(entry.getKey());
        }

        return resultList;
    }



    private Map<String, Set<String>> getMutationOptionsFromDP(){

        Map<String, Map<String, Map<String, Set<Long>>>> mutations = getMutationsFromDP();

        Map<String,Set<String>> tempResults = new HashMap<>();

        for(Map.Entry<String, Map<String, Map<String, Set<Long>>>> platform:mutations.entrySet()){

            for(Map.Entry<String, Map<String, Set<Long>>> marker:platform.getValue().entrySet()){

                for(Map.Entry<String, Set<Long>> variant:marker.getValue().entrySet()){

                    String m = marker.getKey();
                    String v = variant.getKey();

                    if(tempResults.containsKey(m)){
                        tempResults.get(m).add(v);
                    }
                    else{
                        Set<String> set = new HashSet<>();
                        set.add(v);
                        tempResults.put(m, set);

                    }

                }
            }
        }

        return tempResults;
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


    private Map<String, Map<String, Set<Long>>> getBreastCancerMarkersFromDP(){

        log.info("Initializing breast cancer markers ");

        Map<String, Map<String, Set<Long>>> data = new HashMap<>();

        DataProjection dataProjection = dataProjectionRepository.findByLabel("cytogenetics");
        String responses = "{}";

        if(dataProjection != null){

            responses = dataProjection.getValue();
        }

        try{

            ObjectMapper mapper = new ObjectMapper();

            data = mapper.readValue(responses, new TypeReference<Map<String, Map<String, Set<Long>>>>(){});


        }
        catch(Exception e){

            e.printStackTrace();
        }

        return data;
    }

    private Map<String, Set<Long>> getCopyNumberAlterationDP(){

        Map<String, Set<Long>> data = new HashMap<>();

        DataProjection dataProjection = dataProjectionRepository.findByLabel("copy number alteration");

        String responses = "{}";

        if(dataProjection != null){

            responses = dataProjection.getValue();
        }

        try{

            ObjectMapper mapper = new ObjectMapper();

            data = mapper.readValue(responses, new TypeReference<Map<String, Set<Long>>>(){});


        }
        catch(Exception e){

            e.printStackTrace();
        }

        return data;
    }


    private List<String> getCopyNumberAlterationOptions(){

        Map<String, Set<Long>> data = getCopyNumberAlterationDP();
        List<String> options = new ArrayList<>(data.keySet());

        return options;
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